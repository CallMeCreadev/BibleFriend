// build.gradle.kts (Project-level)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Add Safe Args plugin classpath
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}
