@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalPathApi::class)

import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText

plugins {
  id("buildsrc.conventions.kotlin-jvm")
  id("dev.adamko.dev-publish")
}

description = "Integration tests."

dependencies {
  devPublication(project(":modules:accessors"))
  devPublication(project(":modules:accessors-core"))
  devPublication(project(":modules:accessors-dsl"))
  devPublication(project(":modules:accessors-root"))
  devPublication(project(":modules:settings-plugin"))

  implementation(gradleTestKit())
  implementation(platform(libs.junit.bom))
  implementation(libs.junit.jupiter)

  implementation(devPublish.dependency())
  testImplementation(devPublish.dependency())
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter(libs.versions.junit)

      targets.configureEach {
        testTask.configure {
          dependsOn(tasks.updateDevRepo)

          val projectVersion: Provider<String> = providers.provider { project.version.toString() }
          inputs.property("projectVersion", projectVersion)

          systemProperty("junit.jupiter.tempdir.cleanup.mode.default", "ON_SUCCESS")
          systemProperty("junit.jupiter.execution.timeout.testtemplate.invocation.default", "10m")
        }
      }
    }
  }
}

val generateTestResources by tasks.registering {
  val outputDir = temporaryDir.toPath()
  outputs.dir(outputDir)

  val projectVersion: Provider<String> = providers.provider { project.version.toString() }
  inputs.property("projectVersion", projectVersion)

  outputs.doNotCacheIf("never cache - simple task") { true }

  doLast {
    outputDir.deleteRecursively()
    outputDir.createDirectories()
    outputDir.resolve("dev.adamko.gradle.kotlinaccessors.test-resources.properties")
      .writeText("projectVersion=${projectVersion.get()}")
  }
}

kotlin.sourceSets.test {
  resources.srcDir(generateTestResources)
}
