package com.runt9.kgdf.ui.view

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Disposable
import com.runt9.kgdf.ui.Updatable
import com.runt9.kgdf.ui.controller.Controller
import com.runt9.kgdf.ui.viewModel.ViewModel

interface View : Disposable, Updatable {
    val controller: Controller
    val vm: ViewModel

    fun init()
    override fun update() = Unit
    fun remove(): Boolean
    fun getStage(): Stage?

    override fun dispose() {
        if (getStage() != null) {
            remove()
        }
    }

    fun stageChanged(stage: Stage?) {
        // Ensures that when this actor is removed from the stage, regardless of how, dispose gets called
        if (stage == null) {
            controller.dispose()
        }
    }
}
