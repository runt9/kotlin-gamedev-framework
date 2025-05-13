package com.runt9.kgdf.util

import com.runt9.kgdf.game.GameState
import kotlin.random.Random

fun <T> GameState.randomizeBasic(action: (Random) -> T) = rng.randomizeBasic(action)
fun <T : Comparable<T>> GameState.randomize(lucky: Boolean = false, action: (Random) -> T) = rng.randomize(lucky, action)
fun GameState.percentChance(percentChance: Float, lucky: Boolean = false) = rng.percentChance(percentChance, lucky)
fun GameState.coinFlip(lucky: Boolean = false) = rng.coinFlip(lucky)
fun GameState.randomRange(range: ClosedFloatingPointRange<Float>, lucky: Boolean = false) = rng.randomRange(range, lucky)
fun <T> GameState.randomFromCollection(list: Collection<T>) = rng.randomFromCollection(list)
fun <T : Comparable<T>> GameState.randomFromCollection(list: Collection<T>, lucky: Boolean = false) = rng.randomFromCollection(list, lucky)
