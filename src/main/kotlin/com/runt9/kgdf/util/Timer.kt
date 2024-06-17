package com.runt9.kgdf.util

import kotlin.math.min

open class Timer(var targetTime: Float) {
    private var elapsedTime = 0f
    val percentComplete get() = elapsedTime / targetTime
    val isReady get() = elapsedTime >= targetTime && !isPaused
    var isPaused = false

    open fun tick(time: Float) {
        if (isReady || isPaused) return
        elapsedTime += time
    }

    open fun reset(rollover: Boolean = true) {
        elapsedTime = if (rollover) min(0f, elapsedTime - targetTime) else 0f
    }

    fun pause() { isPaused = true }
    fun resume() { isPaused = false }
}
