plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    js(IR) {
        browser()
    }

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
