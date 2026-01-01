package com.runt9.kgdf.settings

import com.badlogic.gdx.Application.LOG_ERROR
import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle
import com.runt9.kgdf.ext.lazyInject
import com.runt9.kgdf.game.GameConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

const val USER_SETTINGS_FILE = "settings.json"

// TODO: Backwards-compatibility capabilities for migrating settings files
class PlayerSettingsConfig(gameConfig: GameConfig) {
    private val json = Json { prettyPrint = true }
    private val settingsDir = gameConfig.gameDataPath
    private val settingsFile by lazy { Lwjgl3FileHandle(settingsDir.resolve(USER_SETTINGS_FILE).toFile(), Files.FileType.Absolute) }
    private val graphics by lazyInject<Graphics>()
    private lateinit var settings: PlayerSettings

    fun get(): PlayerSettings {
        if (!::settings.isInitialized) {
            load()
        }

        return settings
    }

    private fun load() {
        if (!settingsDir.toFile().exists()) {
            settingsDir.toFile().mkdirs()
        }

        if (settingsFile.exists()) {
            settings = Json.decodeFromStream(settingsFile.read())
        } else {
            initDefaultSettings()
        }
    }

    fun apply(settings: PlayerSettings) {
        settings.apply {
            applyResolution()
            graphics.setVSync(settings.vsync)
        }
    }

    // TODO: This is the same as ApplicationConfiguration but different due to no overlapping interface
    private fun PlayerSettings.applyResolution() {
        if (fullscreen) {
            graphics.setUndecorated(true)
            val displayMode = Gdx.graphics.displayMode
            graphics.setWindowedMode(displayMode.width, displayMode.height)
        } else {
            graphics.setUndecorated(false)
            resolution.apply { graphics.setWindowedMode(width, height) }
        }
    }

    fun save(settings: PlayerSettings) {
        settingsFile.writeString(json.encodeToString(settings), false)
        this.settings = settings
    }

    private fun initDefaultSettings() {
        val primaryDisplayMode = Lwjgl3ApplicationConfiguration.getDisplayMode()
        val settings = PlayerSettings(
            fullscreen = false,
            vsync = true,
            logLevel = LOG_ERROR,
            resolution = PlayerSettings.Resolution(primaryDisplayMode.width, primaryDisplayMode.height, primaryDisplayMode.refreshRate),
            mainVolume = 0.2f
        )
        save(settings)
        this.settings = settings
    }
}
