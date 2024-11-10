import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij.platform") version "2.1.0"
    java
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "me.akainth"
version = "24.4"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("com.squareup.okhttp3", "okhttp", "4.9.0")
    intellijPlatform {
        intellijIdeaCommunity("2024.2.3")
        bundledPlugin("com.intellij.java")

        pluginVerifier()
        zipSigner()
        instrumentationTools()
    }
}

tasks.patchPluginXml {
    untilBuild = provider { null }
}

tasks.publishPlugin {
    val publishToken = System.getenv("PUBLISH_TOKEN")
    token.set(publishToken)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    toolchain {
        sourceCompatibility = JavaVersion.VERSION_21
    }
}

tasks.compileJava {
    options.compilerArgs.addLast("-Xlint:deprecation")
}