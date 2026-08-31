package com.runt9.kgdf.input

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter
import com.runt9.kgdf.ext.lazyInject

/**
 * Clears held input when the window loses focus, so a drag whose mouse-up lands outside it does not leave that
 * button held forever. Set on the config in `Launcher.start`.
 *
 * The service must stay lazily resolved: this is built during `Injector.initStartupDeps`, while
 * [InputTrackingService] is not bound until `initRunningDeps` in `KgdfGame.create`.
 */
class FocusTrackingWindowListener : Lwjgl3WindowAdapter() {
    private val inputTracking by lazyInject<InputTrackingService>()

    override fun focusLost() = inputTracking.clear()
}
