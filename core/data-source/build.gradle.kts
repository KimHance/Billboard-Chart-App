import java.util.Properties

plugins {
    alias(libs.plugins.billboard.android.library)
    alias(libs.plugins.billboard.android.hilt)
}

val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties().apply {
    if (secretsFile.exists()) load(secretsFile.inputStream())
}

android {
    namespace = "com.hancekim.billboard.core.datasource"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "YOUTUBE_API_KEY", "\"${secrets.getProperty("YOUTUBE_API_KEY", "")}\"")
    }
}

dependencies {
    implementation(projects.core.data)
    prodImplementation(projects.core.network)
    prodImplementation(libs.retrofit.core)
    prodImplementation(libs.androidx.datastore)
}
