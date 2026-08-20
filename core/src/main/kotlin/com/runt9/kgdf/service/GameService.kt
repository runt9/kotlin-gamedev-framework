package com.runt9.kgdf.service

import com.badlogic.gdx.utils.Disposable
import com.runt9.kgdf.event.EventBus

@Suppress("LeakingThis")
abstract class GameService(private val eventBus: EventBus, registry: GameServiceRegistry) : Disposable {
    init {
        registry.register(this)
    }

    fun start() {
        eventBus.registerHandlers(this)
        startInternal()
    }

    fun stop() {
        eventBus.unregisterHandlers(this)
        stopInternal()
    }

    protected open fun startInternal() = Unit
    protected open fun stopInternal() = Unit

    open fun tick(delta: Float) = Unit
    override fun dispose() = Unit
}
