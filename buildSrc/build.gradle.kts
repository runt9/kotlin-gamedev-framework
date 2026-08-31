plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    // Lots of plugins live here and not in mavenCentral
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlinPlugin)
    implementation(libs.kotlinxSerializationPlugin)
    implementation(libs.koverPlugin)
    implementation(libs.benManesPlugin)
}
