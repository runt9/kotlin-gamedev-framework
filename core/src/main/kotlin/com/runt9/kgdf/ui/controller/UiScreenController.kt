package com.runt9.kgdf.ui.controller

import com.runt9.kgdf.ui.core.UiScreen

abstract class UiScreenController(width: Float, height: Float) : Controller, UiScreen(width, height) {
    override val uiController: Controller get() = this

    override fun dispose() {
        super<Controller>.dispose()
    }
}
