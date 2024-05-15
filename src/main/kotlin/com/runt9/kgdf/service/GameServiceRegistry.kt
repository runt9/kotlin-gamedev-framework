package com.runt9.kgdf.service

import com.badlogic.gdx.utils.Disposable

class GameServiceRegistry : Disposable {
    private val registry = mutableSetOf<GameService>()

    fun register(service: GameService) {
        registry += service
    }

    fun startAll() = registry.forEach(GameService::start)
    fun stopAll() = registry.forEach(GameService::stop)
    override fun dispose() = registry.forEach(Disposable::dispose)
}
