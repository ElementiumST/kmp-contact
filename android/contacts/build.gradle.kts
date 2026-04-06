import com.android.build.gradle.LibraryExtension
import org.gradle.kotlin.dsl.configure

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":kmp:domain"))
            implementation(project(":kmp:data"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

configure<LibraryExtension> {
    namespace = "com.stark.kmpcontact.android.contacts"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }
}
