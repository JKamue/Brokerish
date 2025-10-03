plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":packets"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}