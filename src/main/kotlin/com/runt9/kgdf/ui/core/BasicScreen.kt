package com.runt9.kgdf.ui.core

import com.badlogic.gdx.InputMultiplexer
import com.runt9.kgdf.ext.lazyInject
import com.runt9.kgdf.ui.DialogManager
import com.runt9.kgdf.ui.controller.Controller
import ktx.app.KtxScreen

abstract class BasicScreen : KtxScreen {
    val stage = BasicStage()
    val input by lazyInject<InputMultiplexer>()
    val dialogManager by lazyInject<DialogManager>()
    abstract val controller: Controller

    override fun render(delta: Float) = stage.render(delta)
    override fun dispose() = stage.dispose()

    override fun show() {
        input.addProcessor(stage)
        controller.load()
        stage.setView(controller.view)
        dialogManager.currentStage = stage
    }

    override fun hide() {
        stage.clear()
        input.removeProcessor(stage)
        controller.dispose()
        dialogManager.currentStage = null
    }
}
