package com.runt9.kgdf.asset

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.kotcrab.vis.ui.VisUI
import com.runt9.kgdf.ext.kgdfLogger
import com.runt9.kgdf.inject.Injector
import ktx.assets.async.AssetStorage
import ktx.collections.gdxMapOf
import ktx.freetype.generateFont
import ktx.scene2d.Scene2DSkin

class SkinLoader(private val assetStorage: AssetStorage) {
    private val logger = kgdfLogger()

    // TODO: Configurable skin

    fun initializeSkin() {
        logger.info { "Initializing skin" }
        val fontGen = assetStorage.loadSync<FreeTypeFontGenerator>("skin/Roboto-Medium.ttf")
        val fontMap = gdxMapOf<String, Any>(
            "font-button" to fontGen.buttonFont(),
            "font-label" to fontGen.labelFont(),
            "font-title" to fontGen.titleFont(),
        )
        val skin = assetStorage.loadSync(AssetDescriptor("skin/uiskin.json", Skin::class.java, SkinLoader.SkinParameter(fontMap)))

        logger.info { "Skin loading complete, loading VisUI" }
        Injector.bindSingleton { skin }
        VisUI.load(skin)
        Scene2DSkin.defaultSkin = VisUI.getSkin()

        logger.info { "Skin initialization complete" }
    }

    private fun FreeTypeFontGenerator.buttonFont() = generateFont {
        size = 16
        spaceX = 2
        spaceY = 2
        padLeft = 5
        padRight = 5
    }

    private fun FreeTypeFontGenerator.labelFont(): BitmapFont {
        val font = generateFont {
            size = 16
            spaceX = 2
            spaceY = 2
            padLeft = 5
            padRight = 5
        }
        font.data.markupEnabled = true
        return font
    }

    private fun FreeTypeFontGenerator.titleFont() = generateFont {
        size = 32
        shadowOffsetX = 3
        shadowOffsetY = 2
        spaceX = 2
        spaceY = 2
    }
}
