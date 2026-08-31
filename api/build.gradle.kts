plugins {
    id("kgdfw-common-plugin")
}

dependencies {
    api(project(":core"))

    // core is `api` because HarnessServer.serve takes a ktor Routing, so the type is in this module's public
    // surface; cio is the engine choice and stays an implementation detail.
    api(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    testImplementation(testFixtures(project(":core")))
    testRuntimeOnly(libs.junit.platform.launcher)
}
