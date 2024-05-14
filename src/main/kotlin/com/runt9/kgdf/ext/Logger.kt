package com.runt9.kgdf.ext

import korlibs.time.DateFormat
import korlibs.time.DateTimeTz


class Logger(name: String) : ktx.log.Logger(name) {
    override fun buildMessage(message: String): String {
        val dt = DateFormat("yyyy-MM-dd HH:mm:ss").format(DateTimeTz.nowLocal())
        val caller = Thread.currentThread().stackTrace[2]
        return "[ $dt | ${Thread.currentThread().name} | ${name}.${caller.methodName} ]: $message"
    }
}

fun logger(): Logger {
    val caller = Thread.currentThread().stackTrace[2]
    return Logger(Class.forName(caller.className).simpleName)
}
