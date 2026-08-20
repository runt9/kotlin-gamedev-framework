package com.runt9.kgdf.service

import com.runt9.kgdf.async.AsyncFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ktx.async.KtxAsync

/**
 * One shared background thread for service-layer work that must not run on the rendering thread.
 *
 * Having no callers is normal. Turn-based games have nothing to put here; real-time ones do. Do not delete it on
 * that basis.
 *
 * Injected rather than an object so its context comes from [AsyncFactory] and a test can drain it.
 */
class ServiceAsync(asyncFactory: AsyncFactory) {
    private val serviceContext = asyncFactory.newAsyncContext("Service-Thread")

    fun launchOnServiceThread(block: suspend CoroutineScope.() -> Unit) = KtxAsync.launch(serviceContext, block = block)

    suspend fun onServiceThread(block: suspend CoroutineScope.() -> Unit) = withContext(serviceContext, block = block)
}
