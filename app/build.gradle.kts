plugins {
    alias(libs.plugins.billboard.android.aplication)
    alias(libs.plugins.billboard.android.aplication.compse)
    alias(libs.plugins.billboard.android.hilt)
}

android {
    namespace = "com.hancekim.billboard"

    defaultConfig {
        applicationId = "com.hancekim.billboard"
        versionCode = libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(projects.core.designSystem)
    implementation(projects.core.imageLoader)
    implementation(projects.core.network)
    implementation(libs.timber)
    implementation(libs.androidx.activity.compose)

    implementation(projects.feature.splash)
}