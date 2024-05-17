package com.runt9.kgdf.ui.controller

import com.runt9.kgdf.ui.core.UiScreen

abstract class UiScreenController : Controller, UiScreen() {
    override val uiController: Controller get() = this

    override fun dispose() {
        super<Controller>.dispose()
    }
}
