package com.runt9.kgdf.testsupport

import com.runt9.kgdf.async.AsyncFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

/**
 * Routes kgdfw's async contexts through the test's [TestCoroutineScheduler] so `advanceUntilIdle()` drains them.
 *
 * Only covers contexts created through [AsyncFactory], which is every context in kgdfw — `AsyncFactory` itself is
 * the sole caller of `newSingleThreadAsyncContext`. Keep it that way: a context built directly runs on a real
 * thread no scheduler can drain, and a test seeing stale state afterward is hitting that gap rather than a bug
 * here. Consumer code can still open it.
 */
class TestAsyncFactory(private val scheduler: TestCoroutineScheduler) : AsyncFactory() {
    override fun newAsyncContext(threadName: String): CoroutineDispatcher = StandardTestDispatcher(scheduler, threadName)
}
