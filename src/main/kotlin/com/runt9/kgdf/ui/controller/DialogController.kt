package com.runt9.kgdf.ui.controller

import com.runt9.kgdf.ui.core.BasicStage
import com.runt9.kgdf.ui.view.DialogView

abstract class DialogController : Controller {
    abstract override val view: DialogView
    protected var stage: BasicStage? = null

    var isShown = false

    fun show(stage: BasicStage) {
        if (!isShown) {
            load()
            this.stage = stage
            view.initStage(stage)
            view.show(stage)
            view.init()
            isShown = true
        }
    }

    fun hide() {
        if (isShown) {
            view.hide()
            isShown = false
            stage = null
            dispose()
        }
    }
}
