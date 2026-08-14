package com.runt9.kgdf.event

import kotlin.reflect.KClass

interface EventHandler<in E : Event> {
    suspend fun handle(event: E)
}

inline fun <reified E : Event> eventHandler(crossinline handler: suspend (E) -> Unit) = object : EventHandler<E> {
    override suspend fun handle(event: E) = handler(event)
}

/**
 * Marks a member function as an event handler, picked up by [EventBus.registerHandlers].
 *
 * - The declaring class must not be `private`. Dispatch is reflective, so a private one registers fine and
 *   throws `IllegalCallableAccessException` on its first event.
 * - [eventType] is read only for a zero-parameter function; otherwise the parameter's type wins.
 * - Matching is on the event's exact runtime class, so a handler for a supertype never fires for a subclass.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class HandlesEvent(val eventType: KClass<out Event> = Event::class)
