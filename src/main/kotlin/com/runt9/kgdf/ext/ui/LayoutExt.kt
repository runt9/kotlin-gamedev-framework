package com.runt9.kgdf.ext.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.kotcrab.vis.ui.VisUI
import ktx.scene2d.KTable
import ktx.scene2d.vis.visTable

fun KTable.separator(height: Float, color: Color = Color.BLACK) = visTable {
    background(rectPixmapTexture(1, 1, color).toDrawable())
}.cell(row = true, height = height, growX = true, padTop = height / 2f, padBottom = height / 2f)

fun Table.panelBackground() {
    background(VisUI.getSkin().getDrawable("panel1"))
}
