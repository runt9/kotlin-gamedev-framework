package com.runt9.kgdf.mcp.tool.output

import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondText
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class Structured<T>(val value: T, val serializer: KSerializer<T>) : ToolOutput {
    companion object {
        /**
         * `encodeDefaults` matters: without it a property equal to its default is omitted, so a nullable field
         * disappears from the wire exactly when it is null and a client indexing it starts throwing.
         *
         * Not pretty-printed: on a measured payload, indentation was two thirds of the bytes. Pipe `curl`
         * through `jq` when a human has to read one.
         */
        private val json = Json {
            encodeDefaults = true
        }
    }

    /** Unchecked because the serializer travels with its own value, so the two can never be a mismatched pair. */
    @Suppress("UNCHECKED_CAST")
    private fun encode() = json.encodeToString(serializer as KSerializer<Any?>, value)

    override fun toCallResult() = CallToolResult(content = listOf(TextContent(encode())))
    override suspend fun respondToCall(call: ApplicationCall) = call.respondText(encode(), ContentType.Application.Json)
}
