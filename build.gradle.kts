import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "1.11.0"
    java
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"
}

group = "me.akainth"
version = "22.3.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3", "okhttp", "4.9.0")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2022.3.1")
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
