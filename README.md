<h1 align="center">Example Kotlin Plugin</h1>
<p align="center">
<img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/ShatteredSoftware/KotlinPlugin/prerelease?label=Prerelease&style=for-the-badge">
<img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/ShatteredSoftware/KotlinPlugin/tagged-release?label=Release&style=for-the-badge">
</p>
<hr>

This is an example Kotlin plugin for Spigot that has gradle set up already so that dependencies can be split between 
multiple jars, either to use LibraryLoader, or to include them in the generated jar.

## Gradle
Much of what this repo does can be configured in the `gradle.properties`:
```properties
kotlin.code.style=official

baseName=KotlinPlugin
baseGroup=com.github.shatteredsoftware
minimumApiVersion=1.17
description=A really simple Kotlin plugin.

baseVersion=1.0.0

kotlinVersion=1.5.30
```

### `plugin.yml`
This will generate a `plugin.yml` like so:
```yaml
name: KotlinPlugin
main: com.github.shatteredsoftware.kotlinplugin.KotlinPlugin
version: 1.0.0-abcdefgh
description: A really simple Kotlin plugin.
api-version: 1.17
author: UberPilot
libraries:
  - "org.jetbrains.kotlin:kotlin-reflect:1.5.30"
  - "org.jetbrains.kotlin:kotlin-stdlib:1.5.30"
```

Version is pulled from the current git hash and `baseVersion`. Main is built from a combination of `baseName` and 
`baseGroup`.

### Jars

Two jars are produced: `name-version-hash.jar` and `name-version-hash-legacy.jar`. The first one uses the new 
LibraryLoader to load Kotlin in at runtime; the other includes Kotlin in the jar for older versions that don't have
LibraryLoader.

## GitHub Actions
This repo also has GitHub actions set up in a way that builds the plugin jars on each commit, and automatically creates
releases when they're tagged with a version matching `#.#.#`. 

## FAQ

### How do I add external libraries?

1. Add the library's repository to the `repositories` block.
2. Add the library to the dependencies block.
   * If the plugin is intended to be included in none of the jars (it's given at runtime), add it to the 
     `implementation` configuration.
   * If the plugin is intended to be loaded with Library Loader, add it to the `includeNonLibraryLoader` configuration.
   * If the plugin is intended to be included in all of the jars, add it to the `includeAll` configuration.

<summary>
<strong>A more detailed example using bStats:</strong>
<details>
<ol>
   <li>Add the bStats repo to the <code>repositories</code> block:
      <pre>
repositories {
    mavenLocal()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://oss.sonatype.org/content/repositories/central")
+   maven(url = "https://hub.spigotmc.org/nexus/content/groups/public/")
    mavenCentral()
}</pre>
   </li>
   <li>Add bStats to the <code>dependencies</code> block:
   <pre>
dependencies {
    implementation("org.spigotmc:spigot-api:$minimumApiVersion-R0.1-SNAPSHOT")
+   includeAll("org.bstats:bstats:2.2.1")
    includeNonLibraryLoader("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    includeNonLibraryLoader("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}</pre>
   I chose <code>includeAll</code> because I don't want to load bStats with Library Loader, but instead want to include
   it in my plugin.
   </li>
   <li>Freely use bStats in your plugin after refreshing your gradle project.</li>
</ol>
</details>
</summary>

### Do I need to set up shading?

Shading is configured automatically to shade everything based on the base package name. If your needs are more specific,
you can disable the `relocateFullJar` and `relocateMainJar` tasks and instead configure things using `relocate()` calls
in either of the `fullJar` and `mainJar` tasks, like so, using bStats as an example:

```kotlin
val mainJar: TaskProvider<ShadowJar> = tasks.register<ShadowJar>("mainJar") {
    dependsOn(relocateMainJar)
    from(sourceSets.main.get().output)
    archiveClassifier.set("")
    configurations = listOf(includeAll)
    relocate("org.bstats", "$basePackage.bstats")
}
```
