plugins {
    alias(libs.plugins.billboard.android.library)
}

android {
    namespace = "com.hancekim.billboard.core.domain"
}

dependencies {
    implementation(projects.core.data)
    implementation(libs.javax.inject)
    runtimeOnly(projects.core.dataImpl)
}