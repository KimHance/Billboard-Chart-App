import java.util.Properties

plugins {
    alias(libs.plugins.billboard.android.feature)
    alias(libs.plugins.billboard.android.hilt)
    alias(libs.plugins.billboard.circuit)
    alias(libs.plugins.kotlin.serialization)
}

// local.properties 는 gradle 이 자동으로 읽지 않으므로 직접 로드.
// gitignore 처리되어 있어 키가 절대 git 에 들어가지 않음.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.hancekim.billboard.feature.agentchat"

    defaultConfig {
        // 우선순위: local.properties → 환경 변수 → 빈 값.
        // 빈 값이면 Presenter 가 가드해서 안내 메시지를 채팅으로 표시.
        val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY")
            ?: System.getenv("GEMINI_API_KEY")
            ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // 우리 AppFunction 진입점 - BillboardFunctions 가 :app 에 있어 직접 의존 불가.
    // 대신 :core:domain UseCase 를 직접 사용 (BillboardFunctions 와 동일한 데이터 경로).
    implementation(projects.core.designFoundation)
    implementation(libs.androidx.appfunctions)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // ADK (Agent Development Kit) — multi-agent + @Tool annotation processor.
    implementation(libs.adk.core.android)
    ksp(libs.adk.processor)
}
