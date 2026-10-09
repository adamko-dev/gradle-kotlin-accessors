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

@file:Suppress("UnstableApiUsage")

import java.nio.file.Path
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ConsumableConfiguration
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.kotlin.dsl.dependencyScope
import org.gradle.kotlin.dsl.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir


/**
 * No upstream counterpart: Gradle has no delegated form of the role-based factories.
 *
 * Each accessor is checked for name, role, and laziness.
 */
class ConfigurationContainerExtensionsTest {

  @TempDir
  lateinit var projectDir: Path

  private val project: Project by lazy { buildProject(projectDir) }

  private val configurations: ConfigurationContainer by lazy { project.configurations }

  @Test
  fun `val implementation by dependencyScope`() {
    configurations.apply {
      val implementation by dependencyScope {}

      assertInferredTypeOf(
        implementation,
        typeOf<NamedDomainObjectProvider<DependencyScopeConfiguration>>()
      )
      assertEquals(listOf("implementation"), names.toList())
      assertEquals("implementation", implementation.get().name)
    }
  }

  @Test
  fun `val implementation by dependencyScope { }`() {
    var state = "unconfigured"

    configurations.apply {
      val implementation by dependencyScope {
        state = "configured"
        description = "declared dependencies"
      }

      assertInferredTypeOf(
        implementation,
        typeOf<NamedDomainObjectProvider<DependencyScopeConfiguration>>()
      )
      assertEquals("unconfigured", state, "Expected the action to be deferred until the configuration is realized")

      assertEquals("declared dependencies", implementation.get().description)
      assertEquals("configured", state)
    }
  }

  @Test
  fun `val compileClasspath by resolvable`() {
    configurations.apply {
      val compileClasspath by resolvable

      assertInferredTypeOf(
        compileClasspath,
        typeOf<NamedDomainObjectProvider<ResolvableConfiguration>>()
      )
      assertEquals(listOf("compileClasspath"), names.toList())
      assertEquals("compileClasspath", compileClasspath.get().name)
    }
  }

  @Test
  fun `val compileClasspath by resolvable { }`() {
    var state = "unconfigured"

    configurations.apply {
      val compileClasspath by resolvable {
        state = "configured"
        description = "compile classpath"
      }

      assertEquals("unconfigured", state, "Expected the action to be deferred until the configuration is realized")

      assertEquals("compile classpath", compileClasspath.get().description)
      assertEquals("configured", state)
    }
  }

  @Test
  fun `val apiElements by consumable`() {
    configurations.apply {
      val apiElements by consumable

      assertInferredTypeOf(
        apiElements,
        typeOf<NamedDomainObjectProvider<ConsumableConfiguration>>()
      )
      assertEquals(listOf("apiElements"), names.toList())
      assertEquals("apiElements", apiElements.get().name)
    }
  }

  @Test
  fun `val apiElements by consumable { }`() {
    var state = "unconfigured"

    configurations.apply {
      val apiElements by consumable {
        state = "configured"
        description = "API elements"
      }

      assertEquals("unconfigured", state, "Expected the action to be deferred until the configuration is realized")

      assertEquals("API elements", apiElements.get().description)
      assertEquals("configured", state)
    }
  }

  /** Unlike `by configurations.registering`, which creates a legacy, all-roles configuration. */
  @Test
  fun `each accessor registers a configuration in its own role`() {
    configurations.apply {
      val declared by dependencyScope
      val resolved by resolvable
      val consumed by consumable

      assertEquals(
        listOf(true, false, false),
        listOf(declared, resolved, consumed).map { it.get().isCanBeDeclared },
        "isCanBeDeclared"
      )
      assertEquals(
        listOf(false, true, false),
        listOf(declared, resolved, consumed).map { it.get().isCanBeResolved },
        "isCanBeResolved"
      )
      assertEquals(
        listOf(false, false, true),
        listOf(declared, resolved, consumed).map { it.get().isCanBeConsumed },
        "isCanBeConsumed"
      )
    }
  }

  /** Kotlin picks the member for a call with an argument, the extension for a bare reference. */
  @Test
  fun `the accessors do not shadow the factory methods they call`() {
    configurations.apply {
      val explicit = dependencyScope("explicit")
      val delegated by dependencyScope

      // `names` is sorted, not insertion-ordered
      assertEquals(listOf("delegated", "explicit"), names.toList())
      assertEquals("explicit", explicit.get().name)
      assertEquals("delegated", delegated.get().name)
    }
  }
}
