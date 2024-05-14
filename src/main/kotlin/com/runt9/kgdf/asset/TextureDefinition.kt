package com.runt9.kgdf.asset

interface TextureDefinition {
    val textureFile: String
    val assetFile: String
        get() = "texture/$textureFile"
}
