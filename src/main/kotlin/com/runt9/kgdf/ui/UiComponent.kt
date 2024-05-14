package com.runt9.kgdf.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import com.runt9.kgdf.config.Injector
import com.runt9.kgdf.ui.controller.Controller
import com.runt9.kgdf.ui.view.View

@Scene2dDsl
@Suppress("UNCHECKED_CAST")
inline fun <S, reified C : Controller, V : View> KWidget<S>.uiComponent(
    controllerInit: C.() -> Unit = {},
    noinline init: V.(S) -> Unit = {}
): V {
    val component = Injector.newInstanceOf<C>()
    component.controllerInit()
    component.load()
    component.view.init()
    return actor(component.view as Actor, init as Actor.(S) -> Unit) as V
}
