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

import kotlin.io.path.createDirectories
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.writeText
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Why the accessors live in the root package. Each test plants a rival `registering` and reports
 * which one a build script got.
 */
class PackageResolutionIntegrationTest {

  @AccessorsTest
  fun `a same-signature declaration in Gradle's package loses to Gradle's own`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    val output = project.runShadowBuild(
      fileName = "Shadow.kt",
      declaration = """
        |val <T : Any, C : NamedDomainObjectContainer<T>> C.registering: ShadowProvider<T, C>
        |  get() = ShadowProvider(this)
      """.trimMargin()
    )

    assertFalse(output.contains(SHADOW_MARKER), "Expected Gradle's own accessor to win on Gradle $gradle -- if it no longer does, " +
        "Gradle's own `registering` has changed shape in this version:\n$output")
    assertTrue(gradle.delegatedPropertyNag.containsMatchIn(output), "Expected Gradle's deprecation warning on Gradle $gradle:\n$output")
  }

  /** Ordinary overload resolution: `TaskContainer` beats `C : NamedDomainObjectContainer<T>`. */
  @AccessorsTest
  fun `a more specific declaration in Gradle's package wins`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    val output = project.runShadowBuild(
      fileName = "Shadow.kt",
      declaration = """
        |val TaskContainer.registering: ShadowProvider<Task, TaskContainer>
        |  get() = ShadowProvider(this)
      """.trimMargin()
    )

    assertTrue(output.contains(SHADOW_MARKER), "Expected the more specific declaration to win on Gradle $gradle:\n$output")
    assertFalse(gradle.delegatedPropertyNag.containsMatchIn(output), "Expected Gradle's accessor not to run on Gradle $gradle:\n$output")
  }

  /**
   * `NamedDomainObjectContainerExtensions.kt` compiles to the facade class
   * `org.gradle.kotlin.dsl.NamedDomainObjectContainerExtensionsKt`, which Gradle already ships, so
   * the build's own is never loaded. Differs from the test above only in file name.
   */
  @AccessorsTest
  fun `a declaration whose file name collides with a Gradle facade class is invisible`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    val output = project.runShadowBuild(
      fileName = "NamedDomainObjectContainerExtensions.kt",
      declaration = """
        |val TaskContainer.registering: ShadowProvider<Task, TaskContainer>
        |  get() = ShadowProvider(this)
      """.trimMargin()
    )

    assertFalse(output.contains(SHADOW_MARKER), "Expected the colliding file to be invisible on Gradle $gradle:\n$output")
    assertTrue(gradle.delegatedPropertyNag.containsMatchIn(output), "Expected Gradle's deprecation warning on Gradle $gradle:\n$output")
  }

  /**
   * A build script compiles into the root package, and Kotlin resolves same-package declarations
   * ahead of star and default imports.
   *
   * Uses a jar on the buildscript classpath, as a published library would arrive.
   */
  @AccessorsTest
  fun `accessors in the root package win with no imports`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    // 1. build a jar whose accessors live in the root package
    val lib = project.nestedBuild("shadow-lib")
    lib.writeSettingsScript("""rootProject.name = "shadow-lib"""")
    lib.writeBuildScript(
      """
      |plugins { `kotlin-dsl` }
      |repositories { mavenCentral() }
      |""".trimMargin()
    )
    lib.write(
      "src/main/kotlin/Shadow.kt",
      """
      |import org.gradle.api.NamedDomainObjectContainer
      |import org.gradle.api.NamedDomainObjectProvider
      |import kotlin.reflect.KProperty
      |
      |class ShadowProvider<T : Any, C : NamedDomainObjectContainer<T>>(val container: C)
      |
      |class ShadowDelegate<T>(val value: T)
      |
      |val <T : Any, C : NamedDomainObjectContainer<T>> C.registering: ShadowProvider<T, C>
      |  get() = ShadowProvider(this)
      |
      |operator fun <T : Any, C : NamedDomainObjectContainer<T>> ShadowProvider<T, C>.provideDelegate(
      |  receiver: Any?,
      |  property: KProperty<*>
      |): ShadowDelegate<NamedDomainObjectProvider<T>> =
      |  ShadowDelegate(container.register(property.name).also { println("$SHADOW_MARKER") })
      |
      |operator fun <T> ShadowDelegate<T>.getValue(receiver: Any?, property: KProperty<*>): T = value
      |""".trimMargin()
    )

    lib.runner("jar").build()

    val jar = lib.projectDir.resolve("build/libs/shadow-lib.jar")

    // 2. consume it from a build script's buildscript classpath, with no imports
    project.writeSettingsScript("""rootProject.name = "consumer"""")
    project.writeBuildScript(
      """
      |buildscript {
      |  dependencies { classpath(files("${jar.invariantSeparatorsPathString}")) }
      |}
      |
      |val alpha by tasks.registering
      |
      |tasks.register("verifyAccessor") {
      |  val name = alpha.name
      |  doLast { println("ACCESSOR OK: " + name) }
      |}
      |""".trimMargin()
    )

    val out = project.runner("verifyAccessor", "--warning-mode=all").build().output

    assertTrue(out.contains(SHADOW_MARKER), "Expected the root-package accessor to win on Gradle $gradle:\n$out")
    assertFalse(gradle.delegatedPropertyNag.containsMatchIn(out), "Expected Gradle's accessor not to run on Gradle $gradle:\n$out")
  }

  /**
   * Gradle star-imports only `org.gradle.kotlin.dsl.*` and `org.gradle.kotlin.dsl.plugins.dsl.*`.
   * The rest of its API is imported by simple name, so a declaration in `org.gradle` is never seen.
   */
  @AccessorsTest
  fun `a declaration in a package Gradle does not star-import is invisible`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    val output = project.runShadowBuild(
      fileName = "Shadow.kt",
      packageName = "org.gradle",
      declaration = """
        |val <T : Any, C : NamedDomainObjectContainer<T>> C.registering: ShadowProvider<T, C>
        |  get() = ShadowProvider(this)
      """.trimMargin()
    )

    assertFalse(output.contains(SHADOW_MARKER), "Expected the declaration to be invisible on Gradle $gradle:\n$output")
    assertTrue(gradle.delegatedPropertyNag.containsMatchIn(output), "Expected Gradle's deprecation warning on Gradle $gradle:\n$output")
  }

  /**
   * `@kotlin.internal.HidesMembers` beats a member of the receiver, not another extension.
   * No annotation raises one extension above another.
   */
  @AccessorsTest
  fun `the HidesMembers annotation does not beat another extension`(
    project: AccessorsTestProject,
    gradle: TestedGradleVersion,
  ) {
    val output = project.runShadowBuild(
      fileName = "Shadow.kt",
      declaration = """
        |@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
        |@kotlin.internal.HidesMembers
        |val <T : Any, C : NamedDomainObjectContainer<T>> C.registering: ShadowProvider<T, C>
        |  get() = ShadowProvider(this)
      """.trimMargin()
    )

    assertFalse(output.contains(SHADOW_MARKER), "Expected Gradle's own accessor to still win on Gradle $gradle:\n$output")
    assertTrue(gradle.delegatedPropertyNag.containsMatchIn(output), "Expected Gradle's deprecation warning on Gradle $gradle:\n$output")
  }
  /**
   * Builds a consumer whose `buildSrc` declares [declaration] into `org.gradle.kotlin.dsl`, and a
   * build script that uses `by tasks.registering` with no imports, so the only route to the name is
   * the default `org.gradle.kotlin.dsl.*` import naming both declarations.
   */
  private fun AccessorsTestProject.runShadowBuild(
    fileName: String,
    declaration: String,
    packageName: String = "org.gradle.kotlin.dsl",
  ): String {
    writeSettingsScript("""rootProject.name = "consumer"""")

    val buildSrc = nestedBuild("buildSrc")
    val packageDir = if (packageName.isEmpty()) "" else "/" + packageName.replace('.', '/')
    buildSrc.writeSettingsScript("""rootProject.name = "buildSrc"""")
    buildSrc.writeBuildScript(
      """
      |plugins { `kotlin-dsl` }
      |repositories { mavenCentral() }
      |""".trimMargin()
    )
    buildSrc.write(
      "src/main/kotlin$packageDir/$fileName",
      """
      |${if (packageName.isEmpty()) "" else "package $packageName"}
      |
      |import org.gradle.api.NamedDomainObjectContainer
      |import org.gradle.api.NamedDomainObjectProvider
      |import org.gradle.api.Task
      |import org.gradle.api.tasks.TaskContainer
      |import kotlin.reflect.KProperty
      |
      |class ShadowProvider<T : Any, C : NamedDomainObjectContainer<T>>(val container: C)
      |
      |class ShadowDelegate<T>(val value: T)
      |
      |$declaration
      |
      |operator fun <T : Any, C : NamedDomainObjectContainer<T>> ShadowProvider<T, C>.provideDelegate(
      |  receiver: Any?,
      |  property: KProperty<*>
      |): ShadowDelegate<NamedDomainObjectProvider<T>> =
      |  ShadowDelegate(container.register(property.name).also { println("$SHADOW_MARKER") })
      |
      |operator fun <T> ShadowDelegate<T>.getValue(receiver: Any?, property: KProperty<*>): T = value
      |""".trimMargin()
    )

    writeBuildScript(
      """
      |val alpha by tasks.registering
      |
      |tasks.register("verifyAccessor") {
      |  val name = alpha.name
      |  doLast { println("ACCESSOR OK: " + name) }
      |}
      |""".trimMargin()
    )

    return runner("verifyAccessor", "--warning-mode=all").build().output
  }

  private companion object {

    /** Printed by a rival declaration when it, rather than Gradle's, is the one that ran. */
    const val SHADOW_MARKER = "SHADOW ACCESSOR RAN"
  }
}
