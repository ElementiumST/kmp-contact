plugins {
    base
}

import java.io.File

val npmCommand = when {
    System.getProperty("os.name").startsWith("Windows") && File("C:/Program Files/nodejs/npm.cmd").exists() -> {
        "C:/Program Files/nodejs/npm.cmd"
    }

    System.getProperty("os.name").startsWith("Windows") -> "npm.cmd"
    else -> "npm"
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
    dependsOn(installFrontend)
}

tasks.named("build") {
    dependsOn(buildFrontend)
}

tasks.named("clean") {
    doLast {
        delete(layout.projectDirectory.dir("dist"))
    }
}
