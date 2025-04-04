import java.io.FileInputStream
import java.util.Properties

val mcpVersion = "0.4.0"
val slf4jVersion = "2.0.9"
val anthropicVersion = "0.8.0"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

// local.properties 파일에서 API 키 읽기
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.mcp_test"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mcp_test"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // API 키를 BuildConfig 필드로 추가
        buildConfigField("String", "ANTHROPIC_API_KEY", "\"${localProperties.getProperty("anthropicApiKey")}\"")
    }
    
    buildFeatures {
        buildConfig = true
        compose = true
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
            // 필요한 경우 다음과 같이 추가 제외할 수 있습니다:
            // excludes += '/META-INF/LICENSE'
            // excludes += '/META-INF/LICENSE.txt'
            // excludes += '/META-INF/NOTICE'
        }
    }
}

// 종속성 충돌 해결을 위한 설정
configurations.all {
    resolutionStrategy {
        // 모든 kotlin-logging 관련 의존성을 특정 버전으로 강제
        force("io.github.oshai:kotlin-logging-jvm:5.1.0")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("io.modelcontextprotocol:kotlin-sdk:$mcpVersion")
    implementation("org.slf4j:slf4j-nop:$slf4jVersion")
    implementation("com.anthropic:anthropic-java:$anthropicVersion")

    // HTTP 클라이언트 의존성 (OkHttp 또는 Ktor 사용)
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.10.0")

    // Ktor 클라이언트 의존성 추가
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.plugins)
    implementation(libs.jackson.module.kotlin)

    // Jackson Kotlin 모듈 추가
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.1")

    // Jackson Java 8 데이터 타입 모듈 추가
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.18.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.1")
}
