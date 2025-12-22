plugins {
    alias(libs.plugins.billboard.android.library.compose)
    alias(libs.plugins.billboard.android.hilt)
}
android {
    namespace = "com.hancekim.billboard.core.imageloader"
}

dependencies {
    implementation(libs.coil)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil.network)
    implementation(libs.okhttp.logging)
    implementation(libs.timber)
}