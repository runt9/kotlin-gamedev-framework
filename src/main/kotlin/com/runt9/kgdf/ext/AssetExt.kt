package com.runt9.kgdf.ext

import com.badlogic.gdx.graphics.Texture
import com.runt9.kgdf.asset.TextureDefinition
import ktx.assets.async.AssetStorage

fun AssetStorage.loadTexture(def: TextureDefinition): Texture = this[def.assetFile]
