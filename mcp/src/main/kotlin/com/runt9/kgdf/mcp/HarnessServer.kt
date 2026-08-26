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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.time.Duration.Companion.milliseconds

/** Serves the harness over loopback. */
object HarnessServer {
    private val logger = kgdfLogger()

    private val shutdownGrace = 1_000.milliseconds
    private val shutdownTimeout = 2_000.milliseconds
    private const val LOCALHOST = "127.0.0.1"

    /** Checked at startup rather than found half-read at runtime, because clients truncate long descriptions. */
    private const val MAX_DESCRIPTION_BYTES = 2048

    /**
     * Runs [runGame] with the server up around it.
     *
     * Wraps [runGame] because the game loop blocks, which bounds the server's lifetime by the game's without
     * either one knowing the other.
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

            addTool(tool.name, tool.description, tool.schema()) { request ->
                tool.call(request.arguments?.asStringMap() ?: emptyMap()).toCallResult()
            }
        }
    }

    /**
     * The MCP shape of a tool's declared parameters, built here so no tool has to import the SDK to describe
     * what it takes.
     */
    private fun HarnessTool.schema() = ToolSchema(
        properties = buildJsonObject {
            parameters.forEach { param ->
                putJsonObject(param.name) {
                    put("type", param.type.schemaName)
                    put("description", param.description)
                }
            }
        },
        required = parameters.filter { it.required }.map { it.name }
    )

    /** Tools take strings because the `curl` routes carry query parameters, and a JSON number stringifies cleanly. */
    private fun JsonObject.asStringMap() = entries.associate { (key, value) -> key to value.jsonPrimitive.content }

    /**
     * The same tools over plain HTTP, because MCP streamable HTTP needs a session handshake and so cannot be
     * driven by `curl`. Both paths call the same tool, so they cannot answer differently.
     */
    private fun Application.curlRoutes(tools: () -> List<HarnessTool>) = routing {
        get("/{tool}") {
            val name = call.parameters["tool"]
            when (val tool = tools().find { it.name == name }) {
                null -> call.respondText("no tool named $name", status = HttpStatusCode.NotFound)
                // Query parameters, so `/click?action=<id>` stays a single shell word.
                else -> tool.call(call.request.queryParameters.entries().associate { it.key to it.value.first() })
                    .respondToCall(call)
            }
        }
    }
}
