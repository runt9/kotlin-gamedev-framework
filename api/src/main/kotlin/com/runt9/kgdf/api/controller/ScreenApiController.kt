package com.runt9.kgdf.api.controller

import com.runt9.kgdf.api.observe.ApiScreen
import com.runt9.kgdf.api.observe.ShownScreen
import com.runt9.kgdf.ui.controller.Controller
import io.ktor.http.HttpStatusCode
import kotlin.reflect.KClass

abstract class ScreenApiController<T : Controller>(protected val controllerClass: KClass<T>) : ApiController() {
    @Suppress("UNCHECKED_CAST")
    protected val controller: T get() {
        val shown = ShownScreen.shown() ?: throw ApiException("nothing is showing yet", statusCode = HttpStatusCode.Conflict)

        if (!controllerClass.isInstance(shown)) {
            throw ApiException("that is not the screen showing; currently on ${ApiScreen.current.route}", statusCode = HttpStatusCode.Conflict)
        }

        return shown as T
    }
}
