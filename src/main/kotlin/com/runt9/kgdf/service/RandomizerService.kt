package com.runt9.kgdf.service

import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.ext.random
import com.runt9.kgdf.game.GameState
import kotlin.random.Random

class RandomizerService(private val gameStateService: GameStateService, eventBus: EventBus, registry: GameServiceRegistry) : GameService(eventBus, registry) {
    private lateinit var rng: Random

    override fun startInternal() {
        val state = gameStateService.load<GameState>()
        rng = Random(state.seed.hashCode())
    }

    fun <T> randomizeBasic(action: (Random) -> T) = action(rng)

    fun <T : Comparable<T>> randomize(lucky: Boolean = false, action: (Random) -> T): T {
        val first = action(rng)
        if (lucky) {
            val second = action(rng)
            return maxOf(first, second)
        }

        return first
    }

    fun percentChance(percentChance: Float, lucky: Boolean = false) = randomize(lucky) { it.nextFloat() } <= percentChance
    fun coinFlip(lucky: Boolean = false) = randomize(lucky) { rng.nextBoolean() }

    fun range(range: ClosedFloatingPointRange<Float>, lucky: Boolean = false) = randomize(lucky) {
        range.random(it)
    }

    fun <T> fromCollection(list: Collection<T>) = list.random(rng)
    fun <T : Comparable<T>> fromCollection(list: Collection<T>, lucky: Boolean = false) = randomize(lucky) { list.random(it) }
}
