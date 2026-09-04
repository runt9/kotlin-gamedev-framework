package com.runt9.kgdf.settings

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.runt9.kgdf.log.LogLevel
import com.runt9.kgdf.log.kgdfLogger
import kotlinx.serialization.Serializable

@Serializable
abstract class PlayerSettings {
    abstract val fullscreen: Boolean
    abstract val vsync: Boolean
    abstract val minLogLevel: LogLevel
    abstract val resolution: Resolution
    abstract val uiScale: Float
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
        private val logger = kgdfLogger()

        /** Every mode the settings UI offers is at least this large, so it is a safe floor rather than a guess. */
        private val HEADLESS_RESOLUTION = Resolution(1280, 720, 60)

        /** Reads the primary display mode, falling back to [HEADLESS_RESOLUTION] where there is no display. */
        fun defaultPlayerSettings(): PlayerSettings {
            val defaultResolution = try {
                Lwjgl3ApplicationConfiguration.getDisplayMode().let { Resolution(it.width, it.height, it.refreshRate) }
            } catch (e: Exception) {
                logger.warn(e) { "No display available, defaulting resolution to $HEADLESS_RESOLUTION" }
                HEADLESS_RESOLUTION
            }

            return object : PlayerSettings() {
                override val fullscreen = false
                override val vsync = true
                override val minLogLevel = LogLevel.ERROR
                override val resolution = defaultResolution
                override val uiScale = 1f
                override val mainVolume = 0.2f
                override val soundVolume = 1f
                override val musicVolume = 0.75f
                override val usageData = true
            }
        }
    }
}
