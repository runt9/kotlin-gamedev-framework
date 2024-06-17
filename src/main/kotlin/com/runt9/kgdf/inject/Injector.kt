package com.runt9.kgdf.inject

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.ai.GdxAI
import com.runt9.kgdf.application.ApplicationConfiguration
import com.runt9.kgdf.application.ApplicationInitializer
import com.runt9.kgdf.asset.AssetConfig
import com.runt9.kgdf.asset.AssetLoader
import com.runt9.kgdf.asset.SkinLoader
import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.service.GameInitializer
import com.runt9.kgdf.service.GameServiceRegistry
import com.runt9.kgdf.service.GameStateService
import com.runt9.kgdf.service.RandomizerService
import com.runt9.kgdf.settings.PlayerSettingsConfig
import com.runt9.kgdf.ui.DialogManager
import ktx.inject.Context
import ktx.inject.register

// TODO: Might be a good idea, instead of injecting everything, to instead have the list of dependencies that can be injected and removed
//  from the context, making it easier to fully inject then fully tear-down everything in the Game state
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

        additionalDependencies.initStartupDeps(this)
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
        bindSingleton(GdxAI.getTimepiece())

        bindSingleton<GameServiceRegistry>()
        bindSingleton<GameStateService>()
        bindSingleton<RandomizerService>()

        additionalDependencies.initServiceDeps(this)

        bindSingleton<GameInitializer>()

        bindSingleton<DialogManager>()

        additionalDependencies.initControllerDeps(this)
        additionalDependencies.initOtherRunningDeps(this)
    }
}


