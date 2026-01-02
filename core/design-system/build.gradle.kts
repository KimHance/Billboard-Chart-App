plugins {
    alias(libs.plugins.billboard.android.library.compose)
}

android {
    namespace = "com.hancekim.billboard.core.designsystem"
}

dependencies {
    api(projects.core.designFoundation)
    api(projects.core.imageLoader)
    implementation(libs.timber)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil)
}
