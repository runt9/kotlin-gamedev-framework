package com.runt9.kgdf.mcp.tool

import com.badlogic.gdx.scenes.scene2d.Actor
import com.runt9.kgdf.input.InputCode
import com.runt9.kgdf.mcp.tool.output.ToolOutput
import com.runt9.kgdf.ui.controller.Controller

/**
 * What an action id means, resolved when it is asked for rather than when the screen was last read.
 *
 * That timing carries the design. A game's own guards are silent early returns, so an input it will not act on
 * looks exactly like one it did; resolving late is the only chance to answer [Refused] instead.
 */
sealed interface ActionTarget {
    /**
     * Press and release [button] over [actor], with [modifiers] held around it. The actor is converted to a
     * screen point and hit-tested at dispatch, so a control that has since been covered still refuses.
     */
    class Click(
        val actor: Actor,
        val button: InputCode.Button,
        val modifiers: Set<InputCode.Key> = emptySet()
    ) : ActionTarget

    /** The game will not act on this, and [reason] is what a player would see stopping them. */
    class Refused(val reason: String) : ActionTarget
}

/**
 * How a game answers "what is on this screen, and what can be done with it", and where each of those things
 * points.
 *
 * Called **on the rendering thread**, and should read state rather than the actors drawing it: a ViewModel can
 * hold a value whose actor is hidden, so reading the tree can report what the screen does not.
 *
 * Receives the controller that is actually showing, so the ViewModel presenting player-facing state is
 * available rather than re-derived.
 */
interface GameObserver {
    /** Null for a screen the game has nothing to say about. */
    fun observe(screen: Controller): ToolOutput?

    /**
     * Null when this screen has no such action at all, which the harness reports differently from a refusal.
     *
     * Defaulted because listing is useful on its own; a game adds this when it wants the screen driven.
     */
    fun resolve(screen: Controller, action: String): ActionTarget? = null
}
