package com.runt9.kgdf.game

import com.runt9.kgdf.util.SeededRandomizer

interface GameState {
    val rng: SeededRandomizer

    fun clone(): GameState
}
