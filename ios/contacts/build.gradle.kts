plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

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
