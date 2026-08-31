package com.runt9.kgdf.ext

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.runt9.kgdf.asset.AssetDefinition
import ktx.assets.async.AssetStorage

fun AssetStorage.loadTexture(def: AssetDefinition): Texture = this[def.assetFile]
fun AssetStorage.loadSound(def: AssetDefinition): Sound = this[def.assetFile]
fun AssetStorage.loadMusic(def: AssetDefinition): Music = this[def.assetFile]
