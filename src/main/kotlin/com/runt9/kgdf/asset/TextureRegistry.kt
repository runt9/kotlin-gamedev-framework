package com.runt9.kgdf.asset

object TextureRegistry {
    val textures = mutableListOf<TextureDefinition>()

    fun registerTexture(textureDefinition: TextureDefinition) {
        textures += textureDefinition
    }

    fun registerTextures(textures: Collection<TextureDefinition>) = textures.forEach { registerTexture(it) }
}
