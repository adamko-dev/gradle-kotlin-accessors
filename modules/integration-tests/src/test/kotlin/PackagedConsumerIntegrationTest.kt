/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Consumers whose Kotlin lives in a package rather than in a build script:
 * binary plugins and precompiled script plugins.
 */
class PackagedConsumerIntegrationTest {

  @AccessorsTest
  fun `unused return value triggers warning in a binary plugin`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writeSettingsScript("""rootProject.name = "consumer"""")
    project.writeBuildScript(
      """
      |plugins {
      |  `kotlin-dsl`
      |}
      |
      |repositories {
      |  mavenCentral()
      |  maven { url = uri("${AccessorsTestProject.devMavenRepo}") }
      |}
      |
      |dependencies {
      |  implementation("${AccessorsTestProject.accessorsCoordinates}")
      |}
      |
      |kotlin {
      |  compilerOptions {
      |    freeCompilerArgs.add("-Xreturn-value-checker=check")
      |  }
      |}
      |""".trimMargin()
    )

    // a real plugin lives in a package, so it reaches the root-package accessors by importing them
    // under their simple names, and needs `provideDelegate` and `getValue` imported for `by`
    project.write(
      "src/main/kotlin/com/example/demo.kt",
      """
      |package com.example
      |
      |import org.gradle.api.Project
      |import existing
      |import getValue
      |import provideDelegate
      |import registering
      |
      |fun Project.demo() {
      |  // `by` works from a package, given the operators are imported
      |  val alpha by tasks.registering
      |  alpha.configure { group = "demo" }
      |
      |  // the result is dropped, so this configures nothing
      |  tasks.existing { group = "demo" }
      |}
      |""".trimMargin()
    )

    val result = project.runner("compileKotlin", "--stacktrace", "--no-build-cache").build()

    assertTrue(
      gradle.kotlin.unusedReturnValue.containsMatchIn(result.output),
      "Expected the return value checker to flag the dropped result on Gradle $gradle:\n${result.output}"
    )
  }

  /**
   * A precompiled script plugin is compiled by the Kotlin Gradle Plugin, like a binary plugin.
   *
   * `import registering` only resolves to the root package, so the script compiles with this
   * library and fails without it. Neither checker warnings nor deprecation nags surface here.
   */
  @AccessorsTest
  fun `a precompiled script plugin can import the accessors`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writePrecompiledScriptPlugin(withLibrary = true)
    val compiled = project.runner("compileKotlin", "--warning-mode=all", "--no-build-cache").build()
    assertEquals(TaskOutcome.SUCCESS, compiled.task(":compileKotlin")?.outcome)

