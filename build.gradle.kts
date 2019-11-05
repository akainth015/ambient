import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "0.4.10"
    java
    kotlin("jvm") version "1.3.50"
}

group = "me.akainth"
version = "2.0.4"

repositories {
    mavenCentral()
}

dependencies {
    @Suppress("SpellCheckingInspection")
    implementation("com.squareup.okhttp3", "okhttp", "4.1.0")
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    setPlugins("java")
    version = "LATEST-EAP-SNAPSHOT"
}
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""Snarfed projects are now compatible with Eclipse""")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
