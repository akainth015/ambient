import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "0.4.11"
    id("org.jetbrains.dokka") version "0.10.1"
    java
    kotlin("jvm") version "1.3.50"
}

group = "me.akainth"
version = "2.1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    @Suppress("SpellCheckingInspection")
    implementation("com.squareup.okhttp3", "okhttp", "4.1.0")
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("junit", "junit", "4.12")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    setPlugins("java")
}
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""Ambient is now compiled for IntelliJ 2020.2. SyncedTreeViews will update in the background to avoid freezing the IDE.""")
}
tasks.getByName<org.jetbrains.intellij.tasks.PublishTask>("publishPlugin") {
    token(System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken"))
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.getByName<org.jetbrains.dokka.gradle.DokkaTask>("dokka") {
    outputFormat = "html"
    outputDirectory = "$buildDir/dokka"
}
