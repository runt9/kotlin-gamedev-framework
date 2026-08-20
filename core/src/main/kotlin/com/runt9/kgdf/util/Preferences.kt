package com.runt9.kgdf.util

import com.badlogic.gdx.scenes.scene2d.utils.UIUtils
import java.nio.file.Paths

/**
 * Absolute directory that per-user game data (settings, saves) lives under.
 *
 * **Every branch must return an ABSOLUTE path.** Callers hand the result to LibGDX as
 * [com.badlogic.gdx.Files.FileType.Absolute], so a relative value silently resolves against the process working
 * directory instead: the game then writes saves next to wherever it happened to be launched from, and launching
 * from a different directory reads a different save. The original of this function returned bare `.config` and
 * `Library/Preferences`, which was correct only for LibGDX's `External` type it was lifted from, and looked fine
 * on Windows because `APPDATA` is already absolute.
 */
// TODO: This is mostly pulled from https://github.com/libgdx/libgdx/pull/6614/files to put prefs in the right spot. Will be included in
//  Lwjgl3ApplicationConfiguration in a future release.
fun getDefaultPreferencesDirectory(): String {
    val home = System.getProperty("user.home")
    return when {
        UIUtils.isWindows -> System.getenv("APPDATA")?.takeIf(String::isNotBlank) ?: Paths.get(home, ".prefs").toString()
        UIUtils.isMac -> Paths.get(home, "Library", "Preferences").toString()
        // XDG says an unset OR empty XDG_CONFIG_HOME means fall back to ~/.config.
        UIUtils.isLinux -> System.getenv("XDG_CONFIG_HOME")?.takeIf(String::isNotBlank) ?: Paths.get(home, ".config").toString()
        else -> Paths.get(home, ".prefs").toString()
    }
}
