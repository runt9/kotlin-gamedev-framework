package com.runt9.kgdf.ui.controller

import com.runt9.kgdf.event.AssetsLoadedEvent
import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.event.HandlesEvent
import com.runt9.kgdf.ext.percent
import com.runt9.kgdf.log.kgdfLogger
import com.runt9.kgdf.ui.viewModel.LoadingViewModel
import ktx.assets.async.AssetStorage
import ktx.async.onRenderingThread

abstract class LoadingController(width: Float, height: Float, private val assets: AssetStorage, private val eventBus: EventBus) : UiScreenController(width, height) {
    private val logger = kgdfLogger()
    abstract override val vm: LoadingViewModel

    override fun show() {
        super.show()
        eventBus.registerHandlers(this)
    }

    override fun render(delta: Float) {
        super.render(delta)
        assets.progress.run {
            if (percent != vm.loadingPercent.get()) {
                logger.debug { "Asset loading status: $loaded / $total (${percent.percent()}%)" }
                vm.loadingPercent(percent)
            }
        }
    }

    override fun hide() {
        super.hide()
        eventBus.unregisterHandlers(this)
    }

    @HandlesEvent(AssetsLoadedEvent::class)
    @Suppress("UnusedPrivateMember")
    suspend fun handle() = onRenderingThread {
        logger.debug { "Loading complete" }
        loadComplete()
    }

    abstract fun loadComplete()
}
