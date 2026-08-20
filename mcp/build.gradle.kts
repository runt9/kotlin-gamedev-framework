plugins {
    id("kgdfw-common-plugin")
}

dependencies {
    // api, not implementation: this module's own public API will expose core's types (screens, stages, the
    // injector), so a consumer needs core resolvable rather than merely visible during this module's compilation.
    api(project(":core"))
}
