package com.runt9.kgdf.settings

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.runt9.kgdf.asset.SoundService
import com.runt9.kgdf.ext.lazyInject
import com.runt9.kgdf.intercept.BaseInterceptorHook
import com.runt9.kgdf.intercept.InterceptableAdapter
import com.runt9.kgdf.intercept.UiScaleChangeContext
import com.runt9.kgdf.log.kgdfLogger

const val USER_SETTINGS_FILE = "settings.json"

// TODO: Backwards-compatibility capabilities for migrating settings files
class PlayerSettingsConfig(private val store: SettingsStore<*>) : InterceptableAdapter() {
    private val logger = kgdfLogger()
    private val graphics by lazyInject<Graphics>()
    private val soundService by lazyInject<SoundService>()

    fun get() = store.get()

    fun apply(settings: PlayerSettings) {
        settings.apply {
            applyResolution()
            graphics.setVSync(settings.vsync)
            soundService.adjustMusicVolume(combinedMusicVolume)

            UiScaleChangeContext(uiScale).apply {
                addInterceptors(this@PlayerSettingsConfig)
                intercept(BaseInterceptorHook.ON_UI_SCALE_CHANGE)
            }
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
}
