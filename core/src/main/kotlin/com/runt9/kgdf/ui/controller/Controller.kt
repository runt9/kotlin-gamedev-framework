package com.runt9.kgdf.ui.controller

import com.badlogic.gdx.utils.Disposable
import com.runt9.kgdf.ext.dynamicInject
import com.runt9.kgdf.ext.dynamicInjectCheckIsSubclassOf
import com.runt9.kgdf.ui.view.View
import com.runt9.kgdf.ui.viewModel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.async.MainDispatcher

interface Controller : Disposable {
    val vm: ViewModel
    val view: View

    fun load() = Unit

    override fun dispose() {
        view.dispose()
        vm.dispose()
    }

    fun launchOnRenderingThread(block: suspend CoroutineScope.() -> Unit) = KtxAsync.launch(MainDispatcher, block = block)
}

inline fun <reified V : View> Controller.injectView() = dynamicInject(
    V::class,
    dynamicInjectCheckIsSubclassOf(Controller::class.java) to this,
    dynamicInjectCheckIsSubclassOf(ViewModel::class.java) to vm
)

inline fun <reified V : View> Controller.lazyInjectView() = lazy { injectView<V>() }
