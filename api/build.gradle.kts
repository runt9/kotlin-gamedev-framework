plugins {
    id("kgdfw-common-plugin")
}

dependencies {
    api(project(":core"))

    // core is `api` because HarnessServer.serve takes a ktor Routing, so the type is in this module's public
    // surface; cio is the engine choice and stays an implementation detail.
    api(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
}
