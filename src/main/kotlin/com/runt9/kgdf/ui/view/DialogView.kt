package com.runt9.kgdf.ui.view

import com.badlogic.gdx.Graphics
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.VisDialog
import com.runt9.kgdf.ext.inject
import com.runt9.kgdf.ui.controller.DialogController
import ktx.scene2d.KTable

abstract class DialogView(override val controller: DialogController, name: String) : VisDialog(name, "dialog"), View {
    protected abstract val widthScale: Float
    protected abstract val heightScale: Float


    val graphics = inject<Graphics>()

    override fun init() {
        setOrigin(Align.center)
        centerWindow()
        isMovable = false

        contentTable.wrapDsl().initContentTable()
        buttonsTable.wrapDsl().initButtons()
    }

    private fun Table.wrapDsl() = object : KTable { override fun <T : Actor> add(actor: T) = this@wrapDsl.add(actor) }

    protected abstract fun KTable.initContentTable()
    protected abstract fun KTable.initButtons()

    override fun hide() {
        super.hide()
        dispose()
        controller.isShown = false
    }

    override fun dispose() {
        contentTable.clear()
        buttonsTable.clear()
        remove()
    }

    override fun getPrefWidth(): Float {
        val viewportWidth = stage?.viewport?.worldWidth ?: graphics.width.toFloat()
        return viewportWidth * widthScale
    }
    override fun getPrefHeight(): Float {
        val viewportHeight = stage?.viewport?.worldHeight ?: graphics.height.toFloat()
        return viewportHeight * heightScale
    }

    fun initStage(stage: Stage) {
        this.stage = stage
        pack()
    }
}
