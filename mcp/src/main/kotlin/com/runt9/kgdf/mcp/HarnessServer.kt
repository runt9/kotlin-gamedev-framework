package com.runt9.kgdf.mcp

import com.runt9.kgdf.log.kgdfLogger
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
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
        val server = embeddedServer(CIO, port = port, host = LOCALHOST) {}.start(wait = false)
        logger.info { "Harness listening on http://$LOCALHOST:$port" }

        try {
            runGame()
        } finally {
            server.stop(shutdownGrace.inWholeMilliseconds, shutdownTimeout.inWholeMilliseconds)
        }
    }
}
