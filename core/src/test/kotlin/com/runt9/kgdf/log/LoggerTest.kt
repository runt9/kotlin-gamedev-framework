package com.runt9.kgdf.log

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch

// Called through a named function so the frame Logger inspects is stable, not a Kotest lambda.
private fun buildMessageFrom(logger: Logger) = logger.buildMessage("payload")

// A class, not a top-level function: every top-level function here (including Kotlin's synthetic `access$`
// accessors) compiles into the same `LoggerTestKt`, so a name assertion from there passes at any stack depth.
private class LoggerNamingProbe {
    fun makeLogger() = kgdfLogger()
}

/**
 * Characterization tests pinning the logger's behaviour before it is decoupled from `Gdx.app`.
 *
 * The point is the caller detection, not the format. Both `buildMessage` and `kgdfLogger` index the stack at a
 * fixed depth, so changing the class hierarchy shifts which frame they land on: every line keeps rendering and
 * silently names the wrong method.
 */
class LoggerTest : FunSpec({
    test("buildMessage renders timestamp, thread and caller, and names the calling method") {
        val message = buildMessageFrom(Logger("SomeClass"))

        message shouldMatch Regex("""\[ \d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} \| [^|]+ \| SomeClass\.buildMessageFrom ]: payload""")
    }

    test("buildMessage uses the logger's own name, not the calling class") {
        val message = buildMessageFrom(Logger("DeliberatelyNotTheTestClass"))

        message shouldContain "DeliberatelyNotTheTestClass.buildMessageFrom"
    }

    test("kgdfLogger names itself after the calling class") {
        val logger = LoggerNamingProbe().makeLogger()

        logger.name shouldBe "LoggerNamingProbe"
    }
})
