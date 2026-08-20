package com.runt9.kgdf.log

import io.github.oshai.kotlinlogging.KotlinLogging

/** Where formatted log lines go. Swap [KgdfLog.sink] to redirect them, which is what tests do. */
fun interface LogSink {
    fun log(level: LogLevel, message: String, throwable: Throwable?)
}

class KotlinLoggingSink : LogSink {
    private val logger = KotlinLogging.logger("root")

    override fun log(level: LogLevel, message: String, throwable: Throwable?) = when (level) {
        LogLevel.ERROR -> logger.error(throwable) { message }
        LogLevel.WARN -> logger.warn(throwable) { message }
        LogLevel.INFO -> logger.info(throwable) { message }
        LogLevel.DEBUG -> logger.debug(throwable) { message }
        LogLevel.TRACE -> logger.trace(throwable) { message }
    }
}
