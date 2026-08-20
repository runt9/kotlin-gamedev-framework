package com.runt9.kgdf.ui.view

import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.scene2d.KGroup

abstract class GroupView : Group(), View, KGroup {
    override fun remove(): Boolean {
        clear()
        return super.remove()
    }

    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        stageChanged(stage)
    }
}
