package com.runt9.kgdf.event

import com.badlogic.gdx.utils.Disposable
import com.runt9.kgdf.async.AsyncFactory
import com.runt9.kgdf.async.AsyncWorkQueue
import com.runt9.kgdf.log.kgdfLogger
import kotlinx.coroutines.CancellationException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure


@Suppress("UNCHECKED_CAST")
class EventBus(asyncFactory: AsyncFactory) : Disposable {
    private val logger = kgdfLogger()
    private val queue = AsyncWorkQueue<Event>(asyncFactory, "Event-Thread", ::dispatch)
    private val eventHandlers = mutableMapOf<KClass<out Event>, MutableList<EventHandler<Event>>>()
    private val handlerClasses = mutableSetOf<ClassHandlerMapping>()

    /** Whether every enqueued event has finished dispatching. A plain read, so it is safe on the rendering thread. */
    val isIdle get() = queue.isIdle

    /** Suspends until every enqueued event has finished dispatching. Never call this from a handler -- see [dispatch]. */
    suspend fun awaitIdle() = queue.awaitIdle()

    fun <T : Event> enqueueEvent(event: T) {
        logger.debug { "Enqueuing event $event" }
        queue.submit(event)
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

    fun loop() = queue.start()

    /**
     * Runs every handler for one event, in registration order, and does not return until they all have. That is
     * what keeps dispatch strictly one-event-at-a-time even when a handler suspends, which several consumers
     * depend on for ordering.
     *
     * A handler is caught individually rather than letting the queue's own per-item catch cover it, so one
     * failing handler does not skip the others registered for the same event.
     */
    private suspend fun dispatch(event: Event) {
        eventHandlers[event::class]?.toList()?.forEach {
            logger.debug { "Handling event ${event::class.simpleName}" }
            try {
                it.handle(event)
            } catch (e: CancellationException) {
                throw e // shutdown — must not fall into the catch below
            } catch (e: Exception) {
                val cause = (e as? InvocationTargetException)?.cause ?: e
                logger.error(cause) { "Handler for ${event::class.simpleName} failed; bus continues" }
            }
        }
    }

    override fun dispose() {
        logger.info { "Disposing" }
        queue.dispose()
        eventHandlers.clear()
        handlerClasses.clear()
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
