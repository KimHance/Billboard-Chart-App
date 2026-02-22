plugins {
    alias(libs.plugins.billboard.android.feature)
    alias(libs.plugins.billboard.android.library.compose)
    alias(libs.plugins.billboard.android.hilt)
    alias(libs.plugins.billboard.circuit)
}

android {
    namespace = "com.hancekim.billboard.home"
}

dependencies {
    implementation(projects.core.circuit)
    implementation(projects.core.domain)
    implementation(projects.core.player)

    androidTestImplementation(projects.core.dataTest)
    androidTestImplementation(libs.circuit.test)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
}