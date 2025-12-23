plugins {
    alias(libs.plugins.billboard.android.library)
    alias(libs.plugins.billboard.android.hilt)
}

android {
    namespace = "com.hancekim.billboard.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}