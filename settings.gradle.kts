pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
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
