package com.runt9.kgdf.input

import ktx.app.KtxInputAdapter

/**
 * Feeds [InputTrackingService] from the real input stream.
 *
 * Belongs at multiplexer index 0 and returns `false` from every handler, so it sees every event before anything
 * can consume one and consumes nothing itself. Anywhere else it stops seeing whatever the processor ahead of it
 * swallows, which shows up as held state that is silently wrong rather than as an error.
 */
class InputTracker(private val inputTracking: InputTrackingService) : KtxInputAdapter {
    override fun keyDown(keycode: Int): Boolean {
        inputTracking.keyDown(keycode)
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        inputTracking.keyUp(keycode)
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        inputTracking.buttonDown(button)
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        inputTracking.buttonUp(button)
        return false
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        inputTracking.buttonUp(button)
        return false
    }
}
