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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested

/**
 * One test per accessor, each asserting it works without a deprecation nag.
 *
 * Cases follow Gradle's
 * [KotlinDslDelegatedPropertiesDeprecationIntegrationTest](https://github.com/gradle/gradle/blob/d74857e4d1341ac0ccc384d4522c67f4444ac362/platforms/core-configuration/kotlin-dsl/src/integTest/kotlin/org/gradle/kotlin/dsl/integration/KotlinDslDelegatedPropertiesDeprecationIntegrationTest.kt).
 */
class BuildScriptAccessorsIntegrationTest {

  @Nested
  @DisplayName("`by registering`")
  inner class Registering {
    @AccessorsTest
    fun `tasks registering`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = "val alpha by tasks.registering",
      expression = "alpha.name",
      expected = "alpha"
    )

    @AccessorsTest
    fun `tasks registering with action`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = """val alpha by tasks.registering { group = "demo" }""",
      expression = "alpha.get().group",
      expected = "demo"
    )

    @AccessorsTest
    fun `tasks registering with type`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = "val alpha by tasks.registering(Zip::class)",
      expression = "alpha.get().name",
      expected = "alpha"
    )

    @AccessorsTest
    fun `tasks registering with type and action`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = """val alpha by tasks.registering(Zip::class) { archiveFileName = "alpha.zip" }""",
      expression = "alpha.get().archiveFileName.get()",
      expected = "alpha.zip"
    )
  }

  @Nested
  @DisplayName("`by existing`")
  inner class Existing {
    @AccessorsTest
    fun `tasks existing`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = "val clean by tasks.existing",
      expression = "clean.name",
      expected = "clean"
    )

    @AccessorsTest
    fun `tasks existing with action`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = """val clean by tasks.existing { group = "demo" }""",
      expression = "clean.get().group",
      expected = "demo"
    )

    @AccessorsTest
    fun `tasks existing with type`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = "val clean by tasks.existing(Delete::class)",
      expression = "clean.get().name",
      expected = "clean"
    )

    @AccessorsTest
    fun `tasks existing with type and action`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = """val clean by tasks.existing(Delete::class) { group = "demo" }""",
      expression = "clean.get().group",
      expected = "demo"
    )
  }

  @Nested
  @DisplayName("`by getting`")
  inner class Getting {
    @AccessorsTest
    fun `collection getting`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = """
      |configurations.create("demoConfig")
      |val demoConfig by configurations.getting
      |""".trimMargin(),
      expression = "demoConfig.name",
      expected = "demoConfig"
    )

    @AccessorsTest
    fun `collection getting with action`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = """
      |configurations.create("demoConfig")
      |val demoConfig by configurations.getting { description = "demo" }
      |""".trimMargin(),
      expression = "demoConfig.description",
      expected = "demo"
    )

    @AccessorsTest
    fun `container getting with type`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = """
      |configurations.create("demoConfig")
      |val demoConfig by configurations.getting(Configuration::class)
      |""".trimMargin(),
      expression = "demoConfig.name",
      expected = "demoConfig"
    )

    @AccessorsTest
    fun `container getting with type and action`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = """
      |configurations.create("demoConfig")
      |val demoConfig by configurations.getting(Configuration::class) { description = "demo" }
      |""".trimMargin(),
      expression = "demoConfig.description",
      expected = "demo"
    )
  }

  /** Not a Gradle deprecation: the role-based factories never had a delegated form. */
  @Nested
  @DisplayName("`by dependencyScope` / `by resolvable` / `by consumable`")
  inner class RoleBasedConfigurations {
    @AccessorsTest
    fun `configurations dependencyScope`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = "val declared by configurations.dependencyScope",
      expression = """declared.name + ":" + declared.get().isCanBeDeclared""",
      expected = "declared:true"
    )

    @AccessorsTest
    fun `configurations dependencyScope with action`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = """val declared by configurations.dependencyScope { description = "demo" }""",
      expression = "declared.get().description",
      expected = "demo"
    )

    @AccessorsTest
    fun `configurations resolvable`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = "val resolved by configurations.resolvable",
      expression = """resolved.name + ":" + resolved.get().isCanBeResolved""",
      expected = "resolved:true"
    )

    @AccessorsTest
    fun `configurations resolvable with action`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = """val resolved by configurations.resolvable { description = "demo" }""",
      expression = "resolved.get().description",
      expected = "demo"
    )

    @AccessorsTest
    fun `configurations consumable`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = "val consumed by configurations.consumable",
      expression = """consumed.name + ":" + consumed.get().isCanBeConsumed""",
      expected = "consumed:true"
    )

    @AccessorsTest
    fun `configurations consumable with action`(project: AccessorsTestProject) = project.assertAccessor(
      declarations = """val consumed by configurations.consumable { description = "demo" }""",
      expression = "consumed.get().description",
      expected = "demo"
    )

    @AccessorsTest
    fun `the accessors do not shadow the factory methods they call`(project: AccessorsTestProject) =
      project.assertAccessor(
        declarations = """
        |val explicit = configurations.dependencyScope("explicit")
        |val delegated by configurations.dependencyScope
        |""".trimMargin(),
        expression = """explicit.name + ":" + delegated.name""",
        expected = "explicit:delegated"
      )
  }

  // A receiver typed as the collection interface rather than as a container, taken from the
  // `NamedDomainObjectCollection delegates emit deprecation warnings` case of
  // https://github.com/gradle/gradle/blob/d74857e4d1341ac0ccc384d4522c67f4444ac362/platforms/core-configuration/kotlin-dsl/src/integTest/kotlin/org/gradle/kotlin/dsl/integration/KotlinDslDelegatedPropertiesDeprecationIntegrationTest.kt

  @AccessorsTest
  fun `collection-typed receiver`(project: AccessorsTestProject) = project.assertAccessor(
    declarations = """
      |configurations.register("a")
      |configurations.register("b")
      |val collection: NamedDomainObjectCollection<Configuration> = configurations
      |val a by collection.existing
      |val b by collection.getting
      |""".trimMargin(),
    expression = "a.get().name + b.name",
    expected = "ab"
  )

  // container and provider used directly as delegates

  @AccessorsTest
  fun `collection as delegate`(project: AccessorsTestProject) = project.assertAccessor(
    declarations = "val clean by tasks",
    expression = "clean.name",
    expected = "clean"
  )

  @AccessorsTest
  fun `provider as delegate`(project: AccessorsTestProject) = project.assertAccessor(
    declarations = """val clean: Delete by tasks.named("clean", Delete::class.java)""",
    expression = "clean.name",
    expected = "clean"
  )

  // `by extensions`

  @AccessorsTest
  fun `extension by name`(project: AccessorsTestProject) = project.assertAccessor(
    declarations = "val base: org.gradle.api.plugins.BasePluginExtension by extensions",
    expression = "base.archivesName.get()",
    expected = "consumer"
  )
  /** Public, as the accessors' return types, but with nothing a build script can use. */
  @AccessorsTest
  fun `delegate provider types cannot be constructed from a build script`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writeSettingsScript("""rootProject.name = "consumer"""")
    project.writeConsumerScript(
      declarations = "val provider = ExistingDomainObjectDelegateProvider(tasks)",
      expression = "provider",
      withLibrary = true
    )

    val result = project.buildAndFail()

    assertTrue(
      gradle.kotlin.cannotAccessConstructor.containsMatchIn(result.output) &&
          gradle.kotlin.becauseInternal.containsMatchIn(result.output),
      "Expected the constructor to be invisible to the build script on Gradle $gradle:\n${result.output}"
    )
  }

  /** Calibrates the tests above: without this library, Gradle nags. */
  @AccessorsTest
  fun `gradle's own accessor is deprecated`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writeSettingsScript("""rootProject.name = "consumer"""")
    project.writeConsumerScript(
      declarations = "val alpha by tasks.registering",
      expression = "alpha.name",
      withLibrary = false
    )

    val result = project.build()

    assertEquals(TaskOutcome.SUCCESS, result.task(":verifyAccessor")?.outcome)
    result.assertGradleNagsAboutDelegatedProperties(gradle)
  }

  /** Calibrates the role-based tests above: without this library, the script does not compile. */
  @AccessorsTest
  fun `gradle has no delegated form of the role-based factories`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writeSettingsScript("""rootProject.name = "consumer"""")
    project.writeConsumerScript(
      declarations = "val declared by configurations.dependencyScope",
      expression = "declared.name",
      withLibrary = false
    )

    val result = project.buildAndFail()

    assertTrue(
      gradle.kotlin.scriptCompilationError.containsMatchIn(result.output) &&
          result.output.contains("dependencyScope"),
      "Expected `by configurations.dependencyScope` not to compile without this library on Gradle $gradle:\n${result.output}"
    )
  }

  /**
   * `testing.suites`: a polymorphic container reached through an extension.
   *
   * Only `registering(KClass)` exists. `registering<JvmTestSuite>` would need all three type
   * arguments, and a single-parameter reified overload makes `by registering { }` ambiguous.
   */
  @AccessorsTest
  fun `test suites registering with type`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    project.writeSettingsScript("""rootProject.name = "consumer"""")
    project.writeBuildScript(
      """
      |import org.gradle.api.plugins.jvm.JvmTestSuite
      |
      |${project.libraryOnBuildscriptClasspath()}
      |
      |plugins {
      |  `java-library`
      |}
      |
      |val integrationTest by testing.suites.registering(JvmTestSuite::class) { }
      |
      |tasks.register("verifyAccessor") {
      |  val accessorResult = integrationTest.name
      |  doLast { println("ACCESSOR OK: " + accessorResult) }
      |}
      |""".trimMargin()
    )

    val result = project.runner("verifyAccessor", "--warning-mode=all").build()

    assertTrue(
      result.output.contains("ACCESSOR OK: integrationTest"),
      "Expected the suite accessor to resolve on Gradle $gradle:\n${result.output}"
    )
    result.assertNoDeprecationNagging(gradle)
  }

  private fun AccessorsTestProject.assertAccessor(declarations: String, expression: String, expected: String) {
    writeSettingsScript("""rootProject.name = "consumer"""")
    writeConsumerScript(declarations, expression, withLibrary = true)

    assertBuildSucceedsWith(expected)
  }

  private fun AccessorsTestProject.assertBuildSucceedsWith(expected: String) {
    val result = build()

    assertEquals(TaskOutcome.SUCCESS, result.task(":verifyAccessor")?.outcome)
    assertTrue(
      result.output.contains("ACCESSOR OK: $expected"),
      "Expected 'ACCESSOR OK: $expected' in build output:\n${result.output}"
    )
    result.assertNoDeprecationNagging(gradle)
  }

  private fun AccessorsTestProject.build() = verifyRunner().build()

  private fun AccessorsTestProject.buildAndFail() = verifyRunner().buildAndFail()

  private fun AccessorsTestProject.verifyRunner() = runner("verifyAccessor", "--warning-mode=all", "--stacktrace")

  /** No import needed: the accessors live in the root package, as the script does. */
  private fun AccessorsTestProject.writeConsumerScript(declarations: String, expression: String, withLibrary: Boolean) {
    writeBuildScript(
      """
      |import org.gradle.api.NamedDomainObjectCollection
      |import org.gradle.api.artifacts.Configuration
      |import org.gradle.api.tasks.Delete
      |import org.gradle.api.tasks.bundling.Zip
      |
      |${if (withLibrary) libraryOnBuildscriptClasspath() else ""}
      |
      |plugins {
      |  base
      |}
      |
      |$declarations
      |
      |tasks.register("verifyAccessor") {
      |  val accessorResult = ($expression).toString()
      |  doLast { println("ACCESSOR OK: " + accessorResult) }
      |}
      |""".trimMargin()
    )
  }
}
