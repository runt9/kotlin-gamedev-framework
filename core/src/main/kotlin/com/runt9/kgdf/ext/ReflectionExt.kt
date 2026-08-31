package com.runt9.kgdf.ext

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

@Suppress("UNCHECKED_CAST")
fun <R> Any.readInstanceProperty(propertyName: String) =
    (this::class.memberProperties.first { it.name == propertyName } as KProperty1<Any, *>).get(this) as R
