package com.runt9.kgdf.log

import korlibs.time.DateFormat
import korlibs.time.DateTimeTz
import java.nio.file.Files
import java.nio.file.Path

// Frames declared by these classes are the logging plumbing itself and are skipped when working out who is
// logging. Renaming this file changes "LoggerKt" and would silently start reporting the wrong caller; LoggerTest
// asserts the caller name precisely so that breakage shows up as a failure rather than as wrong log output.
private val SELF_STACKS_TO_SKIP = setOf("com.runt9.kgdf.log.Logger", "com.runt9.kgdf.log.LoggerKt")

private val walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

// Lazy and short-circuiting, unlike Thread.currentThread().stackTrace, which materializes the whole stack. This
// runs per emitted log line, so the difference is not academic.
private fun callerFrame(): StackWalker.StackFrame? = walker.walk { frames ->
    frames.filter { it.declaringClass.name !in SELF_STACKS_TO_SKIP }.findFirst().orElse(null)
}

class Logger(val name: String) {
    fun error(throwable: Throwable? = null, message: () -> String) = log(LogLevel.ERROR, throwable, message)
    fun warn(throwable: Throwable? = null, message: () -> String) = log(LogLevel.WARN, throwable, message)
    fun info(throwable: Throwable? = null, message: () -> String) = log(LogLevel.INFO, throwable, message)
    fun debug(throwable: Throwable? = null, message: () -> String) = log(LogLevel.DEBUG, throwable, message)
    fun trace(throwable: Throwable? = null, message: () -> String) = log(LogLevel.TRACE, throwable, message)

    // The level check has to come first: both the caller's lambda and buildMessage's stack walk are skipped
    // entirely when the level is disabled, which is what keeps a debug line inside the render loop free.
    private fun log(level: LogLevel, throwable: Throwable?, message: () -> String) {
        if (!KgdfLog.isEnabled(level)) return
        KgdfLog.sink.log(level, buildMessage(message()), throwable)
    }

    fun buildMessage(message: String): String {
        val dt = DateFormat("yyyy-MM-dd HH:mm:ss").format(DateTimeTz.nowLocal())
        val caller = callerFrame()
        return "[ $dt | ${Thread.currentThread().name} | ${name}.${caller?.methodName} ]: $message"
    }
}

fun kgdfLogger(): Logger = Logger(callerFrame()?.declaringClass?.simpleName ?: "")

fun teeStderrToFile(filePath: Path) {
    Files.createDirectories(filePath.parent)
    val teePs = TeePrintStream(System.err, filePath.toString())
    System.setErr(teePs)
}
