package com.runt9.kgdf.ui.core

import ktx.app.KtxInputAdapter

abstract class GameCameraController : KtxInputAdapter {
    abstract fun update(delta: Float)
}
