package com.runt9.kgdf.log

/**
 * Process-wide logging configuration. Both fields are deliberately mutable and `@Volatile`: the level is set
 * once at startup from player settings, and the sink is swapped by tests, both from a different thread than
 * the one doing the logging.
 */
object KgdfLog {
    @Volatile
    var sink: LogSink = KotlinLoggingSink()

    @Volatile
    var minLevel: LogLevel = LogLevel.ERROR

    fun isEnabled(level: LogLevel) = level.ordinal <= minLevel.ordinal
}
