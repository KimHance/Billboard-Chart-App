plugins {
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.billboard.android.test)
}

android {
    namespace = "com.hancekim.billboard.benchmark"

    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions.managedDevices {
        localDevices {
            create("pixel6Api32") {
                device = "Pixel 6"
                apiLevel = 32
                systemImageSource = "aosp"
            }
            create("pixel6Api36") {
                device = "Pixel 6"
                apiLevel = 36
                systemImageSource = "aosp"
            }
        }
        groups {
            create("baselineProfileDevices") {
                targetDevices += allDevices["pixel6Api32"]
                targetDevices += allDevices["pixel6Api36"]
            }
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

baselineProfile {
    managedDevices.clear()
    managedDevices += "pixel6Api32"
    managedDevices += "pixel6Api36"
    useConnectedDevices = false
}


androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}