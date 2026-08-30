package com.runt9.kgdf.api.controller

import com.runt9.kgdf.api.observe.RENDER_TIMEOUT
import com.runt9.kgdf.api.observe.renderHop
import com.runt9.kgdf.async.WorkSource
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
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

    /**
     * @param expected completes the sentence "is not ...", so pass a noun phrase like "a UUID".
     * @throws ApiException 400 when [parse] rejects the value.
     * @throws IllegalStateException when the matched route declares no `{name}` placeholder.
     */
    protected fun <T : Any> RoutingContext.pathParam(name: String, expected: String = "valid", parse: (String) -> T?): T {
        // Not a 400: a matched route always carries its own placeholders, so absence means this name and the
        // route template disagree. Answering the caller blames them for a wiring bug they cannot see.
        val raw = call.pathParameters[name] ?: error("Route declares no '{$name}' placeholder")

        return parse(raw) ?: throw ApiException("$name '$raw' is not $expected", HttpStatusCode.BadRequest)
    }

    suspend fun settle() {
        // Suspends rather than blocks: the work only advances from the render loop, so blocking would occupy
        // the very thread that has to finish it.
        withTimeout(settleTimeout) { work.awaitIdle() }
    }
}
