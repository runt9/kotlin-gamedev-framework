package com.runt9.kgdf.api.controller

import io.ktor.server.routing.Routing

object ApiControllerRegistry {
    private val controllers = mutableListOf<ApiController>()

    /**
     * Idempotent by identity. The game registers once at startup, but this list is process-global with no
     * teardown, so a test JVM registering per spec would otherwise register the same routes several times.
     */
    fun register(controller: ApiController) {
        if (controllers.none { it === controller }) controllers += controller
    }

    fun addRoutesToRouting(routing: Routing) = controllers.forEach { it.register(routing) }
}
