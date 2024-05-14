package com.runt9.kgdf.asset

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.runt9.kgdf.event.AssetsLoadedEvent
import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.ext.logger
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.log.logger

class AssetLoader(
    private val assets: AssetStorage,
    private val eventBus: EventBus,
    private val assetConfig: AssetConfig
) : Disposable {
    private val logger = logger()

    fun load() = KtxAsync.launch(assetConfig.asyncContext) {
        logger.info { "Loading assets" }
        // TODO: Need a way for the application to "register" the definition entries
        val assetsToLoad = TextureDefinition.entries.map { assets.loadAsync<Texture>(it.assetFile) }
        assetsToLoad.joinAll()
        logger.info { "Asset loading complete" }
        eventBus.enqueueEvent(AssetsLoadedEvent())
    }

    override fun dispose() {
        logger.info { "Disposing" }
        assetConfig.dispose()
    }
}
