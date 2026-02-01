package com.runt9.kgdf.service

import com.badlogic.gdx.audio.Music

class MusicPlayer(private val playlist: List<Music>, private var volume: Float) {
    private var currentIndex = 0
    private var currentSong: Music? = null

    fun start() {
        if (playlist.isEmpty()) return
        currentSong = playlist[0]
        playSong()
    }

    fun playSong() {
        currentSong?.volume = volume
        currentSong?.play()
        currentSong?.setOnCompletionListener { song ->
            song.stop()
            currentIndex = if (currentIndex == playlist.size - 1) 0 else currentIndex + 1
            currentSong = playlist[currentIndex]
            playSong()
        }
    }

    fun adjustVolume(volume: Float) {
        currentSong?.volume = volume
    }
}
