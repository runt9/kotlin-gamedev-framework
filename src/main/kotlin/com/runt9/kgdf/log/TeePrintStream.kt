package com.runt9.kgdf.log

import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintStream

// Found and modified from https://github.com/oreillymedia/java_cookbook_3e/blob/master/javacooksrc/javacooksrc/main/java/io/TeePrintStream.java
class TeePrintStream(
    private val primary: PrintStream,
    secondary: OutputStream
) : PrintStream(secondary, true) {
    constructor(primary: PrintStream, filename: String) : this(primary, FileOutputStream(filename))

    override fun checkError(): Boolean {
        return primary.checkError() || super.checkError()
    }

    override fun write(x: Int) {
        primary.write(x)
        super.write(x)
    }

    override fun write(x: ByteArray, o: Int, l: Int) {
        primary.write(x, o, l)
        super.write(x, o, l)
    }

    override fun close() {
        primary.close()
        super.close()
    }

    override fun flush() {
        primary.flush()
        super.flush()
    }
}

