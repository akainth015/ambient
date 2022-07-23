plugins {
    id("org.jetbrains.intellij") version "1.3.1"
    java
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.dokka") version "1.6.10"
}

group = "me.akainth"
version = "22.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3", "okhttp", "4.9.0")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2022.1.4")
    plugins.set(listOf("java"))
}

tasks.publishPlugin {
    val publishToken = System.getenv("PUBLISH_TOKEN")
    token.set(publishToken)
}

java {
    toolchain {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
}
