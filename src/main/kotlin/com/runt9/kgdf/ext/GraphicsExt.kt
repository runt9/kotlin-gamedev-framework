package com.runt9.kgdf.ext

import com.badlogic.gdx.Graphics
import com.runt9.kgdf.settings.PlayerSettings

fun Array<Graphics.DisplayMode>.getMatching(resolution: PlayerSettings.Resolution, default: Graphics.DisplayMode) =
    getMatching(resolution.width, resolution.height, default)

fun Array<Graphics.DisplayMode>.getMatching(width: Int, height: Int, default: Graphics.DisplayMode) =
    find { it.width == width && it.height == height } ?: default
