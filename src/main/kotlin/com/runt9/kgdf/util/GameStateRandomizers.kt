package com.runt9.kgdf.util

import com.runt9.kgdf.ext.random
import com.runt9.kgdf.game.GameState
import kotlin.random.Random

val GameState.rng: Random get() = Random(seed.hashCode())

fun <T> GameState.randomizeBasic(action: (Random) -> T) = action(rng)

fun <T : Comparable<T>> GameState.randomize(lucky: Boolean = false, action: (Random) -> T): T {
    val first = action(rng)
    if (lucky) {
        val second = action(rng)
        return maxOf(first, second)
    }

    return first
}

fun GameState.percentChance(percentChance: Float, lucky: Boolean = false) = randomize(lucky) { it.nextFloat() } <= percentChance
fun GameState.coinFlip(lucky: Boolean = false) = randomize(lucky) { rng.nextBoolean() }

fun GameState.randomRange(range: ClosedFloatingPointRange<Float>, lucky: Boolean = false) = randomize(lucky) {
    range.random(it)
}

fun <T> GameState.randomFromCollection(list: Collection<T>) = list.random(rng)
fun <T : Comparable<T>> GameState.randomFromCollection(list: Collection<T>, lucky: Boolean = false) = randomize(lucky) { list.random(it) }