    project.writePrecompiledScriptPlugin(withLibrary = false)
    val failed = project.runner("compileKotlin", "--warning-mode=all").buildAndFail()
    assertTrue(
      gradle.kotlin.unresolvedReference.containsMatchIn(failed.output),
      "Expected the imports to have nothing to resolve to on Gradle $gradle:\n${failed.output}"
    )
  }

  private fun AccessorsTestProject.writePrecompiledScriptPlugin(withLibrary: Boolean) {
    writeSettingsScript("""rootProject.name = "consumer"""")
    val dependency = if (withLibrary) """dependencies { implementation("${AccessorsTestProject.accessorsCoordinates}") }""" else ""
    writeBuildScript(
      """
      |plugins {
      |  `kotlin-dsl`
      |}
      |
      |repositories {
      |  mavenCentral()
      |  maven { url = uri("${AccessorsTestProject.devMavenRepo}") }
      |}
      |
      |$dependency
      |""".trimMargin()
    )

    write(
      "src/main/kotlin/com/example/demo-convention.gradle.kts",
      """
      |import getValue
      |import provideDelegate
      |import registering
      |
      |val alpha by tasks.registering
      |
      |tasks.register("verifyAccessor") {
      |  val accessorResult = alpha.name
      |  doLast { println("ACCESSOR OK: " + accessorResult) }
      |}
      |""".trimMargin()
    )

  }

  /**
   * The documented setup: settings plugin, `accessors` dependency, and a precompiled script plugin
   * in a package. Gradle's accessors would compile too, so the plugin is run to check for the nag.
   */
  @AccessorsTest
  fun `a precompiled script plugin in a package needs no imports`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writePluginBuild()
    project.write(
      "buildSrc/src/main/kotlin/com/example/demo-convention.gradle.kts",
      """
      |package com.example
      |
      |${RESOLVER_ENVIRONMENT_LAYERS}
      |
      |// deliberately no imports
      |val alpha by tasks.registering
      |
      |tasks.register("verifyAccessor") {
      |  val accessorResult = alpha.name
      |  doLast { println("ACCESSOR OK: " + accessorResult) }
      |}
      |""".trimMargin()
    )
    project.writeBuildScript("""plugins { id("com.example.demo-convention") }""")

    val result = project.runner("verifyAccessor", "--warning-mode=all").build()

    assertTrue(
      result.output.contains("ACCESSOR OK: alpha"),
      "Expected the convention plugin to resolve the accessors on Gradle $gradle:\n${result.output}"
    )
    result.assertNoDeprecationNagging(gradle)
  }

  /** Applying `kotlin-dsl` is enough: the settings plugin adds the `accessors` dependency. */
  @AccessorsTest
  fun `a precompiled script plugin needs no accessors dependency`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writePluginBuild(withSettingsPlugin = true, withDependency = false)
    project.write(
      "buildSrc/src/main/kotlin/com/example/demo-convention.gradle.kts",
      """
      |package com.example
      |
      |// deliberately no imports, and no dependency on the accessors
      |val alpha by tasks.registering
      |
      |tasks.register("verifyAccessor") {
      |  val accessorResult = alpha.name
      |  doLast { println("ACCESSOR OK: " + accessorResult) }
      |}
      |""".trimMargin()
    )
    project.writeBuildScript("""plugins { id("com.example.demo-convention") }""")

    val result = project.runner("verifyAccessor", "--warning-mode=all").build()
    assertTrue(
      result.output.contains("ACCESSOR OK: alpha"),
      "Expected the convention plugin to resolve the accessors on Gradle $gradle:\n${result.output}"
    )
    result.assertNoDeprecationNagging(gradle)

    project.writePluginBuild(withSettingsPlugin = false, withDependency = false)
    project.runner("verifyAccessor", "--warning-mode=all").build()
      .assertGradleNagsAboutDelegatedProperties(gradle)
  }

  /**
   * A binary plugin gets no implicit imports.
   *
   * `import org.gradle.kotlin.dsl.*` alone loses, as this library's copies there have Gradle's
   * exact signatures. Adding `import dev.adamko.gradle.kotlin.dsl.*` makes every accessor
   * ambiguous, as both star imports share a tier.
   */
  @AccessorsTest
  fun `a binary plugin needs a single-name import per accessor`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writePluginBuild()
    project.write(
      "buildSrc/src/main/kotlin/com/example/ExamplePlugin.kt",
      """
      |package com.example
      |
      |import org.gradle.api.Plugin
      |import org.gradle.api.Project
      |import org.gradle.kotlin.dsl.*
      |import dev.adamko.gradle.kotlin.dsl.getValue
      |import dev.adamko.gradle.kotlin.dsl.provideDelegate
      |import dev.adamko.gradle.kotlin.dsl.registering
      |
      |abstract class ExamplePlugin : Plugin<Project> {
      |  override fun apply(project: Project) {
      |    with(project) {
      |      val alpha by tasks.registering
      |
      |      tasks.register("verifyAccessor") {
      |        val accessorResult = alpha.name
      |        doLast { println("ACCESSOR OK: " + accessorResult) }
      |      }
      |    }
      |  }
      |}
      |""".trimMargin()
    )
    project.writeBuildScript("""apply<com.example.ExamplePlugin>()""")

    val result = project.runner("verifyAccessor", "--warning-mode=all").build()

    assertTrue(
      result.output.contains("ACCESSOR OK: alpha"),
      "Expected the binary plugin to resolve the accessors on Gradle $gradle:\n${result.output}"
    )
    result.assertNoDeprecationNagging(gradle)
  }

  /**
   * The settings plugin must preserve Gradle's script resolver environment: plugin spec builders,
   * type-safe project accessors, and default imports.
   */
  @AccessorsTest
  fun `the settings plugin leaves precompiled script plugin compilation intact`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writePluginBuild()
    project.write(
      "buildSrc/src/main/kotlin/com/example/untouched-convention.gradle.kts",
      """
      |package com.example
      |
      |$RESOLVER_ENVIRONMENT_LAYERS
      |
      |tasks.register("verifyCompilation") {
      |  doLast { println("COMPILATION OK") }
      |}
      |""".trimMargin()
    )
    project.writeBuildScript("""plugins { id("com.example.untouched-convention") }""")

    val result = project.runner("verifyCompilation", "--warning-mode=all").build()

    assertTrue(
      result.output.contains("COMPILATION OK"),
      "Expected the convention plugin to compile and run on Gradle $gradle:\n${result.output}"
    )
  }

  /** `buildSrc` set up as the README describes. */
  private fun AccessorsTestProject.writePluginBuild(
    withSettingsPlugin: Boolean = true,
    withDependency: Boolean = true,
  ) {
    writeSettingsScript("""rootProject.name = "consumer"""")

    val buildSrc = nestedBuild("buildSrc")
    buildSrc.writeSettingsScript(
      """
      |pluginManagement {
      |  repositories {
      |    maven { url = uri("${AccessorsTestProject.devMavenRepo}") }
      |    gradlePluginPortal()
      |  }
      |}
      |
      |${if (withSettingsPlugin) settingsPluginBlock() else ""}
      |
      |rootProject.name = "buildSrc"
      """.trimMargin()
    )
    buildSrc.writeBuildScript(
      """
      |plugins {
      |  `kotlin-dsl`
      |}
      |
      |repositories {
      |  mavenCentral()
      |  maven { url = uri("${AccessorsTestProject.devMavenRepo}") }
      |}
      |
      |${if (withDependency) "dependencies { implementation($LIBRARY_ACCESSOR) }" else ""}
      |""".trimMargin()
    )
  }
}

/** The three parts of Gradle's resolver environment for a precompiled script plugin. */
private val RESOLVER_ENVIRONMENT_LAYERS = """
  |// `kotlinDslPluginSpecBuildersImplicitImports` - the plugin id accessors
  |plugins {
  |  base
  |}
  |
  |// a type-safe project accessor, generated from the `plugins { }` block above
  |base {
  |  archivesName.set("demo")
  |}
  |
  |// `kotlinDslImplicitImports` - `withType` and `AbstractTestTask` are both default imports
  |tasks.withType<AbstractTestTask>().configureEach { }
  """.trimMargin()
