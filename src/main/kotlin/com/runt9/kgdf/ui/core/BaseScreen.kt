package com.runt9.kgdf.ui.core

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Disposable
import ktx.app.KtxScreen

interface BaseScreen : KtxScreen {
    val stages: List<BasicStage>

    override fun render(delta: Float) = stages.forEach { it.render(delta) }
    override fun hide() = stages.forEach(Stage::clear)

    override fun dispose() {
        stages.forEach(Disposable::dispose)
    }
}
