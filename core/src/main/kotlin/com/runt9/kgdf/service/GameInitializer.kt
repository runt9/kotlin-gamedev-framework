package com.runt9.kgdf.service

import com.badlogic.gdx.utils.Disposable

class GameInitializer(private val gameServiceRegistry: GameServiceRegistry) : Disposable {
    fun initialize() {
        gameServiceRegistry.startAll()
    }

    override fun dispose() {
        gameServiceRegistry.stopAll()
    }
}
