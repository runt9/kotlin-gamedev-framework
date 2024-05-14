package com.runt9.kgdf.ui.core

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.runt9.kgdf.ui.view.View

class BasicStage(viewport: Viewport = ScreenViewport()) : Stage(viewport) {
    fun render(delta: Float) {
        viewport.apply()
        act(delta)
        draw()
    }

    fun setView(view: View) {
        (view as Group).run {
            root = view
            view.init()
        }
    }
}
