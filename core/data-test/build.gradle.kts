plugins {
    alias(libs.plugins.billboard.android.library)
    alias(libs.plugins.billboard.android.hilt)
}

android {
    namespace = "com.hancekim.billboard.core.datatest"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.dataImpl)
    implementation(libs.hilt.android.testing)
}