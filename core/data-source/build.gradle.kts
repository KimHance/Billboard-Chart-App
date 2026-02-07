plugins {
    alias(libs.plugins.billboard.android.library)
    alias(libs.plugins.billboard.android.hilt)
}

android {
    namespace = "com.hancekim.billboard.core.datasource"
}

dependencies {
    implementation(projects.core.data)
    prodImplementation(projects.core.network)
    prodImplementation(libs.retrofit.core)
    prodImplementation(libs.androidx.datastore)
}
