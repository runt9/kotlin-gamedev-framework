package com.runt9.kgdf.testsupport

import com.runt9.kgdf.log.KgdfLog
import com.runt9.kgdf.log.LogLevel
import com.runt9.kgdf.log.LogSink

/**
 * Collects log lines so a test can assert on them. Synchronized because kgdfw logs from several named threads,
 * and a test asserting on output written by the Event-Thread is the normal case rather than the exotic one.
 */
class CapturingSink : LogSink {
    data class Entry(val level: LogLevel, val message: String, val throwable: Throwable?)

    private val captured = mutableListOf<Entry>()

    val entries: List<Entry> get() = synchronized(captured) { captured.toList() }

    override fun log(level: LogLevel, message: String, throwable: Throwable?) {
        synchronized(captured) { captured += Entry(level, message, throwable) }
    }

    fun messagesAt(level: LogLevel): List<String> = entries.filter { it.level == level }.map { it.message }
}

/**
 * Runs [block] with logging captured, then restores whatever was there before.
 *
 * [KgdfLog] is process-wide, so a test that swaps the sink without restoring it silently steals every later
 * test's log output. Always prefer this to assigning `KgdfLog.sink` directly.
 *
 * Inline so [block] may suspend; drop the keyword and any caller awaiting a background thread stops compiling.
 */
inline fun <T> capturingLogs(minLevel: LogLevel = LogLevel.TRACE, block: (CapturingSink) -> T): T {
    val previousSink = KgdfLog.sink
    val previousLevel = KgdfLog.minLevel
    val sink = CapturingSink()
    KgdfLog.sink = sink
    KgdfLog.minLevel = minLevel

    return try {
        block(sink)
    } finally {
        KgdfLog.sink = previousSink
        KgdfLog.minLevel = previousLevel
    }
}
