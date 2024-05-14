package com.runt9.kgdf.ext

inline fun <reified E : Enum<E>> Int.matchOrdinal() = enumValues<E>().find { it.ordinal == this }
fun <E : Enum<E>> E.displayName() = name.lowercase().split('_').joinToString(" ") { it.replaceFirstChar { c -> c.titlecase() } }

