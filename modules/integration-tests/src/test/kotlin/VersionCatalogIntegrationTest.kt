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

import org.junit.jupiter.api.Assertions.assertTrue

/**
 * The version catalog the settings plugin declares, so code compiled by the Kotlin Gradle Plugin
 * can depend on the library without naming a version.
 */
class VersionCatalogIntegrationTest {

  /** Asserts on what a build script reads from the catalog, not on the plugin's constants. */
  @AccessorsTest
  fun `the catalog declares both modules at this plugin's version`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writeSettingsScript(
      """
      |${project.pluginManagementBlock()}
      |
      |${project.settingsPluginBlock()}
      |
      |rootProject.name = "consumer"
      """.trimMargin()
    )
    project.writeBuildScript(
      """
      |fun coordinatesOf(library: Provider<MinimalExternalModuleDependency>): String =
      |  library.get().run { module.group + ":" + module.name + ":" + versionConstraint.requiredVersion }
      |
      |tasks.register("printCatalog") {
      |  val accessors = coordinatesOf($LIBRARY_ACCESSOR)
      |  val settingsPlugin = coordinatesOf($SETTINGS_PLUGIN_ACCESSOR)
      |  val version = $VERSION_ACCESSOR.get()
      |  doLast {
      |    println("CATALOG ACCESSORS: " + accessors)
      |    println("CATALOG SETTINGS PLUGIN: " + settingsPlugin)
      |    println("CATALOG VERSION: " + version)
      |  }
      |}
      |""".trimMargin()
    )

    val result = project.runner("printCatalog", "--warning-mode=all").build()

    val settingsPluginCoordinates =
      "$ACCESSORS_GROUP:$SETTINGS_PLUGIN_ARTIFACT:${AccessorsTestProject.accessorsVersion}"

    assertTrue(
      result.output.contains("CATALOG ACCESSORS: ${AccessorsTestProject.accessorsCoordinates}"),
      "Expected the catalog to declare ${AccessorsTestProject.accessorsCoordinates} on Gradle $gradle:\n${result.output}"
    )
    assertTrue(
      result.output.contains("CATALOG SETTINGS PLUGIN: $settingsPluginCoordinates"),
      "Expected the catalog to declare $settingsPluginCoordinates on Gradle $gradle:\n${result.output}"
    )
    assertTrue(
      result.output.contains("CATALOG VERSION: ${AccessorsTestProject.accessorsVersion}"),
      "Expected the catalog to name version ${AccessorsTestProject.accessorsVersion} on Gradle $gradle:\n${result.output}"
    )
    result.assertNoDeprecationNagging(gradle)
  }

  /**
   * `buildSrc` inherits the main build's classpath but not its catalog, so needs the plugin applied
   * too. The version is never written in the test project.
   */
  @AccessorsTest
  fun `buildSrc can depend on the accessors through the catalog`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writeSettingsScript("""rootProject.name = "consumer"""")
    project.writeBuildScript(
      """
      |plugins {
      |  id("demo-convention")
      |}
      |""".trimMargin()
    )

    val buildSrc = project.nestedBuild("buildSrc")
    buildSrc.writeSettingsScript(
      """
      |pluginManagement {
      |  repositories {
      |    maven { url = uri("${AccessorsTestProject.devMavenRepo}") }
      |    gradlePluginPortal()
      |  }
      |}
      |
      |${project.settingsPluginBlock()}
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
      |dependencies {
      |  // no version here, and none anywhere else in this build
      |  implementation($LIBRARY_ACCESSOR)
      |}
      |""".trimMargin()
    )
    buildSrc.write(
      "src/main/kotlin/demo-convention.gradle.kts",
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

    val result = project.runner("verifyAccessor", "--warning-mode=all").build()

    assertTrue(
      result.output.contains("ACCESSOR OK: alpha"),
      "Expected the convention plugin to compile against the catalogued accessors on Gradle " +
          "$gradle:\n${result.output}"
    )
    result.assertNoDeprecationNagging(gradle)
  }

  /** Calibrates the tests above: without the plugin there is no catalog. */
  @AccessorsTest
  fun `there is no catalog without the plugin`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writeSettingsScript("""rootProject.name = "consumer"""")
    project.writeBuildScript(
      """
      |tasks.register("printCatalog") {
      |  val coordinates = $LIBRARY_ACCESSOR.map { it.module.name }
      |  doLast { println("CATALOG LIBRARY: " + coordinates.get()) }
      |}
      |""".trimMargin()
    )

    val result = project.runner("printCatalog", "--warning-mode=all").buildAndFail()

    assertTrue(
      gradle.kotlin.unresolvedReference.containsMatchIn(result.output) ||
          gradle.kotlin.scriptCompilationError.containsMatchIn(result.output),
      "Expected `$CATALOG_NAME` to have nothing to resolve to on Gradle $gradle:\n${result.output}"
    )
  }
}
