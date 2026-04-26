plugins {
    alias(libs.plugins.billboard.android.feature)
    alias(libs.plugins.billboard.android.hilt)
    alias(libs.plugins.billboard.circuit)
}

android {
    namespace = "com.hancekim.billboard.feature.collection"
}

dependencies {
    implementation(projects.core.designSystem)
    implementation(projects.core.domain)
    implementation(projects.core.circuit)

    androidTestImplementation(projects.core.data)
    androidTestImplementation(projects.core.dataTest)
    androidTestImplementation(libs.circuit.test)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
}
