package com.runt9.kgdf.mcp.observe

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.runt9.kgdf.ext.lazyInject
import com.runt9.kgdf.game.KgdfGame
import com.runt9.kgdf.ui.controller.Controller
import com.runt9.kgdf.ui.core.UiScreen
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
     * The controller for the topmost open dialog if there is one, otherwise for the screen behind it. This is
     * both what a game keys its observers on and the instance they read.
     *
     * This is pushed down into observers rather than observers lazy injecting because [com.runt9.kgdf.ui.DialogManager]
     * builds a dialog's controller with `dynamicInject`, which constructs rather than resolves from the DI container.
     */
    fun Stage.shownController(): Controller? {
        root.children.filterIsInstance<DialogView>().lastOrNull { it.isVisible }?.let { return it.controller }

        val game = Gdx.app?.applicationListener as? KgdfGame ?: return null
        return (game.shownScreen as? UiScreen)?.uiController
    }
}
