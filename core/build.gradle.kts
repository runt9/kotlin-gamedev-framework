plugins {
    id("kgdfw-common-plugin")
}

// These stay in a module build script rather than moving to the convention plugin: they read
// `libs.versions.*`, and generated version-catalog accessors exist in build scripts but NOT in precompiled
// script plugins. Moving them costs three VersionCatalogsExtension lookups and buys nothing.
fun DependencyHandlerScope.apiKotlin(vararg names: String) = names.forEach { api(kotlin(it)) }

fun DependencyHandlerScope.apiGdx(vararg names: String, classifier: String = "") {
    val version = libs.versions.gdx.get()
    // Single-string notation is `group:name:version[:classifier]`, so an empty classifier has to omit the
    // separator entirely -- a trailing colon is not the same coordinate and resolves to nothing.
    val suffix = if (classifier.isEmpty()) "" else ":$classifier"
    names.forEach { api("com.badlogicgames.gdx:$it:$version$suffix") }
}

fun DependencyHandlerScope.apiGdxNative(vararg names: String) = apiGdx(classifier = "natives-desktop", names = names)
fun DependencyHandlerScope.apiKtx(vararg names: String) =
    names.forEach { api("io.github.libktx:ktx-$it:${libs.versions.ktx.get()}") }

fun DependencyHandlerScope.apiKorlibs(vararg names: String) =
    names.forEach { api("com.soywiz.korlibs.$it:$it-jvm:${libs.versions.korlibs.get()}") }

dependencies {
    apiKotlin("stdlib", "reflect")
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.serialization.cbor)
    api(libs.kotlinx.coroutines.core)
    apiGdx("gdx", "gdx-freetype", "gdx-backend-lwjgl3")
    apiGdxNative("gdx-platform", "gdx-freetype-platform")
    api(libs.gdx.ai)
    apiKorlibs("klock")
    api(libs.freetype.stripe)
    api(libs.kotlin.logging)
    api(libs.slf4j.api)
    api(libs.logback.classic)

    apiKtx(
        "app",
        "actors",
        "assets",
        "assets-async",
        "async",
        "collections",
        "freetype",
        "freetype-async",
        "graphics",
        "inject",
        "json",
        "math",
        "preferences",
        "reflect",
        "vis",
        "vis-style"
    )

    // testFixturesApi, not testApi: test-scoped dependencies are never published, so testApi reaches no consumer.
    testFixturesApi(libs.kotest.framework.engine)
    testFixturesApi(libs.kotest.assertions.core)
    testFixturesApi(libs.kotest.runner.junit5)
    testFixturesApi(libs.kotlinx.coroutines.test)

    testRuntimeOnly(libs.junit.platform.launcher)
}
