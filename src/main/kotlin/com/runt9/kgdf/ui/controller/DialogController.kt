package com.runt9.kgdf.ui.controller

import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.runt9.kgdf.log.kgdfLogger
import com.runt9.kgdf.ui.core.BasicStage
import com.runt9.kgdf.ui.view.DialogView

abstract class DialogController : Controller {
    private val logger = kgdfLogger()
    abstract override val view: DialogView
    protected var stage: BasicStage? = null

    var isShown = false

    fun show(stage: BasicStage, skipAnimation: Boolean = false) {
        if (!isShown) {
            load()
            this.stage = stage
            logger.info { "Showing, adding to active dialogs" }
            stage.activeDialogs += this
            view.initStage(stage)
            if (skipAnimation) {
                view.show(stage, Actions.run {  })
            } else {
                view.show(stage)
            }
            view.init()
            isShown = true
        }
    }

    fun hide() {
        if (isShown) {
            view.hide()
            isShown = false
            logger.info { "Hiding, removing from active dialogs" }
            stage?.activeDialogs -= this
            stage = null
            dispose()
        }
    }
}
