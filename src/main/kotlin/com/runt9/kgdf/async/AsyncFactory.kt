package com.runt9.kgdf.async

import kotlinx.coroutines.CoroutineDispatcher
import ktx.async.newSingleThreadAsyncContext

open class AsyncFactory {
    open fun newAsyncContext(threadName: String): CoroutineDispatcher = newSingleThreadAsyncContext(threadName)
}
