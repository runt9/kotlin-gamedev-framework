import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.versions)
    `maven-publish`
    `java-library`
}

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/releases/")
    maven("https://jitpack.io")
}

fun DependencyHandlerScope.apiKotlin(vararg names: String) = names.forEach { api(kotlin(it)) }

fun DependencyHandlerScope.apiGdx(vararg names: String, classifier: String = "") =
    names.forEach { api(group = "com.badlogicgames.gdx", name = it, version = libs.versions.gdx.get(), classifier = classifier) }

fun DependencyHandlerScope.apiGdxNative(vararg names: String) = apiGdx(classifier = "natives-desktop", names = names)
fun DependencyHandlerScope.apiKtx(vararg names: String) =
    names.forEach { api(group = "io.github.libktx", name = "ktx-$it", version = libs.versions.ktx.get()) }

fun DependencyHandlerScope.apiKorlibs(vararg names: String) =
    names.forEach { api(group = "com.soywiz.korlibs.$it", name = "$it-jvm", version = libs.versions.korlibs.get()) }

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
        "log",
        "math",
        "preferences",
        "reflect",
        "vis",
        "vis-style"
    )

    testApi(kotlin("test"))
    testApi(libs.junit.jupiter)
    testApi(libs.assertk)
    testApi(libs.mockk)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name, "Implementation-Version" to project.version))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name
            from(components["java"])
        }
    }
}

tasks.withType<KotlinCompile> {
    val optIns = listOf(
        "ktx.reflect.Reflection",
        "kotlinx.serialization.ExperimentalSerializationApi",
        "kotlinx.coroutines.ExperimentalCoroutinesApi"
    )
    compilerOptions.freeCompilerArgs.add("-opt-in=${optIns.joinToString(",")}")
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}