package com.runt9.kgdf.event

import com.runt9.kgdf.game.GameState

open class GameStateUpdated<T : GameState>(val newState: T) : Event
