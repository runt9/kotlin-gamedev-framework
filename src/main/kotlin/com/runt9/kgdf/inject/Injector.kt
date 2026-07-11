package com.runt9.kgdf.inject

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.ai.GdxAI
import com.runt9.kgdf.application.ApplicationConfiguration
import com.runt9.kgdf.application.ApplicationInitializer
import com.runt9.kgdf.asset.AssetConfig
import com.runt9.kgdf.asset.AssetLoader
import com.runt9.kgdf.asset.ShaderStorage
import com.runt9.kgdf.asset.SkinLoader
import com.runt9.kgdf.asset.SoundService
import com.runt9.kgdf.async.AsyncFactory
import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.service.GameInitializer
import com.runt9.kgdf.service.GameServiceRegistry
import com.runt9.kgdf.ui.DialogManager
import ktx.inject.Context
import ktx.inject.register

object Injector {
    private val originalContext: Context = Context()
    var ctx: Context = originalContext
        private set

    private val additionalDependencies = mutableListOf<InjectorDependencies>()

    // Since Injector simply wraps Context rather than inherits from it (to increase testability), it needs to implement it
    // as if it were an interface then delegate to the given context.
    inline fun <reified Type : Any> inject(): Type = ctx.inject()
    inline fun <reified Type : Any> bindSingleton(singleton: Type) = ctx.bindSingleton(singleton)
    inline fun <reified Type : Any> bindSingleton(noinline provider: () -> Type) = ctx.bindSingleton(provider)
    inline fun <reified Type : Any> bind(noinline provider: () -> Type) = ctx.bind(provider)
    inline fun <reified Type : Any> remove() = ctx.remove<Type>()
    inline fun <reified Type : Any> newInstanceOf(): Type = ctx.newInstanceOf()
    fun <Type> getProvider(forClass: Class<Type>) = ctx.getProvider(forClass)
    fun <Type> removeProvider(ofClass: Class<Type>) = ctx.removeProvider(ofClass)
    fun clear() = ctx.clear()

    fun overrideContext(ctx: Context) {
        this.ctx = ctx
    }

    fun restoreContext() {
        this.ctx = originalContext
    }

    fun registerAdditionalDependencies(deps: InjectorDependencies) {
        additionalDependencies += deps
    }

    fun initStartupDeps() = ctx.register {
        bindSingleton<ApplicationConfiguration>()
        bindSingleton<AsyncFactory>()
        bindSingleton<EventBus>()
        bindSingleton<AssetConfig>()
        bindSingleton<SkinLoader>()
        bindSingleton<AssetLoader>()
        bindSingleton<ApplicationInitializer>()

        additionalDependencies.initStartupDeps(this)
    }

    fun initGdxDeps() = ctx.register {
        bindSingleton(Gdx.app)
        bindSingleton(Gdx.audio)
        bindSingleton(Gdx.files)
        bindSingleton(Gdx.gl)
        bindSingleton(Gdx.graphics)
        bindSingleton(Gdx.input)
        bindSingleton(Gdx.net)
    }

    fun initRunningDeps() = ctx.register {
        bindSingleton(InputMultiplexer())
        bindSingleton(GdxAI.getTimepiece())

        bindSingleton<GameServiceRegistry>()
        bindSingleton<SoundService>()
        bindSingleton<ShaderStorage>()

        additionalDependencies.initServiceDeps(this)

        bindSingleton<GameInitializer>()
        bindSingleton<DialogManager>()

        additionalDependencies.initControllerDeps(this)
        additionalDependencies.initOtherRunningDeps(this)
    }
}


