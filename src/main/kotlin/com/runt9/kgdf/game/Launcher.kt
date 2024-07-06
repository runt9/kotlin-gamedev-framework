package com.runt9.kgdf.game

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.runt9.kgdf.application.ApplicationConfiguration
import com.runt9.kgdf.asset.TextureDefinition
import com.runt9.kgdf.asset.TextureRegistry
import com.runt9.kgdf.ext.inject
import com.runt9.kgdf.inject.AdditionalInjectorDependencies
import com.runt9.kgdf.inject.Injector

object Launcher {
    // If/when more things required to actually start the game pop up, this can get more complicated, but for now a simple start method is good.

    inline fun <reified T : KgdfGame> start(
        gameConfig: GameConfig,
        additionalDependencies: AdditionalInjectorDependencies = emptyList(),
        textures: Collection<TextureDefinition> = emptyList()
    ) {
        additionalDependencies.forEach(Injector::registerAdditionalDependencies)
        Injector.bindSingleton(gameConfig)
        TextureRegistry.registerTextures(textures)
        Injector.initStartupDeps()
        Lwjgl3Application(inject<T>(), inject<ApplicationConfiguration>())
    }
}
