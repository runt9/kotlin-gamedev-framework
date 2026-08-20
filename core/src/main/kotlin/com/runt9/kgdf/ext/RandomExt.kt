package com.runt9.kgdf.ext

import kotlin.random.Random

private val chars = ('A'..'Z') + ('0'..'9')
fun Random.nextAlphaChar() = chars.random(this)
fun Random.randomString(length: Int) = (1..length).map { nextAlphaChar() }.joinToString("")

fun <T> generateWeightedList(weightMap: Map<T, Int>): List<T> {
    val weightedList = mutableListOf<T>()

    weightMap.forEach { (type, weight) ->
        repeat(weight) { weightedList.add(type) }
    }

    return weightedList.toList()
}
