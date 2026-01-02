package com.runt9.kgdf.log

import com.badlogic.gdx.ApplicationLogger
import io.github.oshai.kotlinlogging.KotlinLogging

class KotlinLoggingLogger : ApplicationLogger {
    private val rootLogger = KotlinLogging.logger("root")

    override fun log(tag: String?, message: String?) {
        rootLogger.info { "[$tag] $message" }
    }

    override fun log(tag: String?, message: String?, exception: Throwable?) {
        rootLogger.info(exception) { "[$tag] $message" }
    }

    override fun error(tag: String?, message: String?) {
        rootLogger.error { "[$tag] $message" }
    }

    override fun error(tag: String?, message: String?, exception: Throwable?) {
        rootLogger.error(exception) { "[$tag] $message" }
    }

    override fun debug(tag: String?, message: String?) {
        rootLogger.debug { "[$tag] $message" }
    }

    override fun debug(tag: String?, message: String?, exception: Throwable?) {
        rootLogger.debug(exception) { "[$tag] $message" }
    }
}
