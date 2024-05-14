package com.runt9.kgdf.asset

import com.badlogic.gdx.utils.Disposable
import com.runt9.kgdf.inject.Injector
import ktx.assets.async.AssetStorage
import ktx.async.newSingleThreadAsyncContext
import ktx.freetype.async.registerFreeTypeFontLoaders

class AssetConfig : Disposable {
    val asyncContext = newSingleThreadAsyncContext("Assets-Thread")
    private val assetStorage: AssetStorage

    init {
        assetStorage = configureAssetStorage()
        Injector.bindSingleton(assetStorage)
    }

    private fun configureAssetStorage() = AssetStorage(asyncContext = asyncContext).apply {
        registerFreeTypeFontLoaders(replaceDefaultBitmapFontLoader = true)
    }

    override fun dispose() {
        assetStorage.dispose()
        asyncContext.dispose()
    }
}
