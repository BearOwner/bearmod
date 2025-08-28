plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

// No dependencies needed here. Avoid adding SonarQube or AGP to buildSrc to prevent classpath conflicts.