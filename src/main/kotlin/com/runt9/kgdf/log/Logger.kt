package com.runt9.kgdf.log

import korlibs.time.DateFormat
import korlibs.time.DateTimeTz
import java.nio.file.Files
import java.nio.file.Path


class Logger(name: String) : ktx.log.Logger(name) {
    override fun buildMessage(message: String): String {
        val dt = DateFormat("yyyy-MM-dd HH:mm:ss").format(DateTimeTz.nowLocal())
        val caller = Thread.currentThread().stackTrace[2]
        return "[ $dt | ${Thread.currentThread().name} | ${name}.${caller.methodName} ]: $message"
    }
}

fun kgdfLogger(): Logger {
    val caller = Thread.currentThread().stackTrace[2]
    return Logger(Class.forName(caller.className).simpleName)
}

fun teeStderrToFile(filePath: Path) {
    Files.createDirectories(filePath.parent)
    val teePs = TeePrintStream(System.err, filePath.fileName.toString())
    System.setErr(teePs)
}
