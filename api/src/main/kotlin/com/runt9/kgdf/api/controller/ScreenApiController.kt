package com.runt9.kgdf.api.controller

import com.runt9.kgdf.api.observe.ApiScreen
import com.runt9.kgdf.api.observe.ShownScreen
import com.runt9.kgdf.api.result.respondApi
import com.runt9.kgdf.api.result.respondNoData
import com.runt9.kgdf.ui.controller.Controller
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.RoutingContext
import kotlin.reflect.KClass

abstract class ScreenApiController<C>(protected val controllerClass: KClass<out Controller>) : ApiController() {
    /**
     * The screen's route, leading slash included.
     *
     * Lazy so that constructing a controller does not need the screen registry. Routes are built from
     * [register], which runs well after screens are registered, and eager resolution made merely reflecting
     * over a subclass fatal.
     */
    protected val baseRoute by lazy { "/${ApiScreen.forController(controllerClass).route}" }

    /**
     * Resolves the live controller and runs [block] against it, both on the rendering thread, then waits for the
     * work it queued to drain.
     *
     * Resolution is inside the hop rather than before it so the screen cannot change between the check and the
     * action. Endpoints should reach for this over [controller], which does neither.
     *
     * [settle] defaults on because all but one action wants it, and forgetting it answers a caller before the
     * game has finished reacting. Pass `false` for a pure read, or where settling cannot finish -- exiting tears
     * down the render loop the work would drain on.
     */
    protected suspend fun <R> onScreen(settle: Boolean = true, block: C.() -> R): R =
        onRender { controller.block() }.also { if (settle) settle() }

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
    protected suspend inline fun <reified R> RoutingContext.respondOnScreen(noinline block: C.() -> R) =
        call.respondApi(onScreen(settle = false, block = block))

    @Suppress("UNCHECKED_CAST")
    protected val controller: C
        get() {
            val shown = ShownScreen.shown() ?: throw ApiException("nothing is showing yet", statusCode = HttpStatusCode.Conflict)

            if (!controllerClass.isInstance(shown)) {
                throw ApiException("that is not the screen showing; currently on ${ApiScreen.current.route}", statusCode = HttpStatusCode.Conflict)
            }

            return shown as C
        }

    /** The write-side counterpart to [respondOnScreen]: do the thing, answer with nothing but `currentScreen`. */
    suspend fun <R> RoutingContext.respondNoDataOnScreen(block: C.() -> R) {
        onScreen(block = block)
        call.respondNoData()
    }

    /** Declared once per controller as `override val dtoResponder = dto { SomeDto(vm) }`. */
    protected abstract val dtoResponder: suspend RoutingContext.() -> Unit

    /**
     * A value rather than a type parameter: a `DTO` on this class would be erased, and [respondOnScreen] needs a
     * reified one. Reifying here captures it where the DTO type is still concrete.
     *
     * [block] takes the live controller as its receiver, so a DTO cannot be built off the rendering thread.
     */
    protected inline fun <reified D> dto(crossinline block: C.() -> D): suspend RoutingContext.() -> Unit =
        { respondOnScreen { block() } }

    protected suspend fun RoutingContext.respondDto() = dtoResponder()
}
