package com.runt9.kgdf.ext.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.runt9.kgdf.ui.Updatable
import com.runt9.kgdf.ui.updatable
import com.runt9.kgdf.ui.viewModel.ViewModel
import ktx.actors.onChange

fun <T : Any> Button.bindButtonDisabled(
    binding: ViewModel.Binding<T>,
    disabledValue: T,
    evaluateOnCall: Boolean = true
) = bindUpdatable(binding, evaluateOnCall) {
    isDisabled = binding.get() == disabledValue
    touchable = if (isDisabled) Touchable.disabled else Touchable.enabled
}

fun Button.bindButtonDisabled(
    bindings: Iterable<ViewModel.Binding<*>>,
    evaluateOnCall: Boolean = true,
    predicate: () -> Boolean
) = bindUpdatables(bindings, evaluateOnCall) {
    isDisabled = predicate()
    touchable = if (isDisabled) Touchable.disabled else Touchable.enabled
}

fun Button.bindButtonDisabledToVmDirty(vm: ViewModel, disabledValue: Boolean, evaluateOnCall: Boolean = true) {
    bindButtonDisabled(vm.dirty, disabledValue, evaluateOnCall)
}

fun CheckBox.bindChecked(binding: ViewModel.Binding<Boolean>) {
    isChecked = binding.get()
    onChange { binding.set(isChecked) }
}

// TODO: Looks like bindings are stacking up over time?
fun Label.bindLabelText(strGetter: Updatable.() -> String) = updatable { setText(strGetter()) }.update()

fun <T : Any> Actor.bindVisible(
    binding: ViewModel.Binding<T>,
    visibleValue: T,
    evaluateOnCall: Boolean = true
) = bindVisible(binding, evaluateOnCall) { this == visibleValue }

fun <T : Any> Actor.bindVisible(
    binding: ViewModel.Binding<T>,
    evaluateOnCall: Boolean = true,
    predicate: T.() -> Boolean
) = bindUpdatable(binding, evaluateOnCall) {
    isVisible = predicate(binding.get())
    touchable = if (isVisible) Touchable.enabled else Touchable.disabled
}

fun <A : Actor> A.bindUpdatables(bindings: Iterable<ViewModel.Binding<*>>, evaluateOnCall: Boolean = true, updater: A.() -> Unit) {
    val updatable = updatable { updater() }

    bindings.forEach { it.bind(updatable) }

    if (evaluateOnCall) {
        updatable.update()
    }
}

fun <T : Any, A : Actor> A.bindUpdatable(binding: ViewModel.Binding<T>, evaluateOnCall: Boolean = true, updater: A.(T) -> Unit) {
    val updatable = updatable { updater(binding.get()) }

    binding.bind(updatable)

    if (evaluateOnCall) {
        updatable.update()
    }
}
