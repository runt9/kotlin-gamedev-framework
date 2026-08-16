package com.runt9.kgdf.log

import com.badlogic.gdx.Application

/**
 * Declared most severe first, so a message is emitted when its `ordinal <= KgdfLog.minLevel.ordinal`.
 *
 * [gdxLevel] exists only to keep LibGDX's own internal logging in step with ours, and is lossy in both
 * directions: LibGDX has no WARN, and nothing more verbose than DEBUG.
 */
enum class LogLevel(val gdxLevel: Int) {
    ERROR(Application.LOG_ERROR),
    WARN(Application.LOG_ERROR),
    INFO(Application.LOG_INFO),
    DEBUG(Application.LOG_DEBUG),
    TRACE(Application.LOG_DEBUG)
}
