package com.runt9.kgdf.ui.controller

import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.runt9.kgdf.log.kgdfLogger
import com.runt9.kgdf.ui.core.BasicStage
import com.runt9.kgdf.ui.view.DialogView

abstract class DialogController : Controller {
    abstract override val view: DialogView
    protected var stage: BasicStage? = null

    var isShown = false

    fun show(stage: BasicStage, skipAnimation: Boolean = false) {
        if (!isShown) {
            load()
            this.stage = stage
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
            stage?.activeDialogs -= this
            stage = null
            dispose()
        }
    }
}
