plugins {
    alias(libs.plugins.billboard.android.library)
    alias(libs.plugins.billboard.android.hilt)
}
dependencies {
    implementation(libs.kotlinx.coroutines.core)
}