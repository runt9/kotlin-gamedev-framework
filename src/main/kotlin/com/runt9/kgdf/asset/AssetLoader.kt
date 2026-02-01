package com.runt9.kgdf.asset

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.runt9.kgdf.event.AssetsLoadedEvent
import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.log.kgdfLogger
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync

class AssetLoader(
    private val assets: AssetStorage,
    private val eventBus: EventBus,
    private val assetConfig: AssetConfig
) : Disposable {
    private val logger = kgdfLogger()

    fun load() = KtxAsync.launch(assetConfig.asyncContext) {
        logger.info { "Loading assets" }
        val assetsToLoad = AssetRegistry.assets.map {
            when (it.type) {
                AssetDefinition.AssetType.TEXTURE -> assets.loadAsync<Texture>(it.assetFile)
                AssetDefinition.AssetType.SOUND -> assets.loadAsync<Sound>(it.assetFile)
                AssetDefinition.AssetType.MUSIC -> assets.loadAsync<Music>(it.assetFile)
            }
        }
        assetsToLoad.joinAll()
        logger.info { "Asset loading complete" }
        eventBus.enqueueEvent(AssetsLoadedEvent())
    }

    override fun dispose() {
        logger.info { "Disposing" }
        assetConfig.dispose()
    }
}
