plugins {
    id("org.jetbrains.intellij") version "1.3.1"
    id("org.jetbrains.dokka") version "1.6.10"
    java
    kotlin("jvm") version "1.6.10"
}

group = "me.akainth"
version = "2.3.2"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("com.squareup.okhttp3", "okhttp", "4.9.0")
    testImplementation("junit", "junit", "4.12")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("LATEST-EAP-SNAPSHOT")
    plugins.set(listOf("java"))
}

tasks.publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
