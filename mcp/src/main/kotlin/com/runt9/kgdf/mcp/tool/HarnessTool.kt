package com.runt9.kgdf.mcp.tool

import com.runt9.kgdf.mcp.tool.output.ToolOutput
import kotlin.time.Duration.Companion.seconds

/** Generous: a timeout here means the render loop stopped, which is worth surfacing rather than waiting out. */
internal val RENDER_TIMEOUT = 10.seconds

/**
 * One thing an agent can ask for. Implementations are registered rather than named at the call site, so adding a
 * tool never means editing the server.
 */
interface HarnessTool {
    val name: String

    /** The only instructions the agent gets, so it says what to do next as well as what this returns. */
    val description: String

    suspend fun call(): ToolOutput
}

