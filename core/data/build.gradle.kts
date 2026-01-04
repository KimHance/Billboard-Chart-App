plugins {
    alias(libs.plugins.billboard.android.library)
    alias(libs.plugins.billboard.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.hancekim.billboard.coredata"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.annotation.experimental)
}