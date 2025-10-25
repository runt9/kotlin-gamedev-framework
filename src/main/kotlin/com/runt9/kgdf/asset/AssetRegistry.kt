package com.runt9.kgdf.asset

object AssetRegistry {
    val assets = mutableListOf<AssetDefinition>()

    fun registerAsset(assetDefinition: AssetDefinition) {
        assets += assetDefinition
    }

    fun registerAssets(assets: Collection<AssetDefinition>) = assets.forEach { registerAsset(it) }
}
