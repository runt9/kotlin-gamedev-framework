@file:Suppress("UNCHECKED_CAST")

package com.runt9.kgdf.service

import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.event.GameStateUpdated
import com.runt9.kgdf.log.kgdfLogger
import com.runt9.kgdf.game.GameState

abstract class GameStateService<T : GameState>(
    private val eventBus: EventBus,
    registry: GameServiceRegistry,
    private val stateService: SingleFileSaveStateService<T>
) : GameService(eventBus, registry) {
    private val logger = kgdfLogger()
    private lateinit var gameState: T

    // TODO: This should probably jump into the service thread to load but the caller expects things to be synchronous
    fun load() = gameState.clone() as T

    fun save(gameState: T, forceUpdate: Boolean = false) {
        if (!this@GameStateService::gameState.isInitialized || forceUpdate || gameState != this@GameStateService.gameState) {
            logger.debug { "Saving run state" }
            this@GameStateService.gameState = gameState
            eventBus.enqueueEventSync(GameStateUpdated(gameState.clone()))
            stateService.saveState(gameState)
        }
    }

    fun update(forceUpdate: Boolean = false, update: T.() -> Unit) = launchOnServiceThread {
        load().apply {
            update()
            save(this)
        }
    }
}
