package com.runt9.kgdf.mcp.tool

import com.runt9.kgdf.mcp.tool.output.ToolOutput
import com.runt9.kgdf.ui.controller.Controller

/**
 * How a game answers "what is on this screen, and what can be done with it".
 *
 * Called **on the rendering thread**, and should read state rather than the actors drawing it: a ViewModel can
 * hold a value whose actor is hidden, so reading the tree can report what the screen does not.
 *
 * Receives the controller that is actually showing so direct access to the ViewModel presenting player-facing
 * state is available rather than relying on re-deriving things the controller/view may be doing.
 *
 * Returns null for a screen the game has nothing to say about.
 */
fun interface GameObserver {
    fun observe(screen: Controller): ToolOutput?
}
