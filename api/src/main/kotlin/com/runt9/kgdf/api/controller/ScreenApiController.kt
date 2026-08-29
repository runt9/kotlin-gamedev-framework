package com.runt9.kgdf.api.controller

import com.runt9.kgdf.api.observe.ApiScreen
import com.runt9.kgdf.api.observe.ShownScreen
import com.runt9.kgdf.api.result.respondApi
import com.runt9.kgdf.ui.controller.Controller
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.RoutingContext
import kotlin.reflect.KClass

abstract class ScreenApiController<T : Controller>(protected val controllerClass: KClass<T>) : ApiController() {
    /**
     * The path this controller's endpoints hang off, taken from the screen enum that already pairs a route with
     * the controller serving it. Spelling it again per controller is what lets the two drift.
     *
     * Resolved at object initialization, which is the first time anything touches `objectInstance` -- so every
     * screen must already be registered by then, or this throws. Register screens before controllers.
     */
    protected val baseRoute = "/${ApiScreen.forController(controllerClass).route}"

    /**
     * Resolves the live controller and runs [block] against it, both on the rendering thread.
     *
     * Resolution is inside the hop rather than before it so the screen cannot change between the check and the
     * action. Endpoints should reach for this over [controller], which does neither.
     */
    protected suspend fun <R> onScreen(block: T.() -> R): R = onRender { controller.block() }

    /**
     * Builds the response body on the rendering thread and responds with it.
     *
     * Prefer this over `call.respondApi(onScreen { ... })` for anything that reads a ViewModel. Both are correct,
     * but this one cannot be got wrong: the hop is inside the only primitive that answers a request, so there is
     * no way to read a ViewModel and reply without it. Forgetting the hop does not fail loudly -- it works until
     * a binding rebuild happens to allocate a texture, and then the process dies with no catchable exception.
     *
     * `inline` with a `reified` body type for the same reason [respondApi] is: ContentNegotiation resolves the
     * serializer from a runtime `typeInfo`, and a non-reified frame erases it.
     */
    protected suspend inline fun <reified R> RoutingContext.respondOnScreen(noinline block: T.() -> R) =
        call.respondApi(onScreen(block))

    @Suppress("UNCHECKED_CAST")
    protected val controller: T
        get() {
            val shown = ShownScreen.shown() ?: throw ApiException("nothing is showing yet", statusCode = HttpStatusCode.Conflict)

            if (!controllerClass.isInstance(shown)) {
                throw ApiException("that is not the screen showing; currently on ${ApiScreen.current.route}", statusCode = HttpStatusCode.Conflict)
            }

            return shown as T
        }
}
