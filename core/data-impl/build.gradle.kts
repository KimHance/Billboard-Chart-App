plugins {
    alias(libs.plugins.billboard.android.library)
    alias(libs.plugins.billboard.android.hilt)
    alias(libs.plugins.billboard.android.room)
}

android {
    namespace = "com.hancekim.billboard.core.dataimpl"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.dataSource)
}