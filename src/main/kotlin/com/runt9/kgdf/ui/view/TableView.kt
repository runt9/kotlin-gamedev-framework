package com.runt9.kgdf.ui.view

import com.badlogic.gdx.scenes.scene2d.Stage
import com.kotcrab.vis.ui.widget.VisTable
import ktx.scene2d.KTable

abstract class TableView : VisTable(), View, KTable {
    override fun remove(): Boolean {
        clear()
        return super.remove()
    }

    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        stageChanged(stage)
    }
}
