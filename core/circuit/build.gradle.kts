plugins {
    alias(libs.plugins.billboard.android.library.compose)
    alias(libs.plugins.billboard.android.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.hancekim.billboard.core.circuit"
}

dependencies {
    api(libs.bundles.circuit)
    api(libs.circuit.codegen.annotations)
}