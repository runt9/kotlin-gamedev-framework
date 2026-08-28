package com.runt9.kgdf.api.controller

import com.runt9.kgdf.async.WorkSource
import com.runt9.kgdf.ext.lazyInject
import io.ktor.server.routing.Routing
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

abstract class ApiController {
    protected abstract val work: WorkSource
    protected val settleTimeout = 60.seconds

    abstract fun register(routing: Routing): Routing

    suspend fun settle() {
        // Suspends rather than blocks: the work only advances from the render loop, so blocking would occupy
        // the very thread that has to finish it.
        withTimeout(settleTimeout) { work.awaitIdle() }
    }
}
