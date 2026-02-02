package com.runt9.kgdf.settings

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle
import com.runt9.kgdf.game.GameConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

abstract class SettingsStore<T : PlayerSettings>(
    gameConfig: GameConfig,
    private val serializer: KSerializer<T>,
    private val defaultSettings: T
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    private val settingsDir = gameConfig.gameDataPath
    private val settingsFile by lazy { Lwjgl3FileHandle(settingsDir.resolve(USER_SETTINGS_FILE).toFile(), Files.FileType.Absolute) }

    private lateinit var settings: T

    fun get(): T {
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
            settings = Json.decodeFromStream(serializer, settingsFile.read())
        } else {
            save(defaultSettings)
        }
    }

    fun save(settings: T) {
        settingsFile.writeString(json.encodeToString(serializer, settings), false)
        this.settings = settings
    }

}
