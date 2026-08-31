package com.runt9.kgdf.ui.view

abstract class ScreenView : TableView() {
    override fun init() {
        stage?.apply {
            setSize(width, height)
        }
    }
}
