package com.runt9.kgdf.ext

fun <T : Any> List<T>.removeIf(predicate: (T) -> Boolean): List<T> {
    val mutList = toMutableList()
    mutList.removeIf(predicate)
    return mutList.toList()
}
