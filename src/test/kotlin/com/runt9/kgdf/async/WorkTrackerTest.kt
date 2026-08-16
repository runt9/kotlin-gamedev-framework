package com.runt9.kgdf.async

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * `runTest` rather than the Kotest scheduler used by [AsyncWorkQueueTest]: a tracker owns no dispatcher, so the
 * only thing these need a test scope for is launching a waiter alongside the assertions.
 */
class WorkTrackerTest : FunSpec({
    test("a balanced enter and exit returns the tracker to idle") {
        val tracker = WorkTracker()
        tracker.isIdle shouldBe true

        tracker.enter()
        tracker.pending.value shouldBe 1
        tracker.isIdle shouldBe false

        tracker.exit()
        tracker.pending.value shouldBe 0
        tracker.isIdle shouldBe true
    }

    test("awaitIdle returns without suspending when the tracker is already idle") {
        runTest {
            val tracker = WorkTracker()

            // No launch and no advance: if awaitIdle suspended here the test would hang rather than fail, which
            // is the whole point of StateFlow.first testing the current value before it parks.
            tracker.awaitIdle()

            tracker.isIdle shouldBe true
        }
    }

    test("awaitIdle stays suspended until the last exit") {
        runTest {
            val tracker = WorkTracker()
            tracker.enter()
            tracker.enter()
            var released = false

            val waiter = launch {
                tracker.awaitIdle()
                released = true
            }

            testScheduler.advanceUntilIdle()
            released shouldBe false

            tracker.exit()
            testScheduler.advanceUntilIdle()
            // Still one outstanding, so reaching a non-zero value must not release the waiter.
            released shouldBe false

            tracker.exit()
            testScheduler.advanceUntilIdle()
            released shouldBe true

            waiter.join()
        }
    }

    test("concurrent enters from many threads are all counted") {
        val tracker = WorkTracker()
        val threads = 8
        val entersPerThread = 2_000
        val start = CountDownLatch(1)
        val done = CountDownLatch(threads)

        repeat(threads) {
            Thread {
                start.await()
                repeat(entersPerThread) { tracker.enter() }
                done.countDown()
            }.start()
        }

        start.countDown()
        done.await(30, TimeUnit.SECONDS) shouldBe true

        // Fails if enter() is ever rewritten as `value = value + 1`, which reads and writes non-atomically and
        // loses increments under exactly this contention with no error anywhere.
        tracker.pending.value shouldBe threads * entersPerThread
    }

    test("reset reports what was outstanding and zeroes the count") {
        val tracker = WorkTracker()
        repeat(3) { tracker.enter() }

        tracker.reset() shouldBe 3

        tracker.pending.value shouldBe 0
        tracker.isIdle shouldBe true
        tracker.reset() shouldBe 0
    }
})
