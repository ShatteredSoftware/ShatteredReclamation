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