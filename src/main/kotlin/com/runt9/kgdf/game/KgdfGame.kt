package com.runt9.kgdf.game

import com.badlogic.gdx.Application
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.runt9.kgdf.application.ApplicationInitializer
import com.runt9.kgdf.event.ChangeScreenRequest
import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.event.ExitRequest
import com.runt9.kgdf.event.HandlesEvent
import com.runt9.kgdf.ext.inject
import com.runt9.kgdf.ext.lazyInject
import com.runt9.kgdf.ext.logger
import com.runt9.kgdf.inject.Injector
import com.runt9.kgdf.ui.core.UiScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.async.onRenderingThread

abstract class KgdfGame : KtxGame<KtxScreen>() {
    private val logger = logger()
    private val initializer by lazyInject<ApplicationInitializer>()
    private val input by lazyInject<Input>()
    private val eventBus by lazyInject<EventBus>()
    private val app by lazyInject<Application>()

    override fun create() {
        initializer.initialize()

        Injector.initGdxDeps()
        Injector.initRunningDeps()

        input.inputProcessor = inject<InputMultiplexer>()
        eventBus.registerHandlers(this)

        addScreens()
        setInitialScreen()
    }

    protected abstract fun addScreens()
    protected abstract fun setInitialScreen()

    override fun dispose() {
        eventBus.unregisterHandlers(this)
        super.dispose()
        initializer.shutdown()
    }

    @HandlesEvent
    suspend fun changeScreen(event: ChangeScreenRequest<*>) = onRenderingThread {
        logger.debug { "Changing screen to ${event.screenClass.simpleName}" }
        setScreen(event.screenClass.java)
    }

    @HandlesEvent(ExitRequest::class)
    fun handleExit() {
        app.exit()
    }

    protected inline fun <reified S : UiScreen> addScreen() = addScreen(inject<S>())
}
