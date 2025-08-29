// Archivo: build.gradle.kts (Project-level)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15") // 🔥 Firebase Plugin
        classpath ("com.android.tools.build:gradle:7.4.1")
    }
    repositories {
        google()
        mavenCentral()
    }
}
