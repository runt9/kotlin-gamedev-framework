package com.runt9.kgdf.util

import com.runt9.kgdf.ext.random
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

@Serializable
class SeededRandomizer(val seed: Int, var rngCounter: Int) {

    // Don't serialize the rng, but store how many times we've grabbed the "next something" and when we deserialize the state, spin the rng back to its previous state
    @Transient
    private val rng = Random(seed).apply { repeat(rngCounter) { nextInt() } }

    fun <T> randomizeBasic(action: (Random) -> T): T {
        rngCounter++
        return action(rng)
    }

    fun <T : Comparable<T>> randomize(lucky: Boolean = false, action: (Random) -> T): T {
        val first = randomizeBasic(action)
        if (lucky) {
            val second = randomizeBasic(action)
            return maxOf(first, second)
        }

        return first
    }

    fun percentChance(percentChance: Float, lucky: Boolean = false) = randomize(lucky) { it.nextFloat() } <= percentChance
    fun coinFlip(lucky: Boolean = false) = randomize(lucky) { it.nextBoolean() }

    fun randomRange(range: ClosedFloatingPointRange<Float>, lucky: Boolean = false) = randomize(lucky) {
        range.random(it)
    }

    fun <T> randomFromCollection(list: Collection<T>) = randomizeBasic { list.random(it) }
    fun <T : Comparable<T>> randomFromCollection(list: Collection<T>, lucky: Boolean = false) = randomize(lucky) { list.random(it) }
}
