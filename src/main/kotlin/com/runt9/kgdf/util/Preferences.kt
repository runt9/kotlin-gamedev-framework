package com.runt9.kgdf.util

import com.badlogic.gdx.scenes.scene2d.utils.UIUtils

// TODO: This is mostly pulled from https://github.com/libgdx/libgdx/pull/6614/files to put prefs in the right spot. Will be included in
//  Lwjgl3ApplicationConfiguration in a future release.
fun getDefaultPreferencesDirectory() =
    if (UIUtils.isWindows) System.getenv("APPDATA") ?: ".prefs"
    else if (UIUtils.isMac) "Library/Preferences"
    else if (UIUtils.isLinux) ".config"
    else ".prefs"
