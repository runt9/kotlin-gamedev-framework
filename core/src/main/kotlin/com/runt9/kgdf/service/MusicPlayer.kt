package com.runt9.kgdf.service

import com.badlogic.gdx.audio.Music
import com.runt9.kgdf.log.kgdfLogger

class MusicPlayer(private val playlist: List<Music>, private var volume: Float) {
    private val logger = kgdfLogger()
    private var currentIndex = 0
    private var currentSong: Music? = null

    fun start() {
        if (playlist.isEmpty() || currentSong != null) return
        logger.info { "Starting music player" }
        currentSong = playlist[0]
        playSong()
    }

    fun playSong() {
        logger.debug { "Playing song at volume $volume" }
        currentSong?.volume = volume
        currentSong?.play()
        currentSong?.setOnCompletionListener { song ->
            logger.debug { "Song complete" }
            song.stop()
            currentIndex = if (currentIndex == playlist.size - 1) 0 else currentIndex + 1
            currentSong = playlist[currentIndex]
            playSong()
        }
    }

    fun adjustVolume(volume: Float) {
        currentSong?.volume = volume
        this.volume = volume
    }
}
