package com.runt9.kgdf.event

import com.runt9.kgdf.log.LogLevel
import com.runt9.kgdf.testsupport.TestAsyncFactory
import com.runt9.kgdf.testsupport.capturingLogs
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.time.Duration.Companion.milliseconds

open class BaseEvent : Event
class DerivedEvent : BaseEvent()
class ValueEvent(val value: Int) : Event

// Not `private`, deliberately. EventBus dispatches reflectively via KCallable.call, which cannot reach a member
// of a private class and throws IllegalCallableAccessException at dispatch time rather than failing to compile.
class Recorder {
    val seen = mutableListOf<String>()

    @HandlesEvent
    fun onValue(event: ValueEvent) {
        seen += "value:${event.value}"
    }

    @HandlesEvent
    fun onBase(event: BaseEvent) {
        seen += "base"
    }
}

class ThrowingRecorder {
    val seen = mutableListOf<String>()

    @HandlesEvent
    fun onValue(event: ValueEvent) {
        seen += "value:${event.value}"
        if (event.value == 1) error("deliberate handler failure")
    }
}

// Records entry and exit separately so an interleaving is visible in the order, not just inferable from it.
class SuspendingRecorder {
    val seen = mutableListOf<String>()

    @HandlesEvent
    suspend fun onValue(event: ValueEvent) {
        seen += "enter:${event.value}"
        delay(50.milliseconds)
        seen += "exit:${event.value}"
    }
}

/**
 * Characterization tests pinning EventBus's observable contract before it is restructured.
 *
 * These describe behaviour that must survive the change, not behaviour that is necessarily desirable — the
 * double-registration case in particular documents a known trap rather than an intended feature.
 *
 * Deliberately no LibGDX application: nothing here needs one.
 */
class EventBusTest : FunSpec({
    fun busOn(scheduler: TestCoroutineScheduler) = EventBus(TestAsyncFactory(scheduler)).also { it.loop() }

    test("an event enqueued from a non-suspend caller reaches its handler").config(coroutineTestScope = true) {
        val bus = busOn(testCoroutineScheduler)
        val recorder = Recorder()
        bus.registerHandlers(recorder)

        bus.enqueueEvent(ValueEvent(42))
        testCoroutineScheduler.advanceUntilIdle()

        recorder.seen shouldContainExactly listOf("value:42")
    }

    test("dispatch matches the exact runtime class, so a base-type handler ignores a subclass").config(coroutineTestScope = true) {
        val bus = busOn(testCoroutineScheduler)
        val recorder = Recorder()
        bus.registerHandlers(recorder)

        // Positive control first: the handler does fire for its own exact type, so an empty result below
        // means "subclass was not matched" rather than "registration never worked".
        bus.enqueueEvent(BaseEvent())
        testCoroutineScheduler.advanceUntilIdle()
        recorder.seen shouldContainExactly listOf("base")

        bus.enqueueEvent(DerivedEvent())
        testCoroutineScheduler.advanceUntilIdle()

        recorder.seen shouldContainExactly listOf("base")
    }

    test("registering the same object twice fires its handler twice, and one unregister leaves one copy").config(coroutineTestScope = true) {
        val bus = busOn(testCoroutineScheduler)
        val recorder = Recorder()
        bus.registerHandlers(recorder)
        bus.registerHandlers(recorder)

        bus.enqueueEvent(ValueEvent(1))
        testCoroutineScheduler.advanceUntilIdle()
        recorder.seen shouldContainExactly listOf("value:1", "value:1")

        bus.unregisterHandlers(recorder)
        bus.enqueueEvent(ValueEvent(2))
        testCoroutineScheduler.advanceUntilIdle()

        recorder.seen shouldContainExactly listOf("value:1", "value:1", "value:2")
    }

    test("events are handled in the order they were enqueued").config(coroutineTestScope = true) {
        val bus = busOn(testCoroutineScheduler)
        val recorder = Recorder()
        bus.registerHandlers(recorder)

        (1..5).forEach { bus.enqueueEvent(ValueEvent(it)) }
        testCoroutineScheduler.advanceUntilIdle()

        recorder.seen shouldContainExactly (1..5).map { "value:$it" }
    }

    // The property the settled-predicate design depends on: while a handler is suspended, the bus is still
    // occupied by it. If dispatch ever became per-event coroutines, the exits and entries would interleave here.
    test("a suspending handler completes before the next event is dispatched").config(coroutineTestScope = true) {
        val bus = busOn(testCoroutineScheduler)
        val recorder = SuspendingRecorder()
        bus.registerHandlers(recorder)

        bus.enqueueEvent(ValueEvent(1))
        bus.enqueueEvent(ValueEvent(2))
        testCoroutineScheduler.advanceUntilIdle()

        recorder.seen shouldContainExactly listOf("enter:1", "exit:1", "enter:2", "exit:2")
    }

    // One bad handler must not take the bus down with it. Before this was fixed, the exception cancelled the
    // loop coroutine and nothing was ever dispatched again — which from outside looks like the application
    // quietly stopping rather than like an error.
    test("a handler that throws does not stop the bus, and the failure is logged").config(coroutineTestScope = true) {
        capturingLogs(LogLevel.TRACE) { sink ->
            val bus = busOn(testCoroutineScheduler)
            val recorder = ThrowingRecorder()
            bus.registerHandlers(recorder)

            bus.enqueueEvent(ValueEvent(1))
            testCoroutineScheduler.advanceUntilIdle()
            bus.enqueueEvent(ValueEvent(2))
            testCoroutineScheduler.advanceUntilIdle()

            recorder.seen shouldContainExactly listOf("value:1", "value:2")
            sink.messagesAt(LogLevel.ERROR).size shouldBe 1
        }
    }

    test("dispose stops the loop, and a clean close is not reported as an error").config(coroutineTestScope = true) {
        capturingLogs(LogLevel.TRACE) { sink ->
            val bus = busOn(testCoroutineScheduler)
            val recorder = Recorder()
            bus.registerHandlers(recorder)

            bus.dispose()
            testCoroutineScheduler.advanceUntilIdle()

            // Re-register AFTER disposing, so this cannot pass merely because dispose() also clears the handler
            // map. The only thing left that can stop delivery is the channel being closed and the loop gone.
            bus.registerHandlers(recorder)
            bus.enqueueEvent(ValueEvent(1))
            testCoroutineScheduler.advanceUntilIdle()

            recorder.seen shouldBe emptyList()
            sink.messagesAt(LogLevel.ERROR) shouldBe emptyList()
        }
    }

    // pending exists so the bus can be composed with other WorkSources. The count has to be up the moment
    // enqueueEvent returns, not once the Event-Thread picks the event up, or a caller checking whether it is
    // safe to act sees zero during the gap between the two.
    test("pending counts an event from the moment it is enqueued until its handler finishes").config(coroutineTestScope = true) {
        val bus = busOn(testCoroutineScheduler)
        val recorder = SuspendingRecorder()
        bus.registerHandlers(recorder)

        bus.pending.value shouldBe 0

        bus.enqueueEvent(ValueEvent(1))
        // Deliberately no advance: this is the window the counting-on-submit rule exists to close.
        bus.pending.value shouldBe 1
        bus.isIdle shouldBe false

        bus.enqueueEvent(ValueEvent(2))
        bus.pending.value shouldBe 2

        testCoroutineScheduler.advanceUntilIdle()

        bus.pending.value shouldBe 0
        bus.isIdle shouldBe true
        recorder.seen shouldContainExactly listOf("enter:1", "exit:1", "enter:2", "exit:2")
    }
})
