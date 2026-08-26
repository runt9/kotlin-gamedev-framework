package com.runt9.kgdf.mcp.tool.builtin

import com.badlogic.gdx.Input
import com.runt9.kgdf.async.WorkSource
import com.runt9.kgdf.input.InputCode
import com.runt9.kgdf.log.kgdfLogger
import com.runt9.kgdf.mcp.input.SynthesizedInput
import com.runt9.kgdf.mcp.observe.ShownScreen
import com.runt9.kgdf.mcp.observe.observeShownScreen
import com.runt9.kgdf.mcp.observe.renderHop
import com.runt9.kgdf.mcp.tool.ActionTarget
import com.runt9.kgdf.mcp.tool.GameObserver
import com.runt9.kgdf.mcp.tool.HarnessTool
import com.runt9.kgdf.mcp.tool.ParameterType
import com.runt9.kgdf.mcp.tool.SETTLE_TIMEOUT
import com.runt9.kgdf.mcp.tool.ToolParameter
import com.runt9.kgdf.mcp.tool.output.HarnessError
import com.runt9.kgdf.mcp.tool.output.ToolOutput
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/** Does what a player's mouse does, then answers with the screen that resulted. */
class ClickTool(private val observer: GameObserver, private val work: WorkSource) : HarnessTool {
    private val logger = kgdfLogger()

    override val name = "click"

    override val description =
        "Do one thing a player could do with the mouse, then read back the screen it produced. Pass an action " +
                "id from the last observation, which is the normal way. Pass x and y instead to click a raw " +
                "screen point, which is for driving from a screenshot and checks nothing about what is there. " +
                "Returns the new screen, or an error saying what stopped it."

    override val parameters = listOf(
        ToolParameter("action", "An action id taken from an observation"),
        ToolParameter("x", "Screen x, only with y, and only for clicking a raw point", ParameterType.INTEGER),
        ToolParameter("y", "Screen y, only with x, and only for clicking a raw point", ParameterType.INTEGER),
        ToolParameter("button", "left (default) or right, only for a raw point")
    )

    override suspend fun call(args: Map<String, String>): ToolOutput {
        val action = args["action"]
        val x = args["x"]?.toIntOrNull()
        val y = args["y"]?.toIntOrNull()

        val refusal = when {
            action != null -> deliver(action)
            x != null && y != null -> renderHop { SynthesizedInput.clickAt(x, y, button(args["button"])); null }
            else -> return ToolOutput.of(HarnessError("pass either an action id, or both x and y"))
        }

        if (refusal != null) return ToolOutput.of(HarnessError(refusal))

        settle()
        return observer.observeShownScreen()
    }

    /**
     * Resolve and dispatch inside one hop. Splitting them would let a frame land in between, so the actor a
     * refusal was decided against is not necessarily the one the click reaches.
     */
    private suspend fun deliver(action: String) = renderHop {
        val screen = ShownScreen.shown() ?: return@renderHop ShownScreen.NOTHING_SHOWING

        when (val target = observer.resolve(screen, action)) {
            null -> "${screen::class.simpleName} has no action '$action'"
            is ActionTarget.Refused -> target.reason
            is ActionTarget.Click -> SynthesizedInput.click(target)
        }
    }

    /**
     * Suspends, never blocks: the work being waited on only advances from the render loop, so blocking a thread
     * would occupy the very one that has to finish it.
     */
    private suspend fun settle() {
        try {
            withTimeout(SETTLE_TIMEOUT) { work.awaitIdle() }
        } catch (e: TimeoutCancellationException) {
            logger.error(e) { "Game never settled after $SETTLE_TIMEOUT" }
            throw e
        }
    }

    private fun button(name: String?) =
        InputCode.Button(if (name == "right") Input.Buttons.RIGHT else Input.Buttons.LEFT)
}
