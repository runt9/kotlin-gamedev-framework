package com.runt9.kgdf.api.controller

import com.runt9.kgdf.api.observe.ApiScreen
import com.runt9.kgdf.api.observe.ShownScreen
import com.runt9.kgdf.ui.controller.Controller
import io.ktor.http.HttpStatusCode
import kotlin.reflect.KClass

abstract class ScreenApiController<T : Controller>(protected val controllerClass: KClass<T>) : ApiController() {
    /**
     * Resolves the live controller and runs [block] against it, both on the rendering thread.
     *
     * Resolution is inside the hop rather than before it so the screen cannot change between the check and the
     * action. Endpoints should reach for this over [controller], which does neither.
     */
    protected suspend fun <R> onScreen(block: T.() -> R): R = onRender { controller.block() }

    @Suppress("UNCHECKED_CAST")
    protected val controller: T get() {
        val shown = ShownScreen.shown() ?: throw ApiException("nothing is showing yet", statusCode = HttpStatusCode.Conflict)

        if (!controllerClass.isInstance(shown)) {
            throw ApiException("that is not the screen showing; currently on ${ApiScreen.current.route}", statusCode = HttpStatusCode.Conflict)
        }

        return shown as T
    }
}
