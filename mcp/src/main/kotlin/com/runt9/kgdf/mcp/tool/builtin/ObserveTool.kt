package com.runt9.kgdf.mcp.tool.builtin

import com.runt9.kgdf.mcp.observe.observeShownScreen
import com.runt9.kgdf.mcp.tool.GameObserver
import com.runt9.kgdf.mcp.tool.HarnessTool
import com.runt9.kgdf.mcp.tool.output.ToolOutput

class ObserveTool(private val observer: GameObserver) : HarnessTool {
    override val name = "observe"

    override val description =
        "Read the current screen: what is on it and what you can do with it. Anything the player cannot see is " +
                "absent rather than hidden, so a missing field means the screen does not show it. Every action id " +
                "listed here can be passed straight to click."

    override suspend fun call(args: Map<String, String>) = observer.observeShownScreen()
}
