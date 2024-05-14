package com.runt9.kgdf.settings

import kotlinx.serialization.Serializable

@Serializable
data class PlayerSettings(val fullscreen: Boolean, val vsync: Boolean, val logLevel: Int, val resolution: Resolution) {
    @Serializable
    data class Resolution(val width: Int, val height: Int)
}

