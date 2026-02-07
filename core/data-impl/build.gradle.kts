plugins {
    alias(libs.plugins.billboard.android.library)
    alias(libs.plugins.billboard.android.hilt)
}

android {
    namespace = "com.hancekim.billboard.core.dataimpl"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.dataSource)
}