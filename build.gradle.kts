import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val gdxVersion: String by project
val gdxAiVersion: String by project
val ktxVersion: String by project
val korlibsVersion: String by project
val kotlinCoroutinesVersion: String by project
val kotlinSerializationVersion: String by project
val junitVersion: String by project
val assertkVersion: String by project
val mockkVersion: String by project

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.6.0"
    id("com.github.ben-manes.versions") version "0.51.0"
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
fun DependencyHandlerScope.apiKotlinx(vararg opts: Pair<String, String>) =
    opts.forEach { api(group = "org.jetbrains.kotlinx", name = "kotlinx-${it.first}", version = it.second) }

fun DependencyHandlerScope.apiGdx(vararg names: String, classifier: String = "") =
    names.forEach { api(group = "com.badlogicgames.gdx", name = it, version = gdxVersion, classifier = classifier) }

fun DependencyHandlerScope.apiGdxNative(vararg names: String) = apiGdx(classifier = "natives-desktop", names = names)
fun DependencyHandlerScope.apiKtx(vararg names: String) =
    names.forEach { api(group = "io.github.libktx", name = "ktx-$it", version = ktxVersion) }

fun DependencyHandlerScope.apiKorlibs(vararg names: String) =
    names.forEach { api(group = "com.soywiz.korlibs.$it", name = "$it-jvm", version = korlibsVersion) }

dependencies {
    apiKotlin("stdlib", "reflect")
    apiKotlinx("serialization-json" to kotlinSerializationVersion, "serialization-protobuf" to kotlinSerializationVersion, "coroutines-core" to kotlinCoroutinesVersion)
    apiGdx("gdx", "gdx-freetype", "gdx-backend-lwjgl3")
    apiGdxNative("gdx-platform", "gdx-freetype-platform")
    api("com.badlogicgames.gdx:gdx-ai:$gdxAiVersion")
    apiKorlibs("klock")

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
    testApi("org.junit.jupiter:junit-jupiter:$junitVersion")
    testApi("com.willowtreeapps.assertk:assertk-jvm:$assertkVersion")
    testApi("io.mockk:mockk:$mockkVersion")
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
    kotlinOptions.freeCompilerArgs += "-Xopt-in=${optIns.joinToString(",")}"
    kotlinOptions.jvmTarget = "17"
}
