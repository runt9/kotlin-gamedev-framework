plugins {
    id("kgdfw-common-plugin")
}

dependencies {
    api(project(":core"))
    api(libs.mcp.sdk)
    implementation(libs.ktor.server.cio)
}
