@file:Suppress("UNCHECKED_CAST")

package com.runt9.kgdf.service

import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.event.GameStateUpdated
import com.runt9.kgdf.game.GameState
import com.runt9.kgdf.log.kgdfLogger
import com.runt9.kgdf.service.ServiceAsync.launchOnServiceThread

abstract class GameStateService<T : GameState, E : GameStateUpdated<T>>(
    private val eventBus: EventBus,
    private val stateService: SingleFileSaveStateService<T>
) {
    private val logger = kgdfLogger()
    private lateinit var gameState: T

    fun load(): T {
        if (!this@GameStateService::gameState.isInitialized) {
            if (stateService.hasSavedFile()) {
                gameState = stateService.loadState()
            } else {
                gameState = initNewState()
                stateService.saveState(gameState)
            }
        }

        return gameState.clone() as T
    }

    fun save(gameState: T, forceUpdate: Boolean = false) {
        if (!this@GameStateService::gameState.isInitialized || forceUpdate || gameState != this@GameStateService.gameState) {
            logger.debug { "Saving game state" }
            this@GameStateService.gameState = gameState
            eventBus.enqueueEventSync(updatedEvent(gameState.clone() as T))
            stateService.saveState(gameState)
        }
    }

    fun update(forceUpdate: Boolean = false, update: T.() -> Unit) = launchOnServiceThread {
        load().apply {
            update()
            save(this, forceUpdate)
        }
    }

    abstract fun initNewState(): T
    abstract fun updatedEvent(state: T): E
}
