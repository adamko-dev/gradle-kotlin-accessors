@file:Suppress("UnstableApiUsage")

import kotlin.io.path.*
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.VERIFICATION
import org.gradle.api.attributes.DocsType.DOCS_TYPE_ATTRIBUTE
import org.gradle.api.attributes.DocsType.SOURCES
import org.gradle.api.attributes.VerificationType.MAIN_SOURCES
import org.gradle.api.attributes.VerificationType.VERIFICATION_TYPE_ATTRIBUTE
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
  id("buildsrc.conventions.kotlin-library")
}

description = """
    Gradle Kotlin DSL delegated-property accessors.
    (Defined in the root package so they are automatically available in `build.gradle.kts`.)
  """.trimIndent().lines().joinToString(" ")

dependencies {
  implementation(project(":modules:accessors-core"))

  compileOnly(gradleApi())
  compileOnly(gradleKotlinDsl())

  testFixturesCompileOnly(gradleApi())
  testFixturesCompileOnly(gradleKotlinDsl())
  testFixturesApi(platform(libs.junit.bom))
  testFixturesApi(libs.junit.jupiter)
}

val projectSources by configurations.dependencyScope {
  description = "The sources of a project."
  defaultDependencies {
    add(project.dependencies.project(":modules:accessors-dsl"))
  }
}

val projectSourcesResolver by configurations.resolvable {
  description = "Resolves ${projectSources.name}."
  extendsFrom(projectSources)
  isTransitive = false
  attributes {
    attribute(CATEGORY_ATTRIBUTE, objects.named(VERIFICATION))
    attribute(DOCS_TYPE_ATTRIBUTE, objects.named(SOURCES))
    attribute(VERIFICATION_TYPE_ATTRIBUTE, objects.named(MAIN_SOURCES))
  }
}

val prepSources by tasks.registering {
  group = project.name
  description =
    "Copies sources from ${projects.modules.accessorsDsl.name}, removing the `package org.gradle.kotlin.dsl` declaration. " +
        "Sources are copied so they are kept in sync."
  val fs = serviceOf<FileSystemOperations>()

  val outputDir = layout.buildDirectory.dir("generated-sources/main/kotlin")
  outputs.dir(outputDir)

  val projectSources = projectSourcesResolver.map { it.incoming.files }
  inputs.files(projectSources)
    .withPropertyName("projectSources")
    .withPathSensitivity(PathSensitivity.RELATIVE)
    .normalizeLineEndings()

  doLast {
    val rootKtExtension = ".root.kt"
    val kotlinDslPackage = "package org.gradle.kotlin.dsl"
    fs.sync {
      from(projectSources)
      into(outputDir)
      rename { it.replace(".dsl.kt", rootKtExtension) }

      // Exclude the new scoped configurations.
      // Gradle doesn't have them, so they don't need to be in the root package to get prioritised.
      exclude("**/ConfigurationContainerExtensions.*.kt")
    }
    outputDir.get().asFile.toPath().walk()
      .filter { it.isRegularFile() }
      .filter { it.name.endsWith(rootKtExtension) }
      .forEach { file ->
        val source = file.readText()
        val rootSource = source.replace(Regex("""\R${Regex.escape(kotlinDslPackage)}\R"""), "")
        check(rootSource != source) { "No `package org.gradle.kotlin.dsl` declaration found in $file" }
        file.writeText(rootSource)
      }
  }
}

kotlin.sourceSets.main {
  kotlin.srcDir(prepSources)
}

testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      useJUnitJupiter(libs.versions.junit)
    }

    // Unit tests: the accessors driven directly, against real containers from ProjectBuilder.
    val test by getting(JvmTestSuite::class) {
      dependencies {
        implementation(gradleApi())
        implementation(gradleKotlinDsl())
      }

      targets.configureEach {
        testTask.configure {
          // Required by ProjectBuilder
          jvmArgs(
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED"
          )
        }
      }
    }
  }
}
