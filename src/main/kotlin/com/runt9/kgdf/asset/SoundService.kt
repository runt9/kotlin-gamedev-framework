package com.runt9.kgdf.asset

import com.runt9.kgdf.ext.loadMusic
import com.runt9.kgdf.ext.loadSound
import com.runt9.kgdf.service.MusicPlayer
import com.runt9.kgdf.settings.PlayerSettingsConfig
import ktx.assets.async.AssetStorage

class SoundService(private val assets: AssetStorage, private val settings: PlayerSettingsConfig) {
    private var currentMusicPlayer: MusicPlayer? = null

    fun playSoundEffect(soundDef: AssetDefinition, pitch: Float = 1f, stop: Boolean = false) {
        val sound = assets.loadSound(soundDef)
        val volume = settings.get().combinedSoundVolume
        if (stop) sound.stop()
        sound.play(volume, pitch, 0f)
    }

    fun playMusic(soundDefs: List<AssetDefinition>) {
        val playlist = soundDefs.map { assets.loadMusic(it) }
        val initialVolume = settings.get().combinedMusicVolume
        currentMusicPlayer = MusicPlayer(playlist, initialVolume)
        currentMusicPlayer?.start()
    }

    fun adjustMusicVolume(volume: Float) = currentMusicPlayer?.adjustVolume(volume)
}
