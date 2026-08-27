plugins {
    id("kgdfw-common-plugin")
}

dependencies {
    api(project(":core"))
    implementation(libs.ktor.server.cio)
}
