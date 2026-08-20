package com.runt9.kgdf.asset

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.kotcrab.vis.ui.VisUI
import com.runt9.kgdf.inject.Injector
import com.runt9.kgdf.log.kgdfLogger
import ktx.assets.async.AssetStorage
import ktx.scene2d.Scene2DSkin
import ktx.style.getAll
import ktx.style.set
import kotlin.math.floor
import kotlin.math.roundToInt

class SkinLoader(private val assetStorage: AssetStorage) {
    private val logger = kgdfLogger()
    private lateinit var skin: ParameterAvailableFreeTypeSkin

    // TODO: Configurable skin

    fun initializeSkin() {
        logger.info { "Initializing skin" }
        assetStorage.setLoader<Skin> { ParameterAvailableFreeTypeSkinLoader(assetStorage.fileResolver) }
        skin = assetStorage.loadSync(AssetDescriptor("skin/uiskin.json", Skin::class.java)) as ParameterAvailableFreeTypeSkin
        logger.info { "Skin loading complete, loading VisUI" }
        Injector.bindSingleton { skin }
        VisUI.load(skin)
        Scene2DSkin.defaultSkin = VisUI.getSkin()

        logger.info { "Skin initialization complete" }
    }

    fun regenerateFonts(stage: Stage) {
        // Cover minimize
        if (Gdx.graphics.width == 0) return
        val fontGen = assetStorage.loadSync<FreeTypeFontGenerator>("skin/Roboto-Medium.ttf")
        val fontScale = stage.viewport.worldWidth / Gdx.graphics.width.toFloat()
        logger.info { "Regenerating fonts with font scale $fontScale" }

        // TODO: Refactor and optimize this
        skin.getAll<BitmapFont>()?.forEach { entry ->
            val name = entry.key
            val fontParams = skin.fontParams[name] ?: return@forEach
            val font = entry.value
            val oldFontSize = fontParams.size
            val currentFontSize = font.data.name.replace("Roboto-Medium-", "").toInt()
            // Floor is important: we don't want to round up because then a glyph could be too big and get cut off.
            val newFontSize = floor(oldFontSize / fontScale).roundToInt()
            logger.debug { "Regenerating font $name: [ oldSize: $oldFontSize, currentSize: $currentFontSize, newSize: $newFontSize ]" }
            if (currentFontSize == newFontSize) return@forEach

            val newFontParams = fontParams.copy().apply {
                size = newFontSize
                genMipMaps = true
                hinting = FreeTypeFontGenerator.Hinting.None
                minFilter = Texture.TextureFilter.MipMapLinearLinear
                magFilter = Texture.TextureFilter.MipMapLinearLinear
            }
            val newFont = fontGen.generateFont(newFontParams)
            newFont.data.setScale(fontScale)
            newFont.setUseIntegerPositions(false)
            skin[name] = newFont

            // TODO: It'd be nice to refactor this but they don't share an interface so that makes the abstraction difficult
            skin.getAll<TextButton.TextButtonStyle>()?.forEach { entry -> if (entry.value.font == font) entry.value.font = newFont }
            skin.getAll<Label.LabelStyle>()?.forEach { entry -> if (entry.value.font == font) entry.value.font = newFont }
            skin.getAll<List.ListStyle>()?.forEach { entry -> if (entry.value.font == font) entry.value.font = newFont }
            skin.getAll<SelectBox.SelectBoxStyle>()?.forEach { entry -> if (entry.value.font == font) entry.value.font = newFont }
            skin.getAll<Window.WindowStyle>()?.forEach { entry -> if (entry.value.titleFont == font) entry.value.titleFont = newFont }
            skin.getAll<CheckBox.CheckBoxStyle>()?.forEach { entry -> if (entry.value.font == font) entry.value.font = newFont }

            stage.actors.filterIsInstance<Group>().forEach { actor -> updateChildren(actor) }
        }
    }

    fun updateChildren(actor: Group) {
        actor.children.forEach { child ->
            when (child) {
                is TextButton -> child.style = child.style
                is Label -> child.style = child.style
                is List<*> -> child.style = child.style
                is SelectBox<*> -> child.style = child.style
                is Window -> child.style = child.style
                is Group -> updateChildren(child)
            }
        }
    }

    private fun FreeTypeFontGenerator.FreeTypeFontParameter.copy() = FreeTypeFontGenerator.FreeTypeFontParameter().also { newParam ->
        newParam.size = size
        newParam.mono = mono
        newParam.hinting = hinting
        newParam.color = color
        newParam.gamma = gamma
        newParam.renderCount = renderCount
        newParam.borderWidth = borderWidth
        newParam.borderColor = borderColor
        newParam.borderStraight = borderStraight
        newParam.borderGamma = borderGamma
        newParam.shadowOffsetX = shadowOffsetX
        newParam.shadowOffsetY = shadowOffsetY
        newParam.shadowColor = shadowColor
        newParam.spaceX = spaceX
        newParam.spaceY = spaceY
        newParam.padTop = padTop
        newParam.padBottom = padBottom
        newParam.padLeft = padLeft
        newParam.padRight = padRight
        newParam.characters = characters
        newParam.kerning = kerning
        newParam.packer = packer
        newParam.flip = flip
        newParam.genMipMaps = genMipMaps
        newParam.minFilter = minFilter
        newParam.magFilter = magFilter
        newParam.incremental = incremental
    }
}
