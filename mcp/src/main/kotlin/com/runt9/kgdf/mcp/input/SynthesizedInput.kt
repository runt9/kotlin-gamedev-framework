package com.runt9.kgdf.mcp.input

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.math.Vector2
import com.runt9.kgdf.ext.lazyInject
import com.runt9.kgdf.input.InputCode
import com.runt9.kgdf.mcp.tool.ActionTarget

/**
 * Turns a resolved [ActionTarget] into the events a mouse or keyboard would have produced.
 *
 * Everything goes through the injected `InputMultiplexer`, which is what a real device feeds, so a click a
 * dialog would have swallowed is swallowed here too and stop-at-first-consumer ordering is preserved. Reaching
 * past it into a screen's own handler would drive a path no player can reach.
 *
 * **Every method here must be called on the rendering thread.**
 */
internal object SynthesizedInput {
    private val multiplexer by lazyInject<InputMultiplexer>()

    /** The refusal a player would have run into, or null once the events have been delivered. */
    fun click(target: ActionTarget.Click): String? {
        val actor = target.actor
        val stage = actor.stage ?: return "it is not on screen"
        val center = actor.localToStageCoordinates(Vector2(actor.width / 2f, actor.height / 2f))

        // Asking the stage what it would hit answers occlusion and modality together, through the same dispatch
        // a real mouse goes through. Deriving either separately means re-implementing Scene2D and disagreeing
        // with it. The descendant clause matters because a childrenOnly Table is never itself the hit result.
        val hit = stage.hit(center.x, center.y, true) ?: return "nothing there is taking input"
        if (hit != actor && !hit.isDescendantOf(actor)) {
            return "it is covered by ${hit::class.simpleName ?: "another actor"}"
        }

        val screen = stage.stageToScreenCoordinates(center)
        press(screen.x.toInt(), screen.y.toInt(), target.button, target.modifiers)
        return null
    }

    /** Coordinates straight from the caller, for driving from a screenshot. Nothing validates what is there. */
    fun clickAt(x: Int, y: Int, button: InputCode.Button) = press(x, y, button, emptySet())

    /**
     * Modifiers are held across the press and released after it, all inside one render-thread hop, so synthetic
     * held state never spans a frame and cannot leak into whatever the player does next.
     */
    private fun press(x: Int, y: Int, button: InputCode.Button, modifiers: Set<InputCode.Key>) {
        modifiers.forEach { multiplexer.keyDown(it.keycode) }
        multiplexer.touchDown(x, y, 0, button.buttonId)
        multiplexer.touchUp(x, y, 0, button.buttonId)
        modifiers.forEach { multiplexer.keyUp(it.keycode) }
    }
}
