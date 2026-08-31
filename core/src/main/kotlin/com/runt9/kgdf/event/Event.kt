package com.runt9.kgdf.event

interface Event {
    val name: String get() = this::class.simpleName ?: "Event"
}
