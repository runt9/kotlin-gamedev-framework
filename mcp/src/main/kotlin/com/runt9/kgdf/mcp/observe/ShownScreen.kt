package com.runt9.kgdf.mcp.observe

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.runt9.kgdf.ext.lazyInject
import com.runt9.kgdf.game.KgdfGame
import com.runt9.kgdf.ui.view.DialogView

/**
 * Which screen the player is on. **Screen here means screen or topmost open dialog**, which is what a player
 * means by it too.
 */
object ShownScreen {
    private val multiplexer by lazyInject<InputMultiplexer>()

    /**
     * The stage currently receiving input, or null when nothing is showing.
     *
     * Read from the multiplexer because that is the list dispatch itself walks, so nothing is live here without
     * being live for a real click too. **Last wins** — later processors sit on top.
     */
    fun stage(): Stage? = multiplexer.processors.filterIsInstance<Stage>().lastOrNull()

    /**
     * The topmost open dialog if there is one, otherwise the screen behind it. This is the key a game registers
     * its observers against.
     *
     * Named by controller rather than view, because the controller is the type the rest of a game refers to a
     * screen by.
     */
    fun Stage.currentScreenName(): String? {
        root.children.filterIsInstance<DialogView>().lastOrNull { it.isVisible }
            ?.let { return it.controller::class.simpleName }

        val game = Gdx.app?.applicationListener as? KgdfGame ?: return null
        return game.shownScreen::class.simpleName
    }
}
