import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "0.4.26"
    id("org.jetbrains.dokka") version "0.10.1"
    java
    kotlin("jvm") version "1.3.70"
}

group = "me.akainth"
version = "2.2.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    @Suppress("SpellCheckingInspection")
    implementation("com.squareup.okhttp3", "okhttp", "4.9.0")
    implementation(kotlin("reflect", "1.4.0"))
    testImplementation("junit", "junit", "4.12")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    setPlugins("java")
}
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.patchPluginXml {
    changeNotes("""Ambient is now compiled for IntelliJ 2020.3.""")
}
tasks.publishPlugin {
    token(System.getenv("PUBLISH_TOKEN"))
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.getByName<org.jetbrains.dokka.gradle.DokkaTask>("dokka") {
    outputFormat = "html"
    outputDirectory = "$buildDir/dokka"
}
