plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.hiltAndroid)
}

kotlin {
    jvmToolchain(17)
}

kapt {
    correctErrorTypes = true
}

android {
    namespace = "com.stark.kmpcontact.android.contacts"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
        buildConfigField("String", "SERVER_URL", "\"https://alpha.hi-tech.org/api/rest\"")
        buildConfigField("String", "AUTH_LOGIN", "\"mobileuser3@testivcs.su\"")
        buildConfigField("String", "AUTH_PASSWORD", "\"test\"")
        buildConfigField("boolean", "AUTH_REMEMBER_ME", "false")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":kmp:domain"))
    implementation(project(":kmp:data"))
    implementation(project(":kmp:support"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.okhttp)
    implementation(libs.gson)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)

    kapt(libs.hilt.compiler)

    debugImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
}
