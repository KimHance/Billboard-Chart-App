plugins {
    id("hance.android.library.compose")
}

android {
    namespace = "com.hancekim.billboard.core.designsystem"
}

dependencies {
    api(projects.core.designFoundation)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil)
}
