pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kmp-contact"

include(
    ":kmp:domain",
    ":kmp:data",
    ":android:contacts",
    ":android:main",
    ":ios:contacts",
    ":web:contacts",
)
