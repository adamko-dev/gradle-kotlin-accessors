[![Maven Central](https://img.shields.io/maven-central/v/dev.adamko.gradle.kotlinaccessors/settings-plugin?style=for-the-badge)](https://central.sonatype.com/artifact/dev.adamko.gradle.kotlinaccessors/settings-plugin)

# Gradle Kotlin Delegate Accessors

This library adds Kotlin `by` delegated accessors for easier, succinct, and safer access to Gradle
objects.

## Why delegates?

- The name comes from the property, so it cannot drift from the thing it names.
- Two properties cannot share a name in a scope, so duplicate registrations are impossible.
- Renaming one is a refactor the compiler checks, not a search-and-replace.

## Install

```kotlin
// settings.gradle.kts
plugins {
  id("dev.adamko.gradle-kotlin-accessors") version "main-SNAPSHOT"
}
```

Every build script in the build, and in `buildSrc`, can then use the accessors (with no need to import).
An included build, such as `build-logic`, needs the plugin applied in its own `settings.gradle.kts`.

## Usage

```kotlin
// build.gradle.kts

// tasks: reference one that already exists, or register a new one lazily
val jar by tasks.existing
val rebuild by tasks.registering {
  dependsOn("clean", jar)
}

// ...with a type, when the type matters
val test by tasks.existing(Test::class)
val sourcesJar by tasks.registering(Jar::class) {
  archiveClassifier = "sources"
}

// any other container works the same way
val main by sourceSets.getting
val integrationTest by sourceSets.registering

// configurations, in the role they are meant to have
val internalApi by configurations.dependencyScope
val internalApiClasspath by configurations.resolvable {
  extendsFrom(internalApi.get())
}
val internalApiElements by configurations.consumable

// extensions, by the name they are registered under
val java: JavaPluginExtension by extensions
```

## In plugins

Convention and binary plugins are compiled, not scripted, so they need the accessors as a dependency.
In a project that applies `kotlin-dsl`, the settings plugin adds it:

```kotlin
// buildSrc/settings.gradle.kts (or build-logic/settings.gradle.kts, or wherever the plugins are built)
plugins {
  id("dev.adamko.gradle-kotlin-accessors") version "main-SNAPSHOT"
}
```

Any other project declares it from the `gradleKotlinAccessorsLibs` version catalog, which the
settings plugin also adds:

```kotlin
// build.gradle.kts
dependencies {
  implementation(gradleKotlinAccessorsLibs.accessors)
}
```

Precompiled script plugins need no imports, even in a package. Other code in a package imports the
accessors by their simple names, plus the two operators `by` resolves to:

```kotlin
package com.example

import org.gradle.api.*
import dev.adamko.gradle.kotlin.dsl.*

// the task names are tracked in a binary-compatibility-validator ABI dump
class ExamplePluginTasks(project: Project) {
  val printHelloWorld by project.tasks.registering {
    doLast {
      println("Hello, world!")
    }
  }
}
```

The precompiled script plugin imports are injected through Gradle internals, on Gradle 9.6 up to 10.
To turn them off:

```kotlin
// settings.gradle.kts
import dev.adamko.gradle.kotlinaccessors.AccessorsSettingsExtension.ImplicitImportsMode

gradleKotlinAccessors {
  implicitImports = ImplicitImportsMode.Disabled
}
```

Compiled code can also be told when it drops an accessor result (e.g. calling `tasks.registering()` is a no-op)
by enabling [unused return value checker](https://kotlinlang.org/docs/unused-return-value-checker.html):

```kotlin
kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xreturn-value-checker=check")
  }
}
```

(Unused return value checker is not supported by `build.gradle.kts` yet https://github.com/gradle/gradle/issues/39225).
