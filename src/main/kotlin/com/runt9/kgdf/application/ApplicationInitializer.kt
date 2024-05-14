package com.runt9.kgdf.application

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager
import com.runt9.kgdf.asset.AssetLoader
import com.runt9.kgdf.asset.SkinLoader
import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.settings.PlayerSettingsConfig
import ktx.async.KtxAsync


class ApplicationInitializer(
    private val eventBus: EventBus,
    private val assetLoader: AssetLoader,
    private val config: PlayerSettingsConfig,
    private val skinLoader: SkinLoader
) {
    fun initialize() {
        Gdx.app.logLevel = config.get().logLevel
        KtxAsync.initiate()

        TooltipManager.getInstance().apply {
            instant()
            animations = false
        }

        skinLoader.initializeSkin()
        eventBus.loop()
        assetLoader.load()
    }

    fun shutdown() {
        eventBus.dispose()
        assetLoader.dispose()
    }
}
