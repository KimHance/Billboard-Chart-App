plugins {
    alias(libs.plugins.billboard.android.library.compose)
}

android {
    namespace = "com.hancekim.billboard.core.designsystem"
}

dependencies {
    api(projects.core.designFoundation)
    api(projects.core.imageLoader)
    api(projects.core.data)
    api(projects.core.resource)
    implementation(libs.timber)
}
