package com.runt9.kgdf.ui

import com.badlogic.gdx.utils.Disposable
import com.runt9.kgdf.event.ShowDialogRequest
import com.runt9.kgdf.ext.dynamicInject
import com.runt9.kgdf.ext.dynamicInjectCheckAssignableFrom
import com.runt9.kgdf.event.EventBus
import com.runt9.kgdf.event.HandlesEvent
import com.runt9.kgdf.ui.core.BasicStage
import ktx.async.onRenderingThread

class DialogManager(private val eventBus: EventBus) : Disposable {
    var currentStage: BasicStage? = null

    init {
        eventBus.registerHandlers(this)
    }

    @HandlesEvent
    suspend fun showDialog(event: ShowDialogRequest<*>) = onRenderingThread {
        currentStage?.run {
            val injects = event.data.map { dynamicInjectCheckAssignableFrom(it::class.java) to it }.toTypedArray()
            val dialog = dynamicInject(event.dialogClass, *injects)
            dialog.show(this)
        }
    }

    override fun dispose() {
        eventBus.unregisterHandlers(this)
    }
}
