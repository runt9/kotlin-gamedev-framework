package com.runt9.kgdf.mcp.tool.builtin

import com.runt9.kgdf.mcp.observe.ShownScreen
import com.runt9.kgdf.mcp.observe.ShownScreen.currentScreenName
import com.runt9.kgdf.mcp.tool.GameObserver
import com.runt9.kgdf.mcp.tool.HarnessTool
import com.runt9.kgdf.mcp.tool.RENDER_TIMEOUT
import com.runt9.kgdf.mcp.tool.output.ToolOutput
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import ktx.async.onRenderingThread

/** Nothing is showing. A normal state during startup, so it is a result rather than a thrown error. */
@Serializable
private data class NothingShowing(val error: String = "no screen is showing")

/** The game registered no observer for this screen. */
@Serializable
private data class UnknownScreen(val screen: String)

class ObserveTool(private val observer: GameObserver) : HarnessTool {
    override val name = "observe"

    override val description =
        "Read the current screen: what is on it and what you can do with it. Anything the player cannot see is " +
            "absent rather than hidden, so a missing field means the screen does not show it."

    /** Read in one render-thread hop, so an observation cannot describe two different frames. */
    override suspend fun call(): ToolOutput = withTimeout(RENDER_TIMEOUT) {
        onRenderingThread {
            val screen = ShownScreen.stage()?.currentScreenName() ?: return@onRenderingThread ToolOutput.of(NothingShowing())

            observer.observe(screen) ?: ToolOutput.of(UnknownScreen(screen))
        }
    }
}
