import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.BufferedReader

plugins {
    kotlin("jvm") version "1.5.30"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

// Get the current Git commit to be added to the version -- if you're not using Git, you should be!
var commitHash: String by extra
commitHash = Runtime
    .getRuntime()
    .exec("git rev-parse --short HEAD")
    .let { process ->
        process.waitFor()
        val output = process.inputStream.use {
            it.bufferedReader().use(BufferedReader::readText)
        }
        process.destroy()
        output.trim()
    }

// Project properties
val baseName: String by project
val baseGroup: String by project
val baseVersion: String by project
val kotlinVersion: String by project
val minimumApiVersion: String by project
val description: String by project
val basePackage = "$baseGroup.${baseName.toLowerCase()}"

version = "$baseVersion-$commitHash"
group = baseGroup

// Set up configurations for Shadow to use.
val includeAll = configurations.create("includeAll")

val includeNonLibraryLoader = configurations.create("includeNonLibraryLoader")
includeNonLibraryLoader.extendsFrom(includeAll)

val implementationConfiguration = configurations.getByName("implementation")
implementationConfiguration.extendsFrom(includeNonLibraryLoader)

// Declare repositories so we can get the libraries we need during development.
repositories {
    mavenLocal()
    maven(
        url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
    )
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://oss.sonatype.org/content/repositories/central")
    mavenCentral()
}

// Declare dependencies using the above configurations
dependencies {
    implementation("org.spigotmc:spigot-api:$minimumApiVersion-R0.1-SNAPSHOT")
    includeNonLibraryLoader("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    includeNonLibraryLoader("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}

// Set up a task that includes all dependencies, including those that would be loaded with library loader.
val relocateFullJar = tasks.register<ConfigureShadowRelocation>("relocateFullJar") {
    target = fullJar.get()
    prefix = "$basePackage.include"
}

val fullJar: TaskProvider<ShadowJar> = tasks.register<ShadowJar>("fullJar") {
    dependsOn(relocateFullJar)
    from(sourceSets.main.get().output)
    archiveClassifier.set("legacy")
    configurations = listOf(includeNonLibraryLoader)
}

// Set up a task that includes all dependencies except those that would be loaded with library loader.
val relocateMainJar = tasks.register<ConfigureShadowRelocation>("relocateMainJar") {
    target = mainJar.get()
    prefix = "$basePackage.include"
}

val mainJar: TaskProvider<ShadowJar> = tasks.register<ShadowJar>("mainJar") {
    dependsOn(relocateMainJar)
    from(sourceSets.main.get().output)
    archiveClassifier.set("")
    configurations = listOf(includeAll)
}


// Fix placeholders in resource files (see plugin.yml)
tasks.processResources {
    expand(
        "baseName" to baseName,
        "baseGroup" to baseGroup,
        "basePackage" to basePackage,
        "baseVersion" to baseVersion,
        "description" to description,
        "commit" to commitHash,
        "version" to version,
        "kotlinVersion" to kotlinVersion,
        "apiVersion" to minimumApiVersion
    )
}

// Build both jars in the new "jars" task
tasks.register("jars") {
    dependsOn(setOf("fullJar", "mainJar"))
}

// Use the above jars task when we request a jar.
tasks.jar {
    dependsOn("jars")
}

// Fixes IntelliJ not getting test results.
tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}