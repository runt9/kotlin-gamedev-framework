@file:Suppress("UNCHECKED_CAST")

package com.runt9.kgdf.service

import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.event.GameStateUpdated
import com.runt9.kgdf.ext.logger
import com.runt9.kgdf.game.GameState

class GameStateService(private val eventBus: EventBus, registry: GameServiceRegistry) : GameService(eventBus, registry) {
    private val logger = logger()
    private lateinit var gameState: GameState

    // TODO: This should probably jump into the service thread to load but the caller expects things to be synchronous
    fun <T : GameState> load() = gameState.clone() as T

    fun save(gameState: GameState) {
        if (!this@GameStateService::gameState.isInitialized || gameState != this@GameStateService.gameState) {
            logger.debug { "Saving run state" }
            this@GameStateService.gameState = gameState
            eventBus.enqueueEventSync(GameStateUpdated(gameState.clone()))
            // TODO: This should also flush the current state to disk
        }
    }

    fun <T : GameState> update(update: T.() -> Unit) = launchOnServiceThread {
        load<T>().apply {
            update()
            save(this)
        }
    }
}
