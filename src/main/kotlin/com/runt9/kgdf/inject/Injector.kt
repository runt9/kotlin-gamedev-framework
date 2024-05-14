package com.runt9.kgdf.inject

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.runt9.kgdf.application.ApplicationConfiguration
import com.runt9.kgdf.application.ApplicationInitializer
import com.runt9.kgdf.asset.AssetConfig
import com.runt9.kgdf.asset.AssetLoader
import com.runt9.kgdf.asset.SkinLoader
import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.settings.PlayerSettingsConfig
import ktx.inject.Context
import ktx.inject.register

object Injector : Context() {
    private val additionalDependencies = mutableListOf<InjectorDependencies>()

    fun registerAdditionalDependencies(deps: InjectorDependencies) {
        additionalDependencies += deps
    }

    fun initStartupDeps() = register {
        bindSingleton<PlayerSettingsConfig>()
        bindSingleton<ApplicationConfiguration>()
        bindSingleton<EventBus>()
        bindSingleton<AssetConfig>()
        bindSingleton<SkinLoader>()
        bindSingleton<AssetLoader>()
        bindSingleton<ApplicationInitializer>()


        additionalDependencies.forEach { it.initStartupDeps(this) }
    }

    fun initGdxDeps() = register {
        bindSingleton(Gdx.app)
        bindSingleton(Gdx.audio)
        bindSingleton(Gdx.files)
        bindSingleton(Gdx.gl)
        bindSingleton(Gdx.graphics)
        bindSingleton(Gdx.input)
        bindSingleton(Gdx.net)
    }

    fun initRunningDeps() = register {
        bindSingleton(InputMultiplexer())

        additionalDependencies.forEach { it.initRunningDeps(this) }
    }

}
