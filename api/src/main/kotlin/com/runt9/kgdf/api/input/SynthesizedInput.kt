package com.runt9.kgdf.api.input

import com.badlogic.gdx.InputMultiplexer
import com.runt9.kgdf.ext.lazyInject
import com.runt9.kgdf.input.InputCode
import com.runt9.kgdf.api.observe.renderHop

/**
 * Turns a coordinate into the events a mouse or keyboard would have produced.
 *
 * Everything goes through the injected `InputMultiplexer`, which is what a real device feeds, so a click a
 * dialog would have swallowed is swallowed here too and stop-at-first-consumer ordering is preserved. Reaching
 * past it into a screen's own handler would drive a path no player can reach.
 */
object SynthesizedInput {
    private val multiplexer by lazyInject<InputMultiplexer>()

    /** Coordinates straight from the caller, for driving from a screenshot. Nothing validates what is there. */
    suspend fun clickAt(x: Int, y: Int, button: InputCode.Button) = renderHop { press(x, y, button, emptySet()) }

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
