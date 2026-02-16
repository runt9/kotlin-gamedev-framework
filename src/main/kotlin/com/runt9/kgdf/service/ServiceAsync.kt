package com.runt9.kgdf.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext

object ServiceAsync {
    val serviceContext = newSingleThreadAsyncContext("Service-Thread")

    fun launchOnServiceThread(block: suspend CoroutineScope.() -> Unit) = KtxAsync.launch(serviceContext, block = block)
    suspend fun onServiceThread(block: suspend CoroutineScope.() -> Unit) = withContext(serviceContext, block = block)
}
