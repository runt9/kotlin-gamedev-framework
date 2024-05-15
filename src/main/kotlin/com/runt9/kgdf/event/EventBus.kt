@file:OptIn(DelicateCoroutinesApi::class)

package com.runt9.kgdf.event

import com.badlogic.gdx.utils.Disposable
import com.runt9.kgdf.ext.logger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.async.newSingleThreadAsyncContext
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure


@Suppress("UNCHECKED_CAST")
class EventBus : Disposable {
    private val logger = logger()
    private val asyncContext = newSingleThreadAsyncContext("Event-Thread")
    private val eventQueue = Channel<Event>()
    private val eventHandlers = mutableMapOf<KClass<out Event>, MutableList<EventHandler<Event>>>()
    private val handlerClasses = mutableSetOf<ClassHandlerMapping>()

    suspend fun <T : Event> enqueueEvent(event: T) {
        logger.debug { "Enqueuing event $event" }
        if (!eventQueue.isClosedForSend) {
            eventQueue.send(event)
        }
    }

    fun <T : Event> enqueueEventSync(event: T) {
        KtxAsync.launch(asyncContext) { enqueueEvent(event) }
    }

    fun registerHandlers(obj: Any) {
        if (handlerClasses.none { it.obj == obj }) {
            logger.debug { "Initializing event handlers for ${obj::class.simpleName}" }
            handlerClasses.add(ClassHandlerMapping(obj))
        }

        handlerClasses.find { it.obj == obj }?.registerHandlers()
    }

    fun unregisterHandlers(obj: Any) {
        logger.debug { "Deinitializing event handlers for ${obj::class.simpleName}" }
        handlerClasses.find { it.obj == obj }?.unregisterHandlers()
    }

    fun <T : Event> registerHandler(eventType: KClass<T>, handler: EventHandler<T>) {
        eventHandlers.computeIfAbsent(eventType) { mutableListOf() } += handler as EventHandler<Event>
    }

    fun <T : Event> unregisterHandler(eventType: KClass<T>, handler: EventHandler<T>) {
        eventHandlers[eventType]?.remove(handler)
    }

    fun loop() {
        KtxAsync.launch(asyncContext) {
            logger.info { "Starting loop" }
            while (!eventQueue.isClosedForReceive) {
                eventQueue.receiveCatching().apply {
                    if (isFailure) {
                        logger.error { "An error occurred while receiving event from queue. ${this.exceptionOrNull()}" }
                        return@launch
                    }

                    val event = getOrThrow()
                    eventHandlers[event::class]?.toList()?.forEach {
                        logger.debug { "Handling event ${event::class.simpleName}" }
                        it.handle(event)
                    }
                }
            }
            logger.info { "Loop complete." }
        }
    }

    override fun dispose() {
        logger.info { "Disposing" }
        eventQueue.close()
        eventHandlers.clear()
        handlerClasses.clear()
        asyncContext.dispose()
    }

    private inner class ClassHandlerMapping(val obj: Any) {
        private val handlers: Map<KClass<out Event>, EventHandler<Event>>

        init {
            val handlers = mutableMapOf<KClass<out Event>, EventHandler<Event>>()

            obj::class.memberFunctions.filter { it.hasAnnotation<HandlesEvent>() }.forEach { fn ->
                val params = fn.valueParameters

                if (params.isEmpty()) {
                    handlers[fn.findAnnotation<HandlesEvent>()!!.eventType] = eventHandler { if (fn.isSuspend) fn.callSuspend(obj) else fn.call(obj) }
                } else {
                    handlers[params.first().type.jvmErasure as KClass<Event>] = eventHandler { event ->
                        if (fn.isSuspend) fn.callSuspend(obj, event) else fn.call(obj, event)
                    }
                }
            }

            logger.debug { "Found ${handlers.size} event handlers in ${obj::class.simpleName}" }
            this.handlers = handlers.toMap()
        }

        fun registerHandlers() {
//            logger.debug { "Registering event handlers for ${obj::class.simpleName}" }
            handlers.forEach {
//                logger.debug { "Registering handler for ${it.key.simpleName} from ${obj::class.simpleName}" }
                registerHandler(it.key, it.value)
            }
        }

        fun unregisterHandlers() {
//            logger.debug { "Unregistering event handlers for ${obj::class.simpleName}" }
            handlers.forEach {
//                logger.debug { "Unregistering handler for ${it.key.simpleName} from ${obj::class.simpleName}" }
                unregisterHandler(it.key, it.value)
            }
        }
    }
}
