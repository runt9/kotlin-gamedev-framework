package com.runt9.kgdf.mcp.observe

import com.runt9.kgdf.mcp.tool.GameObserver
import com.runt9.kgdf.mcp.tool.RENDER_TIMEOUT
import com.runt9.kgdf.mcp.tool.output.HarnessError
import com.runt9.kgdf.mcp.tool.output.ToolOutput
import kotlinx.coroutines.withTimeout
import ktx.async.onRenderingThread

/**
 * One hop onto the rendering thread, bounded. Everything the harness reads or drives goes through here, so an
 * answer cannot describe two different frames and a stalled render loop surfaces rather than hanging.
 */
internal suspend fun <T> renderHop(block: () -> T): T = withTimeout(RENDER_TIMEOUT) { onRenderingThread { block() } }

/** Reads whatever is on screen. Shared, because every action answers with a fresh observation. */
internal suspend fun GameObserver.observeShownScreen(): ToolOutput = renderHop {
    val screen = ShownScreen.shown() ?: return@renderHop ToolOutput.of(HarnessError(ShownScreen.NOTHING_SHOWING))

    observe(screen) ?: ToolOutput.of(HarnessError("nothing is registered for ${screen::class.simpleName}"))
}
