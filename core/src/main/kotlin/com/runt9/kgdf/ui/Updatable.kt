package com.runt9.kgdf.ui

import com.runt9.kgdf.ui.viewModel.ViewModel

interface Updatable {
    fun update()

    operator fun <T : Any> ViewModel.Binding<T>.invoke(): T {
        bind(this@Updatable)
        return get()
    }
}

interface UpdatableValue<T : Any> {
    fun update(newValue: T)
}

fun updatable(updater: Updatable.() -> Unit) = object : Updatable {
    override fun update() {
        updater()
    }
}

fun <T : Any> updatableValue(updater: UpdatableValue<T>.(T) -> Unit) = object : UpdatableValue<T> {
    override fun update(newValue: T) {
        updater(newValue)
    }
}
