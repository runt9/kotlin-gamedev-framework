package com.runt9.kgdf.mcp.tool

import com.runt9.kgdf.mcp.tool.output.ToolOutput
import kotlin.time.Duration.Companion.seconds

/** Generous: a timeout here means the render loop stopped, which is worth surfacing rather than waiting out. */
internal val RENDER_TIMEOUT = 10.seconds

/**
 * Generous for a different reason: one action can cascade into further work that resolves a frame at a time, so
 * seconds is normal. A timeout here means the cascade never terminated, which is worth surfacing too.
 */
internal val SETTLE_TIMEOUT = 60.seconds

/**
 * One argument a tool accepts. Described here rather than as an MCP schema so a tool never imports the SDK;
 * [com.runt9.kgdf.mcp.HarnessServer] is the only place that shape is known.
 */
data class ToolParameter(
    val name: String,
    val description: String,
    val type: ParameterType = ParameterType.STRING,
    val required: Boolean = false
)

enum class ParameterType {
    STRING,
    INTEGER;

    /** JSON Schema spells these lowercase, and the constant names already match. */
    val schemaName get() = name.lowercase()
}

/**
 * One thing an agent can ask for. Implementations are registered rather than named at the call site, so adding a
 * tool never means editing the server.
 */
interface HarnessTool {
    val name: String

    /** The only instructions the agent gets, so it says what to do next as well as what this returns. */
    val description: String

    val parameters: List<ToolParameter> get() = emptyList()

    /**
     * Arguments arrive as strings from both transports, because the `curl` routes carry query parameters and
     * nothing is lost stringifying an MCP number. A tool declaring no [parameters] receives an empty map.
     */
    suspend fun call(args: Map<String, String>): ToolOutput
}
