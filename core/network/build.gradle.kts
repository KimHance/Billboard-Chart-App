plugins {
    alias(libs.plugins.billboard.android.library)
    alias(libs.plugins.billboard.android.hilt)
}

android {
    namespace = "com.hancekim.billboard.core.network"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(projects.core.model)
    api(libs.retrofit.core)
    api(libs.retrofit.kotlin.serialization)
    api(libs.okhttp.logging)
    api(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
}