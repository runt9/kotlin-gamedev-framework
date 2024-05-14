package com.runt9.kgdf.ui.controller

import com.runt9.kgdf.ui.core.BasicScreen

abstract class BasicScreenController : Controller, BasicScreen() {
    override val controller: Controller get() = this

    override fun dispose() {
        super<Controller>.dispose()
    }
}
