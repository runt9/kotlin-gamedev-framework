package com.runt9.kgdf.ext

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.runt9.kgdf.settings.PlayerSettings

fun Array<Graphics.DisplayMode>.getMatching(resolution: PlayerSettings.Resolution, default: Graphics.DisplayMode) =
    getMatching(resolution.width, resolution.height, resolution.refreshRate, default)

fun Array<Graphics.DisplayMode>.getMatching(width: Int, height: Int, refreshRate: Int, default: Graphics.DisplayMode) =
    find { it.width == width && it.height == height && it.refreshRate == refreshRate } ?: default

fun getDisplayModes() = Gdx.graphics.displayModes.map { mode -> PlayerSettings.Resolution(mode.width, mode.height, mode.refreshRate) }
