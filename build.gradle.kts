import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.BufferedReader

plugins {
    kotlin("jvm") version "1.5.30"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

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

val baseName: String by project
val baseGroup: String by project
val basePackage = "$baseGroup.${baseName.toLowerCase()}"
val baseVersion: String by project
val kotlinVersion: String by project
val minimumApiVersion: String by project
val description: String by project

version = "$baseVersion-$commitHash"
group = baseGroup


repositories {
    mavenLocal()
    maven(
        url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
    )
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://oss.sonatype.org/content/repositories/central")
    mavenCentral()
}

val includeAll = configurations.create("includeAll")

val includeNonLibraryLoader = configurations.create("includeNonLibraryLoader")
includeNonLibraryLoader.extendsFrom(includeAll)

val implementationConfiguration = configurations.getByName("implementation")
implementationConfiguration.extendsFrom(includeNonLibraryLoader)

dependencies {
    implementation("org.spigotmc:spigot-api:$minimumApiVersion-R0.1-SNAPSHOT")
    includeNonLibraryLoader("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    includeNonLibraryLoader("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}

tasks.register<ShadowJar>("fullJar") {
    from(sourceSets.main.get().output)
    archiveClassifier.set("legacy")
    configurations = listOf(includeNonLibraryLoader)

}

tasks.register<ShadowJar>("mainJar") {
    from(sourceSets.main.get().output)
    archiveClassifier.set("")
    configurations = listOf(includeAll)
}

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

tasks.register("jars") {
    dependsOn(setOf("fullJar", "mainJar"))
}

tasks.jar {
    dependsOn("jars")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}