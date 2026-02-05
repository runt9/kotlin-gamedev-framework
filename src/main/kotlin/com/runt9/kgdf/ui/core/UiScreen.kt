package com.runt9.kgdf.ui.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.runt9.kgdf.asset.SkinLoader
import com.runt9.kgdf.ext.lazyInject
import com.runt9.kgdf.intercept.BaseInterceptorHook
import com.runt9.kgdf.intercept.UiScaleChangeContext
import com.runt9.kgdf.intercept.addInterceptor
import com.runt9.kgdf.intercept.intercept
import com.runt9.kgdf.intercept.removeInterceptor
import com.runt9.kgdf.log.kgdfLogger
import com.runt9.kgdf.settings.PlayerSettingsConfig
import com.runt9.kgdf.ui.DialogManager
import com.runt9.kgdf.ui.controller.Controller

/**
 * Used for UI displays such as menus or part of a GameScreen to display UI elements on the screen while
 * allowing the game to control its own grid.
 */
abstract class UiScreen(private val width: Float, private val height: Float) : BaseScreen {
    private val logger = kgdfLogger()
    val uiStage = BasicStage(FitViewport(width, height))
    val input by lazyInject<InputMultiplexer>()
    val dialogManager by lazyInject<DialogManager>()
    private val skinLoader by lazyInject<SkinLoader>()
    private val playerSettingsConfig by lazyInject<PlayerSettingsConfig>()
    override val stages = listOf(uiStage)
    abstract val uiController: Controller
    private val uiChangeInterceptor = intercept<UiScaleChangeContext>(BaseInterceptorHook.ON_UI_SCALE_CHANGE) { ctx ->
        applyUiScale(ctx.uiScale, true)
    }

    init {
        playerSettingsConfig.get().apply { applyUiScale(uiScale) }
    }

    override fun show() {
        playerSettingsConfig.addInterceptor(uiChangeInterceptor)
        input.addProcessor(uiStage)
        uiController.load()
        uiStage.setView(uiController.view)
        dialogManager.currentStage = uiStage
    }

    override fun hide() {
        playerSettingsConfig.removeInterceptor(uiChangeInterceptor)
        uiStage.clear()
        input.removeProcessor(uiStage)
        uiController.dispose()
        dialogManager.currentStage = null
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        uiStage.viewport.update(width, height)
        skinLoader.regenerateFonts(uiStage)
    }

    // TODO: Maybe can just push this all to BasicStage
    fun applyUiScale(uiScale: Float, atRuntime: Boolean = false) {
        // Divide by uiScale because we want 50% UI scale to decrease the size of UI elements, which means we need to make the Viewport have a larger world size
        val newWidth = width / uiScale
        val newHeight = height / uiScale
        val activeDialogs = uiStage.activeDialogs.toList()
        if (atRuntime) {
            uiStage.clear()
            activeDialogs.forEach { it.hide() }
        }
        uiStage.viewport.setWorldSize(newWidth, newHeight)
        uiStage.viewport.update(Gdx.graphics.width, Gdx.graphics.height, true)
        if (atRuntime) {
            show()
            logger.info { "At runtime, reshowing all active dialogs $activeDialogs" }
            activeDialogs.forEach { it.show(uiStage, skipAnimation = true) }
        }
        skinLoader.regenerateFonts(uiStage)
    }
}
