package com.runt9.kgdf.ui.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2

class Dynamic2dCameraController(private val camera: OrthographicCamera) : GameCameraController() {
    // TODO: Make these configurable
    var forwardKeys = listOf(Input.Keys.W, Input.Keys.UP)
    var backwardKeys = listOf(Input.Keys.S, Input.Keys.DOWN)
    var rightKeys = listOf(Input.Keys.D, Input.Keys.RIGHT)
    var leftKeys = listOf(Input.Keys.A, Input.Keys.LEFT)
    var translateButton = Input.Buttons.MIDDLE

    var forwardPressed = false
    var backwardPressed = false
    var rightPressed = false
    var leftPressed = false
    var touched = false
    var button = -1
    var startX = 0f
    var startY = 0f

    var translateUnits = 8f
    var scrollFactor = 0.1f

    override fun update(delta: Float) {
        if (leftPressed || rightPressed || forwardPressed || backwardPressed) {
            if (forwardPressed) camera.translate(Vector2(0f, 1f).scl(delta * translateUnits * camera.zoom))
            if (backwardPressed) camera.translate(Vector2(0f, -1f).scl(delta * translateUnits * camera.zoom))
            if (leftPressed) camera.translate(Vector2(-1f, 0f).scl(delta * translateUnits * camera.zoom))
            if (rightPressed) camera.translate(Vector2(1f, 0f).scl(delta * translateUnits * camera.zoom))
            camera.update()
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        touched = true
        if (this.button < 0) {
            startX = screenX.toFloat()
            startY = screenY.toFloat()
            this.button = button
        }
        return super.touchDown(screenX, screenY, pointer, button)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        touched = false
        if (this.button == button) this.button = -1
        return super.touchUp(screenX, screenY, pointer, button)
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val result = super.touchDragged(screenX, screenY, pointer)
        if (result || button < 0) return result
        val deltaX = (screenX - startX) / Gdx.graphics.width
        val deltaY = (startY - screenY) / Gdx.graphics.height
        startX = screenX.toFloat()
        startY = screenY.toFloat()
        return process(deltaX, deltaY, button)
    }

    private fun process(deltaX: Float, deltaY: Float, button: Int): Boolean {
        if (button == translateButton) {
            camera.translate(Vector2(-deltaX, -deltaY).scl(translateUnits * camera.zoom))
            camera.update()
        }
        return true
    }


    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        return zoom(amountY * scrollFactor)
    }

    fun zoom(amount: Float): Boolean {
        if ((camera.zoom <= 0.5f && amount < 0) || camera.zoom >= 3f && amount > 0) return false

        camera.zoom += amount
        camera.update()
        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        when {
            forwardKeys.contains(keycode) -> forwardPressed = true
            backwardKeys.contains(keycode) -> backwardPressed = true
            leftKeys.contains(keycode) -> leftPressed = true
            rightKeys.contains(keycode) -> rightPressed = true
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        when {
            forwardKeys.contains(keycode) -> forwardPressed = false
            backwardKeys.contains(keycode) -> backwardPressed = false
            leftKeys.contains(keycode) -> leftPressed = false
            rightKeys.contains(keycode) -> rightPressed = false
        }
        return false
    }
}
