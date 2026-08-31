package com.runt9.kgdf.api.observe

import com.runt9.kgdf.ui.controller.Controller
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

@Serializable(with = ApiScreenSerializer::class)
interface ApiScreen {
    val route: String
    val controller: KClass<out Controller>?

    companion object {
        internal val screens = mutableListOf<ApiScreen>()
        val current get() = of(ShownScreen.shown())

        fun of(shown: Controller?): ApiScreen {
            if (shown == null) return DefaultApiScreen.NONE

            return screens.find { it.controller?.isInstance(shown) == true } ?: DefaultApiScreen.UNKNOWN
        }

        /**
         * The screen [controllerClass] serves. Matches the declared class exactly, where [of] matches a showing
         * instance by [KClass.isInstance].
         *
         * @throws IllegalStateException when no registered screen names it. Answering [DefaultApiScreen.UNKNOWN]
         * here, as [of] does, would bind that controller's endpoints under `/unknown` and 404 them all.
         */
        fun forController(controllerClass: KClass<out Controller>): ApiScreen =
            screens.find { it.controller == controllerClass }
                ?: error("No ApiScreen names ${controllerClass.simpleName}. Register screens before controllers.")

        /**
         * Idempotent by identity, for the same reason as [com.runt9.kgdf.api.controller.ApiControllerRegistry]:
         * the list is process-global with no teardown, so a test JVM registering per spec must not accumulate.
         */
        fun registerScreens(screens: Iterable<ApiScreen>) {
            screens.forEach { screen -> if (Companion.screens.none { it === screen }) Companion.screens += screen }
        }
    }
}

enum class DefaultApiScreen(override val route: String, override val controller: KClass<out Controller>?) : ApiScreen {
    /** Nothing is showing at all, which happens while the game is still starting up. */
    NONE("none", null),

    /** Something is showing that nobody has listed here. Visible in a response, unlike a name silently guessed. */
    UNKNOWN("unknown", null);
}

/**
 * Serializes by [ApiScreen.route] rather than the entry name, so a screen's wire name is written in exactly one
 * place. kotlinx has no `@JsonValue` equivalent (Kotlin/kotlinx.serialization#31, open since 2017); the only
 * alternative is a `@SerialName` on every entry restating its own route.
 */
object ApiScreenSerializer : KSerializer<ApiScreen> {
    override val descriptor = PrimitiveSerialDescriptor("ApiScreen", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ApiScreen) = encoder.encodeString(value.route)

    override fun deserialize(decoder: Decoder): ApiScreen {
        val route = decoder.decodeString()
        return ApiScreen.screens.find { it.route == route } ?: DefaultApiScreen.UNKNOWN
    }
}
