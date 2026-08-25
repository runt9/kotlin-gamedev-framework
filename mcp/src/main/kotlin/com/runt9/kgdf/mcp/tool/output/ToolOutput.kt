package com.runt9.kgdf.mcp.tool.output

import io.ktor.server.application.ApplicationCall
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.full.createType

/**
 * What a tool hands back, in the two forms a client can receive. Keeping the serializer with the value is what
 * lets the server render any tool without knowing its type.
 */
sealed interface ToolOutput {
    fun toCallResult(): CallToolResult
    suspend fun respondToCall(call: ApplicationCall)

    companion object {
        inline fun <reified T> of(value: T) = Structured(value, serializer<T>())

        /**
         * For a value whose static type is gone by the time it reaches here, which is what happens to anything
         * pulled out of a generic collection. Resolves the serializer from the runtime class instead.
         *
         * **Throws if that class is not `@Serializable`**, and at call time rather than compile time — so a
         * caller registering these should have a test that runs each one.
         */
        @Suppress("UNCHECKED_CAST")
        fun ofRuntimeType(value: Any) = Structured(value, serializer(value::class.createType()) as KSerializer<Any>)
    }
}
