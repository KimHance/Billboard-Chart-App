plugins {
    alias(libs.plugins.billboard.android.library)
    alias(libs.plugins.billboard.android.hilt)
}

android {
    namespace = "com.hancekim.billboard.core.testing"
}

dependencies {
    api(libs.androidx.test.runner)
    api(libs.hilt.android.testing)
    api(libs.kotlinx.coroutines.test)

    implementation(libs.junit)
}
