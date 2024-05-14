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
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.6.0"
    id("com.github.ben-manes.versions") version "0.51.0"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/releases/")
    maven("https://jitpack.io")
}

fun DependencyHandlerScope.implementationKotlin(vararg names: String) = names.forEach { implementation(kotlin(it)) }
fun DependencyHandlerScope.implementationKotlinx(vararg opts: Pair<String, String>) =
    opts.forEach { implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-${it.first}", version = it.second) }

fun DependencyHandlerScope.implementationGdx(vararg names: String, classifier: String = "") =
    names.forEach { implementation(group = "com.badlogicgames.gdx", name = it, version = gdxVersion, classifier = classifier) }

fun DependencyHandlerScope.implementationGdxNative(vararg names: String) = implementationGdx(classifier = "natives-desktop", names = names)
fun DependencyHandlerScope.implementationKtx(vararg names: String) =
    names.forEach { implementation(group = "io.github.libktx", name = "ktx-$it", version = ktxVersion) }

fun DependencyHandlerScope.implementationKorlibs(vararg names: String) =
    names.forEach { implementation(group = "com.soywiz.korlibs.$it", name = "$it-jvm", version = korlibsVersion) }

dependencies {
    implementationKotlin("stdlib", "reflect")
    implementationKotlinx("serialization-json" to kotlinSerializationVersion, "coroutines-core" to kotlinCoroutinesVersion)
    implementationGdx("gdx", "gdx-freetype", "gdx-backend-lwjgl3")
    implementationGdxNative("gdx-platform", "gdx-freetype-platform")
    implementation("com.badlogicgames.gdx:gdx-ai:$gdxAiVersion")
    implementationKorlibs("klock")

    implementationKtx(
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

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertkVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.test {
    useJUnitPlatform()
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
