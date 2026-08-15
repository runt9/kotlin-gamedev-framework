package com.runt9.kgdf.testsupport

import com.runt9.kgdf.async.AsyncFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

/**
 * Routes kgdfw's async contexts through the test's [TestCoroutineScheduler] so `advanceUntilIdle()` drains them.
 *
 * Only covers contexts created through [AsyncFactory]. Anything calling `newSingleThreadAsyncContext` directly
 * still runs on a real thread, and a test seeing stale state after such a call is hitting that gap, not a bug
 * here. Nothing in kgdfw does that any more, so the gap is only reachable by consumer code that builds its own.
 */
class TestAsyncFactory(private val scheduler: TestCoroutineScheduler) : AsyncFactory() {
    override fun newAsyncContext(threadName: String): CoroutineDispatcher = StandardTestDispatcher(scheduler, threadName)
}
