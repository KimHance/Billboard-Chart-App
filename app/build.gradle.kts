plugins {
    id("hance.android.application.compose")
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
    implementation(libs.androidx.activity.compose)
}