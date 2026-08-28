package com.runt9.kgdf.api.result

import com.runt9.kgdf.api.observe.ApiScreen
import com.runt9.kgdf.api.observe.ShownScreen
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

/**
 * Every JSON response.
 *
 * [currentScreen] rides on all of them rather than sitting behind an endpoint of its own because an action can
 * change which screen is up and the caller cannot predict which: ending a round may open a level-up dialog, a
 * round-end dialog, or the shop.
 */
@Serializable
class ApiResult<T>(val currentScreen: ApiScreen, val data: T)

/** For an endpoint whose entire answer is [ApiResult.currentScreen]. Serializes as `{}`. */
@Serializable
object NoData

suspend inline fun <reified T> ApplicationCall.respondApi(data: T) = respond(ApiResult(ApiScreen.current, data))
