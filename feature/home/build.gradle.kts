plugins {
    alias(libs.plugins.billboard.android.feature)
    alias(libs.plugins.billboard.android.library.compose)
    alias(libs.plugins.billboard.android.hilt)
    alias(libs.plugins.billboard.circuit)
}

android {
    namespace = "com.hancekim.billboard.home"
}

dependencies {
    implementation(projects.core.circuit)
    implementation(projects.core.network)
}