package com.runt9.kgdf.asset

interface AssetDefinition {
    enum class AssetType { TEXTURE, SOUND }

    val filename: String
    val path: String
    val type: AssetType
    val assetFile: String
        get() = "${path}/$filename"
}
