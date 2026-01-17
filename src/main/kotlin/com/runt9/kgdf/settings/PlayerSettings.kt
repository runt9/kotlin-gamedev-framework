package com.runt9.kgdf.settings

import kotlinx.serialization.Serializable

@Serializable
data class PlayerSettings(val fullscreen: Boolean, val vsync: Boolean, val logLevel: Int, val resolution: Resolution, val mainVolume: Float = 0.2f, val usageData: Boolean = true) {
    @Serializable
    data class Resolution(val width: Int, val height: Int, val refreshRate: Int) {
        override fun toString() = "${width}x${height} @ ${refreshRate}hz"
    }
}
