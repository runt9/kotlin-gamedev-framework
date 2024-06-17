package com.runt9.kgdf.ui.core

import com.badlogic.gdx.InputMultiplexer
import com.runt9.kgdf.ext.lazyInject
import com.runt9.kgdf.ui.DialogManager
import com.runt9.kgdf.ui.controller.Controller

/**
 * Used for UI displays such as menus or part of a GameScreen to display UI elements on the screen while
 * allowing the game to control its own grid.
 */
abstract class UiScreen : BaseScreen {
    val uiStage = BasicStage()
    val input by lazyInject<InputMultiplexer>()
    val dialogManager by lazyInject<DialogManager>()
    override val stages = listOf(uiStage)
    abstract val uiController: Controller

    override fun show() {
        input.addProcessor(uiStage)
        uiController.load()
        uiStage.setView(uiController.view)
        dialogManager.currentStage = uiStage
    }

    override fun hide() {
        uiStage.clear()
        input.removeProcessor(uiStage)
        uiController.dispose()
        dialogManager.currentStage = null
    }
}
