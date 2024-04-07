import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "1.17.3"
    java
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "me.akainth"
version = "24.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3", "okhttp", "4.9.0")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2024.1")
    plugins.set(listOf("java"))
}

tasks.publishPlugin {
    val publishToken = System.getenv("PUBLISH_TOKEN")
    token.set(publishToken)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

java {
    toolchain {
        sourceCompatibility = JavaVersion.VERSION_17
    }
}

tasks.compileJava {
    options.compilerArgs.addLast("-Xlint:deprecation")
}