package com.runt9.kgdf.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.runt9.kgdf.inject.Injector
import com.runt9.kgdf.ui.controller.Controller
import com.runt9.kgdf.ui.view.View
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor

@Scene2dDsl
@Suppress("UNCHECKED_CAST")
inline fun <S, reified C : Controller, V : View> KWidget<S>.uiComponent(
    controllerInit: C.() -> Unit = {},
    noinline init: V.(S) -> Unit = {}
): V {
    val component = Injector.newInstanceOf<C>()
    component.controllerInit()
    component.load()
    val initFn = init as Actor.(S) -> Unit

    return actor(component.view as Actor) {
        component.view.init()
        initFn(it)
    } as V
}
