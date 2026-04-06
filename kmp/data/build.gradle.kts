import com.android.build.gradle.LibraryExtension
import org.gradle.kotlin.dsl.configure

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":kmp:domain"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

configure<LibraryExtension> {
    namespace = "com.stark.kmpcontact.data"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
