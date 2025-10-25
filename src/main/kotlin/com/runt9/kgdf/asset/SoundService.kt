package com.runt9.kgdf.asset

import com.runt9.kgdf.ext.loadSound
import com.runt9.kgdf.settings.PlayerSettingsConfig
import ktx.assets.async.AssetStorage

class SoundService(private val assets: AssetStorage, private val settings: PlayerSettingsConfig) {
    fun playSoundEffect(soundDef: AssetDefinition, pitch: Float = 1f, stop: Boolean = false) {
        val sound = assets.loadSound(soundDef)
        val volume = settings.get().mainVolume
        if (stop) sound.stop()
        sound.play(volume, pitch, 0f)
    }
}
