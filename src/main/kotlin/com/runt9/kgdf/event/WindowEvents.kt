package com.runt9.kgdf.event

import com.runt9.kgdf.ui.controller.DialogController
import com.runt9.kgdf.ui.core.UiScreen
import kotlin.reflect.KClass

class ChangeScreenRequest<S : UiScreen>(val screenClass: KClass<S>) : Event
inline fun <reified S : UiScreen> changeScreenRequest() = ChangeScreenRequest(S::class)
inline fun <reified S : UiScreen> EventBus.enqueueChangeScreen() = enqueueEventSync(changeScreenRequest<S>())

class ShowDialogRequest<D : DialogController>(val dialogClass: KClass<D>, vararg val data: Any) : Event
inline fun <reified D : DialogController> showDialogRequest(vararg data: Any) = ShowDialogRequest(D::class, *data)
inline fun <reified S : DialogController> EventBus.enqueueShowDialog(vararg data: Any) = enqueueEventSync(showDialogRequest<S>(*data))

class ExitRequest : Event
fun EventBus.enqueueExitRequest() = enqueueEventSync(ExitRequest())
