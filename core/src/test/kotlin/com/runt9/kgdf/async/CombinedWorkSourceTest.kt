package com.runt9.kgdf.async

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

/** Composed over bare [WorkTracker]s: a real consumer would drag a dispatcher in without exercising anything extra. */
class CombinedWorkSourceTest : FunSpec({
    test("a single source behaves exactly like that source alone") {
        val tracker = WorkTracker()
        val combined = CombinedWorkSource(tracker)

        combined.isIdle shouldBe true

        tracker.enter()
        combined.isIdle shouldBe false

        tracker.exit()
        combined.isIdle shouldBe true
    }

    test("isIdle is false while any one source is busy") {
        val a = WorkTracker()
        val b = WorkTracker()
        val c = WorkTracker()
        val combined = CombinedWorkSource(a, b, c)

        combined.isIdle shouldBe true

        // One at a time, or a composite consulting only sources[0] would pass on the busy-a case alone.
        a.enter()
        combined.isIdle shouldBe false
        a.exit()

        b.enter()
        combined.isIdle shouldBe false
        b.exit()

        c.enter()
        combined.isIdle shouldBe false
        c.exit()

        combined.isIdle shouldBe true
    }

    test("awaitIdle stays suspended until every source has drained") {
        runTest {
            val a = WorkTracker()
            val b = WorkTracker()
            val combined = CombinedWorkSource(a, b)
            a.enter()
            b.enter()
            var released = false

            val waiter = launch {
                combined.awaitIdle()
                released = true
            }

            testScheduler.advanceUntilIdle()
            released shouldBe false

            // b is still busy, so a reaching zero must not release the waiter -- the false idle this exists to avoid.
            a.exit()
            testScheduler.advanceUntilIdle()
            released shouldBe false

            b.exit()
            testScheduler.advanceUntilIdle()
            released shouldBe true

            waiter.join()
        }
    }

    test("awaitIdle returns without suspending when every source is already idle") {
        runTest {
            val combined = CombinedWorkSource(WorkTracker(), WorkTracker())

            // No launch and no advance: suspending here would hang the test rather than fail it.
            combined.awaitIdle()

            combined.isIdle shouldBe true
        }
    }

    test("a composite with no sources is idle rather than throwing") {
        runTest {
            val combined = CombinedWorkSource()

            combined.isIdle shouldBe true

            // combine() over an empty array completes without emitting, so an unguarded first {} throws
            // NoSuchElementException here instead of returning.
            shouldNotThrowAny { combined.awaitIdle() }
        }
    }

    test("composites nest, because a composite is itself a source") {
        runTest {
            val inner = WorkTracker()
            val outer = WorkTracker()
            val combined = CombinedWorkSource(CombinedWorkSource(inner), outer)

            inner.enter()
            combined.isIdle shouldBe false

            inner.exit()
            combined.isIdle shouldBe true
        }
    }

    test("describePending names only the sources still outstanding") {
        val busy = WorkTracker()
        val idle = CombinedWorkSource()
        val combined = CombinedWorkSource(busy, idle)

        combined.describePending() shouldBe ""

        busy.enter()
        combined.describePending() shouldContain "WorkTracker"
        combined.describePending() shouldNotContain "CombinedWorkSource"
    }
})
