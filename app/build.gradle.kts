plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.6.10"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

val geojsonJacksonVersion: String by project
val graphhopperVersion: String by project
val jtsVersion: String by project
val kotlinLoggingVersion: String by project
val kotlinSerializationVersion: String by project
val kotlinxCoroutinesVersion: String by project
val kotlinxCliVersion: String by project
val logbackVersion: String by project
val parallelPbfVersion: String by project
dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")

    // logging
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // geo utils
    implementation("de.grundid.opendatalab:geojson-jackson:$geojsonJacksonVersion")
    implementation("org.locationtech.jts:jts-core:$jtsVersion")
    implementation("org.locationtech.jts:jts:$jtsVersion")

    // graphhopper
    implementation("com.graphhopper:graphhopper-core:$graphhopperVersion")

    // misc
    implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinxCliVersion")
}

application {
    mainClass.set("kkris.hatschen.MainKt")
}
