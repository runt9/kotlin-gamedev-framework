package com.runt9.kgdf.api.controller

import com.runt9.kgdf.api.observe.RENDER_TIMEOUT
import com.runt9.kgdf.api.observe.renderHop
import com.runt9.kgdf.async.WorkSource
import io.ktor.server.routing.Routing
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

abstract class ApiController {
    protected abstract val work: WorkSource
    protected val settleTimeout = 60.seconds

    abstract fun register(routing: Routing): Routing

    /**
     * Runs [block] on the rendering thread, bounded by [RENDER_TIMEOUT].
     *
     * Every endpoint that drives a Controller must go through this. Setting a ViewModel binding rebuilds its
     * Scene2D layout synchronously, and a rebuild can allocate a texture -- an OpenGL call, which aborts the JVM
     * outright when it lands on a request thread rather than the one holding the GL context.
     */
    protected suspend fun <R> onRender(block: () -> R): R = renderHop(block)

    suspend fun settle() {
        // Suspends rather than blocks: the work only advances from the render loop, so blocking would occupy
        // the very thread that has to finish it.
        withTimeout(settleTimeout) { work.awaitIdle() }
    }
}
