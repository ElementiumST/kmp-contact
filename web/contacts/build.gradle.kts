import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    base
}

val npmCommand = when {
    System.getProperty("os.name").startsWith("Windows") && File("C:/Program Files/nodejs/npm.cmd").exists() -> {
        "C:/Program Files/nodejs/npm.cmd"
    }

    System.getProperty("os.name").startsWith("Windows") -> "npm.cmd"
    else -> "npm"
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "kotlin-runtime.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":kmp:data"))
            implementation(project(":kmp:domain"))
            implementation(project(":kmp:support"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

val syncKotlinRuntime by tasks.registering(Copy::class) {
    group = "web"
    description = "Copy Kotlin/JS runtime bundle for Vite."
    dependsOn("jsBrowserProductionWebpack")
    from(layout.buildDirectory.dir("kotlin-webpack/js/productionExecutable"))
    include("*.js")
    rename { "kotlin-runtime.js" }
    into(layout.projectDirectory.dir("public"))
}

val installFrontend by tasks.registering(Exec::class) {
    group = "web"
    description = "Install React/TypeScript frontend dependencies."
    workingDir = projectDir
    commandLine(npmCommand, "install")
}

val buildFrontend by tasks.registering(Exec::class) {
    group = "web"
    description = "Build the React/TypeScript frontend."
    workingDir = projectDir
    commandLine(npmCommand, "run", "build")
    dependsOn(syncKotlinRuntime)
    dependsOn(installFrontend)
}

tasks.named("build") {
    dependsOn(buildFrontend)
}

tasks.named("clean") {
    doLast {
        delete(layout.projectDirectory.dir("dist"))
        delete(layout.projectDirectory.file("public/kotlin-runtime.js"))
    }
}
