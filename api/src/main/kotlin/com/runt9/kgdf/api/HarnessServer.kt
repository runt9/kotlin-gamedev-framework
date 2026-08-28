package com.runt9.kgdf.api

import com.runt9.kgdf.api.controller.ApiControllerRegistry
import com.runt9.kgdf.api.controller.ApiException
import com.runt9.kgdf.log.kgdfLogger
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

/** Serves the harness over loopback. */
object HarnessServer {
    private val logger = kgdfLogger()

    private val shutdownGrace = 1_000.milliseconds
    private val shutdownTimeout = 2_000.milliseconds
    private const val LOCALHOST = "127.0.0.1"

    /**
     * Runs [runGame] with the server up around it.
     *
     * Wraps [runGame] because the game loop blocks, which bounds the server's lifetime by the game's without
     * either one knowing the other.
     */
    @Suppress("HttpUrlsUsage")
    fun serve(port: Int, runGame: () -> Unit) {
        val server = embeddedServer(CIO, port = port, host = LOCALHOST) {
            routing(ApiControllerRegistry::addRoutesToRouting)

            // Global exception handler that allows exceptions to be thrown to respond with an error as opposed to each controller
            // doing error handling in its own way then having to return from their handler function. Can easily be expanded in the
            // future to support strucutred errors by funneling all exceptions through ApiException
            install(StatusPages) {
                exception<ApiException> { call, cause -> call.respondText(cause.message ?: "An unknown error occurred", status = cause.statusCode) }
            }

            install(ContentNegotiation) {
                json(Json {
                    // Without encodeDefaults a nullable field vanishes from the wire exactly when it is null.
                    encodeDefaults = true
                })
            }
        }.start(wait = false)

        logger.info { "Harness listening on http://$LOCALHOST:$port" }

        try {
            runGame()
        } finally {
            server.stop(shutdownGrace.inWholeMilliseconds, shutdownTimeout.inWholeMilliseconds)
        }
    }
}
