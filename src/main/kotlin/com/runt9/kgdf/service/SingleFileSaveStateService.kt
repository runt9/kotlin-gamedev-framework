@file:OptIn(InternalSerializationApi::class)

package com.runt9.kgdf.service

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle
import com.runt9.kgdf.ext.kgdfLogger
import com.runt9.kgdf.game.GameConfig
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

const val SAVES_DIR = "saves"
const val SAVED_RUN_FILE = "savedRun"

// TODO: Take manifest style from AutoHack and make that another version of this
abstract class SingleFileSaveStateService<T : Any>(
    private val stateType: KClass<T>,
    gameConfig: GameConfig,
    // Default to saved run for roguelikes
    private val filename: String = SAVED_RUN_FILE
) {
    private val logger = kgdfLogger()
    // TODO: Protobuf isn't a "safe" format so people could easily modify their saves.
    private val protobuf = ProtoBuf {}
    private val json = Json {
        prettyPrint = true
        allowStructuredMapKeys = true
    }
    private val savesDir by lazy { gameConfig.gameDataPath.resolve(SAVES_DIR) }

    private fun stateFileHandle() = Lwjgl3FileHandle(savesDir.resolve(filename).toFile(), Files.FileType.Absolute)

    fun saveState(saveState: T) {
        logger.info { "Saving save state" }
        val saveFile = stateFileHandle()
//        saveFile.writeBytes(protobuf.encodeToByteArray(stateType.serializer(), saveState), false)
        saveFile.writeString(json.encodeToString(stateType.serializer(), saveState), false)
    }

    fun loadState(): T {
        logger.info { "Loading save state" }
        val saveFile = stateFileHandle()
//        return protobuf.decodeFromByteArray(stateType.serializer(), saveFile.readBytes())
        return json.decodeFromStream(stateType.serializer(), saveFile.read())
    }

    fun hasSavedFile(): Boolean {
        val fileHandle = stateFileHandle()
        return fileHandle.exists() && fileHandle.readString().isNotEmpty()
    }
    fun clearSavedFile(): Boolean {
        logger.info { "Clearing saved file" }
        // TODO: Hacky workaround for file not getting deleted sometimes
        stateFileHandle().writeString("", false)
        return stateFileHandle().delete()
    }
}
