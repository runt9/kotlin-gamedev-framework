package com.runt9.kgdf.application

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.runt9.kgdf.ext.getMatching
import com.runt9.kgdf.settings.PlayerSettings
import com.runt9.kgdf.settings.PlayerSettingsConfig


class ApplicationConfiguration(settingsConfig: PlayerSettingsConfig) : Lwjgl3ApplicationConfiguration() {
    init {
        val settings = settingsConfig.get()
        // TODO: Configurable title
        setTitle("Autochess Slot Machine")
        handleResolution(settings.fullscreen, settings.resolution)
        useVsync(settings.vsync)
        setResizable(false)
    }

    private fun handleResolution(fullscreen: Boolean, resolution: PlayerSettings.Resolution) {
        if (fullscreen) {
            setFullscreenMode(getDisplayModes().getMatching(resolution, getDisplayMode()))
        } else {
            resolution.apply { setWindowedMode(width, height) }
        }
    }
}
