package com.runt9.kgdf.asset

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.kotcrab.vis.ui.VisUI
import com.ray3k.stripe.FreeTypeSkinLoader
import com.runt9.kgdf.ext.kgdfLogger
import com.runt9.kgdf.inject.Injector
import ktx.assets.async.AssetStorage
import ktx.scene2d.Scene2DSkin

class SkinLoader(private val assetStorage: AssetStorage) {
    private val logger = kgdfLogger()

    // TODO: Configurable skin

    fun initializeSkin() {
        logger.info { "Initializing skin" }
        assetStorage.setLoader<Skin> { FreeTypeSkinLoader(assetStorage.fileResolver) }
        val skin = assetStorage.loadSync(AssetDescriptor("skin/uiskin.json", Skin::class.java))

        logger.info { "Skin loading complete, loading VisUI" }
        Injector.bindSingleton { skin }
        VisUI.load(skin)
        Scene2DSkin.defaultSkin = VisUI.getSkin()

        logger.info { "Skin initialization complete" }
    }
}
