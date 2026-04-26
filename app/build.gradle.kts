plugins {
    alias(libs.plugins.billboard.android.application)
    alias(libs.plugins.billboard.android.application.compose)
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
            isMinifyEnabled = true
            isShrinkResources = true
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
    implementation(libs.androidx.core.splash)

    implementation(projects.feature.splash)
    implementation(projects.feature.home)
    implementation(projects.feature.setting)
    implementation(projects.feature.collection)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(projects.core.dataTest)

    baselineProfile(projects.benchmark)
}

baselineProfile {
    automaticGenerationDuringBuild = false
    dexLayoutOptimization = true
}

// demo에서 수집한 베이스라인 프로파일을 src/main에 복사하여 prodRelease에도 적용
tasks.register<Copy>("copyBaselineProfileToMain") {
    from("src/demoRelease/generated/baselineProfiles")
    into("src/main/generated/baselineProfiles")
}

// generateDemoBaselineProfile 실행 후 자동으로 main에 복사
tasks.matching { it.name == "generateDemoBaselineProfile" }.configureEach {
    finalizedBy("copyBaselineProfileToMain")
}
