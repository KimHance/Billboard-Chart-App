plugins {
    alias(libs.plugins.billboard.android.library.compose)
}

android {
    namespace = "com.hancekim.billboard.core.player"
}

dependencies {
    implementation(projects.core.designSystem)
    implementation(libs.timber)
    api(libs.youtube.player)
}