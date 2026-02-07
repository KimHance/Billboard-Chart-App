plugins {
    alias(libs.plugins.billboard.android.aplication)
    alias(libs.plugins.billboard.android.aplication.compse)
    alias(libs.plugins.billboard.android.hilt)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.hancekim.billboard"

    defaultConfig {
        applicationId = "com.hancekim.billboard"
        versionCode = libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()
        testInstrumentationRunner = "com.hancekim.billboard.core.testing.BillboardTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
}

dependencies {
    implementation(projects.core.designSystem)
    implementation(projects.core.imageLoader)
    implementation(projects.core.circuit)
    implementation(projects.core.domain)
    implementation(libs.timber)
    implementation(libs.androidx.activity.compose)

    implementation(projects.feature.splash)
    implementation(projects.feature.home)
    implementation(projects.feature.setting)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(projects.core.dataTest)

    baselineProfile(projects.benchmark)
}

baselineProfile {
    automaticGenerationDuringBuild = false
    dexLayoutOptimization = true
}
