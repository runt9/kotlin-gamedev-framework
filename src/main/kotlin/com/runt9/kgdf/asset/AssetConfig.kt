package com.runt9.kgdf.asset

import com.badlogic.gdx.utils.Disposable
import com.runt9.kgdf.async.AsyncFactory
import com.runt9.kgdf.inject.Injector
import ktx.assets.async.AssetStorage
import ktx.freetype.async.registerFreeTypeFontLoaders

class AssetConfig(asyncFactory: AsyncFactory) : Disposable {
    // Through AsyncFactory, not newSingleThreadAsyncContext: that seam is the only thing a test can substitute,
    // and a context built directly here is one no scheduler can drain.
    val asyncContext = asyncFactory.newAsyncContext("Assets-Thread")
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
        // Cast, not a call: AsyncFactory returns a plain CoroutineDispatcher, and only the real one owns a thread.
        (asyncContext as? Disposable)?.dispose()
    }
}
