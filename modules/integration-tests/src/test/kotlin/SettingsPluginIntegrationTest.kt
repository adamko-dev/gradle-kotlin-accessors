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

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * The settings plugin puts the accessors on every build script's classpath, and adds them as a
 * dependency of `kotlin-dsl` projects. The catalog is covered by [VersionCatalogIntegrationTest].
 */
class SettingsPluginIntegrationTest {

  /** Uses a subproject whose script never mentions the library. */
  @AccessorsTest
  fun `the settings plugin makes the accessors available to every project`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writeSettingsScript(
      """
      |pluginManagement {
      |  repositories { maven { url = uri("${AccessorsTestProject.devMavenRepo}") } }
      |}
      |
      |plugins {
      |  id("dev.adamko.gradle-kotlin-accessors") version "${AccessorsTestProject.accessorsVersion}"
      |}
      |
      |rootProject.name = "consumer"
      |
      |include(":sub")
      |""".trimMargin()
    )

    // neither script declares a buildscript block
    project.writeBuildScript(
      """
      |val alpha by tasks.registering
      |
      |tasks.register("verifyAccessor") {
      |  val accessorResult = alpha.name
      |  doLast { println("ACCESSOR OK: " + accessorResult) }
      |}
      |""".trimMargin()
    )
    project.write(
      "sub/build.gradle.kts",
      """
      |val beta by tasks.registering
      |
      |tasks.register("verifySubprojectAccessor") {
      |  val accessorResult = beta.name
      |  doLast { println("SUBPROJECT ACCESSOR OK: " + accessorResult) }
      |}
      |""".trimMargin()
    )

    val result = project.runner("verifyAccessor", ":sub:verifySubprojectAccessor", "--warning-mode=all").build()

    assertTrue(
      result.output.contains("ACCESSOR OK: alpha"),
      "Expected the root project to resolve the accessors:\n${result.output}"
    )
    assertTrue(
      result.output.contains("SUBPROJECT ACCESSOR OK: beta"),
      "Expected the subproject to resolve the accessors:\n${result.output}"
    )
    result.assertNoDeprecationNagging(gradle)
  }

  /** `buildSrc` has its own settings file, but inherits the main build's settings classpath. */
  @AccessorsTest
  fun `buildSrc scripts use the accessors when the main build applies the settings plugin`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    assertFalse(
      project.buildSrcNags(gradle, withPlugin = true),
      "Expected buildSrc to resolve this library's accessors on Gradle $gradle"
    )
    assertTrue(
      project.buildSrcNags(gradle, withPlugin = false),
      "Expected buildSrc to fall back to Gradle's accessors on Gradle $gradle"
    )
  }

  /** Runs a build whose `buildSrc` uses `by registering`, and reports whether Gradle nagged. */
  private fun AccessorsTestProject.buildSrcNags(
    gradle: TestedGradleVersion,
    withPlugin: Boolean,
  ): Boolean {
    val settingsPlugin =
      if (withPlugin) """plugins { id("dev.adamko.gradle-kotlin-accessors") version "${AccessorsTestProject.accessorsVersion}" }"""
      else ""
    writeSettingsScript(
      """
      |pluginManagement {
      |  repositories { maven { url = uri("${AccessorsTestProject.devMavenRepo}") } }
      |}
      |
      |$settingsPlugin
      |
      |rootProject.name = "consumer"
      |""".trimMargin()
    )
    writeBuildScript("""tasks.register("noop")""" + "\n")

    val buildSrc = nestedBuild("buildSrc")
    buildSrc.writeBuildScript(
      """
      |val alpha by tasks.registering
      |tasks.register("noopBuildSrc") { dependsOn(alpha) }
      |""".trimMargin()
    )

    return gradle.delegatedPropertyNag.containsMatchIn(
      runner("noop", "--warning-mode=all").build().output
    )
  }

  @AccessorsTest
  fun `main build scripts use the accessors when a kotlin-dsl buildSrc applies the settings plugin`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    assertFalse(
      project.mainBuildNagsWithKotlinDslBuildSrc(gradle, withPlugin = true),
      "Expected the main build to resolve this library's accessors through buildSrc on Gradle $gradle"
    )
    assertTrue(
      project.mainBuildNagsWithKotlinDslBuildSrc(gradle, withPlugin = false),
      "Expected the main build to fall back to Gradle's accessors on Gradle $gradle"
    )
  }

  /**
   * Runs a build whose `buildSrc` applies `kotlin-dsl` and declares no dependency on the accessors,
   * and whose main build script uses `by registering`, and reports whether Gradle nagged.
   */
  private fun AccessorsTestProject.mainBuildNagsWithKotlinDslBuildSrc(
    gradle: TestedGradleVersion,
    withPlugin: Boolean,
  ): Boolean {
    writeSettingsScript("""rootProject.name = "consumer"""")
    writeBuildScript(
      """
      |val alpha by tasks.registering
      |
      |tasks.register("verifyAccessor") {
      |  val accessorResult = alpha.name
      |  doLast { println("ACCESSOR OK: " + accessorResult) }
      |}
      |""".trimMargin()
    )

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
      |${if (withPlugin) settingsPluginBlock() else ""}
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
      |""".trimMargin()
    )

    val result = runner("verifyAccessor", "--warning-mode=all").build()
    assertTrue(
      result.output.contains("ACCESSOR OK: alpha"),
      "Expected the build script to run on Gradle $gradle:\n${result.output}"
    )
    return gradle.delegatedPropertyNag.containsMatchIn(result.output)
  }

  /** Unlike `buildSrc`, an included build does not inherit the main build's settings classpath. */
  @AccessorsTest
  fun `included build scripts do not use the accessors when the main build applies the settings plugin`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    val included = project.nestedBuild("included")
    included.writeSettingsScript("""rootProject.name = "included"""")
    included.writeBuildScript(
      """
      |val alpha by tasks.registering
      |tasks.register("noopIncluded") { dependsOn(alpha) }
      |""".trimMargin()
    )

    project.writeSettingsScript(
      """
      |${project.pluginManagementBlock()}
      |
      |${project.settingsPluginBlock()}
      |
      |rootProject.name = "consumer"
      |
      |includeBuild("included")
      """.trimMargin()
    )
    project.writeBuildScript("""tasks.register("noop")""" + "\n")

    val result = project.runner("noop", "--warning-mode=all").build()

    assertTrue(
      gradle.delegatedPropertyNag.containsMatchIn(result.output),
      "Expected the included build to fall back to Gradle's accessors on Gradle $gradle:\n${result.output}"
    )
  }
}
