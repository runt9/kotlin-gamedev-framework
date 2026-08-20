package com.runt9.kgdf.serialization

import com.badlogic.gdx.math.Vector2
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object Vector2Serializer : KSerializer<Vector2> {
    private val delegateSerializer = PairSerializer(Float.serializer(), Float.serializer())
    override val descriptor = SerialDescriptor("Vector2", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: Vector2) {
        val data = value.x to value.y
        encoder.encodeSerializableValue(delegateSerializer, data)
    }

    override fun deserialize(decoder: Decoder): Vector2 {
        val data = decoder.decodeSerializableValue(delegateSerializer)
        return Vector2(data.first, data.second)
    }
}
