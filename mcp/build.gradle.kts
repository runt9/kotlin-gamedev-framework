plugins {
    id("kgdfw-common-plugin")
}

dependencies {
    // CHANGE: I don't think any of these comments are necessary
    // api, not implementation: this module's own public API will expose core's types (screens, stages, the
    // injector), so a consumer needs core resolvable rather than merely visible during this module's compilation.
    api(project(":core"))

    // api for the same reason: a consumer registers its own tools against SDK types, so it needs them resolvable.
    api(libs.mcp.sdk)
    // The SDK brings ktor-server transitively; naming the engine keeps CIO a choice rather than whatever the
    // SDK happened to pull.
    implementation(libs.ktor.server.cio)
}
