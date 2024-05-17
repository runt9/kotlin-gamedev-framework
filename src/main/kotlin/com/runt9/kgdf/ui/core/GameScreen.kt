package com.runt9.kgdf.ui.core

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.runt9.kgdf.inject.Injector
import com.runt9.kgdf.ui.controller.Controller

/**
 * Used to combine both a UI screen and a Game screen together so that UI elements can be displayed in proper resolution but the game
 * can be displayed in whatever grid size is best. If a GameCameraController is supplied, will connect that in to receive updates on render.
 */
abstract class GameScreen(worldWidth: Float, worldHeight: Float) : UiScreen() {
    private val camera = OrthographicCamera(worldWidth, worldHeight)
    protected val gameStage: BasicStage = BasicStage(FitViewport(worldWidth, worldHeight, camera))
    override val stages = listOf(gameStage, uiStage)
    abstract val gameController: Controller

    override fun show() {
        Injector.bindSingleton(camera)
        getCameraController()?.also(input::addProcessor)
        input.addProcessor(gameStage)
        gameController.load()
        gameStage.setView(gameController.view)
        super.show()
    }

    override fun hide() {
        super.hide()
        input.removeProcessor(gameStage)
        getCameraController()?.also(input::removeProcessor)
        Injector.remove<OrthographicCamera>()
        gameController.dispose()
    }

    override fun render(delta: Float) {
        getCameraController()?.also { it.update(delta) }
        super.render(delta)
    }

    // Default to null as no camera is required to be supplied. But can be overridden if necessary.
    fun getCameraController(): GameCameraController? = null
}
