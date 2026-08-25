package com.runt9.kgdf.mcp.tool.output

import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondBytes
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.ImageContent
import java.util.Base64

class Png(val bytes: ByteArray) : ToolOutput {
    override fun toCallResult() = CallToolResult(
        content = listOf(ImageContent(data = Base64.getEncoder().encodeToString(bytes), mimeType = "image/png"))
    )

    override suspend fun respondToCall(call: ApplicationCall) = call.respondBytes(bytes, ContentType.Image.PNG)
}
