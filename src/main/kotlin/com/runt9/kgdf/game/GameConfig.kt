package com.runt9.kgdf.game

import com.runt9.kgdf.util.getDefaultPreferencesDirectory
import java.nio.file.Path
import java.nio.file.Paths

data class GameConfig(val title: String, val company: String = "Runt9 Productions") {
    val gameDataPath: Path = Paths.get(getDefaultPreferencesDirectory(), company, title)
}
