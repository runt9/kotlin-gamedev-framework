package com.runt9.kgdf.game

interface GameState {
    /**
     * Used to seed randomizer.
     */
    val seed: String

    fun clone(): GameState
}
