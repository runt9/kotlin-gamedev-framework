package com.runt9.kgdf.mcp

import com.runt9.kgdf.log.kgdfLogger
import com.runt9.kgdf.mcp.tool.HarnessTool
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Duration.Companion.milliseconds

/**
 * Serves the harness over loopback. Started only from a harness `main()`, which no packaging task reaches, so
 * there is no build in which this can be switched on by accident.
 */
object HarnessServer {
    private val logger = kgdfLogger()

    private val shutdownGrace = 1_000.milliseconds
    private val shutdownTimeout = 2_000.milliseconds
    private const val LOCALHOST = "127.0.0.1"

    /** Clients truncate descriptions, so this is checked at startup rather than found half-read at runtime. */
    private const val MAX_DESCRIPTION_BYTES = 2048

    /**
     * Runs [runGame] with the server up around it.
     *
     * Wraps the incoming [runGame] rather than simply offering up a method call because the game itself is blocking,
     * so either this class has to know about the game, or the game has to know about the MCP. This alternative allows
     * the MCP to _wrap_ the game and control its own lifecycle. This way the MCP knows to stop itself when the game stops
     * without knowing what the game is or how it runs.
     *
     * @param tools resolved on each call rather than passed in, because the server starts before the game and
     *   these are bound during the game's own startup.
     */
    @Suppress("HttpUrlsUsage")
    fun serve(port: Int, name: String, tools: () -> List<HarnessTool>, runGame: () -> Unit) {
        val server = embeddedServer(CIO, port = port, host = LOCALHOST) {
            mcpStreamableHttp { mcpServer(name, tools) }
            curlRoutes(tools)
        }.start(wait = false)
        logger.info { "MCP harness listening on http://$LOCALHOST:$port (MCP at /mcp)" }

        try {
            runGame()
        } finally {
            server.stop(shutdownGrace.inWholeMilliseconds, shutdownTimeout.inWholeMilliseconds)
        }
    }

    private fun mcpServer(name: String, tools: () -> List<HarnessTool>) = Server(
        serverInfo = Implementation(name = name, version = "0.1.0"),
        options = ServerOptions(capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false))),
    ).apply {
        tools().forEach { tool ->
            check(tool.description.toByteArray(Charsets.UTF_8).size <= MAX_DESCRIPTION_BYTES) {
                "${tool.name} description is over the $MAX_DESCRIPTION_BYTES byte limit clients truncate at"
            }

            addTool(tool.name, tool.description, ToolSchema(properties = buildJsonObject { })) {
                tool.call().toCallResult()
            }
        }
    }

    /**
     * The same tools over plain HTTP, because MCP streamable HTTP needs a session handshake and cannot be driven
     * by `curl` — which is how game-side behavior is actually verified. Both paths call the same tool, so they
     * cannot answer differently.
     */
    private fun Application.curlRoutes(tools: () -> List<HarnessTool>) = routing {
        get("/{tool}") {
            val name = call.parameters["tool"]
            when (val tool = tools().find { it.name == name }) {
                null -> call.respondText("no tool named $name", status = HttpStatusCode.NotFound)
                else -> tool.call().respondToCall(call)
            }
        }
    }
}
