package com.runt9.kgdf.settings

import com.badlogic.gdx.Application.LOG_ERROR
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import kotlinx.serialization.Serializable

@Serializable
abstract class PlayerSettings {
    abstract val fullscreen: Boolean
    abstract val vsync: Boolean
    abstract val logLevel: Int
    abstract val resolution: Resolution
    abstract val mainVolume: Float
    abstract val soundVolume: Float
    abstract val musicVolume: Float
    abstract val usageData: Boolean
    val combinedSoundVolume get() = mainVolume * soundVolume
    val combinedMusicVolume get() = mainVolume * musicVolume

    @Serializable
    data class Resolution(val width: Int, val height: Int, val refreshRate: Int) {
        override fun toString() = "${width}x${height} @ ${refreshRate}hz"
    }

    companion object {
        fun defaultPlayerSettings(): PlayerSettings {
            val primaryDisplayMode = Lwjgl3ApplicationConfiguration.getDisplayMode()
            return object : PlayerSettings() {
                override val fullscreen = false
                override val vsync = true
                override val logLevel = LOG_ERROR
                override val resolution = Resolution(primaryDisplayMode.width, primaryDisplayMode.height, primaryDisplayMode.refreshRate)
                override val mainVolume = 0.2f
                override val soundVolume = 1f
                override val musicVolume = 0.75f
                override val usageData = true
            }
        }
    }
}
