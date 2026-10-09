/*
 * Copyright 2018 the original author or authors.
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

import dev.adamko.gradle.kotlin.dsl.ExistingDomainObjectDelegateProvider
import java.nio.file.Path
import org.gradle.api.Task
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.typeOf
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

/**
 * Ported from Gradle's [TaskContainerExtensionsTest](https://github.com/gradle/gradle/blob/d74857e4d1341ac0ccc384d4522c67f4444ac362/platforms/core-configuration/kotlin-dsl/src/test/kotlin/org/gradle/kotlin/dsl/TaskContainerExtensionsTest.kt).
 *
 * Covers the `TaskContainer` overloads, whose delegate is a `TaskProvider`, so the inferred type is
 * under test too.
 *
 * Changed from upstream as [NamedDomainObjectCollectionExtensionsTest], and drops the `create` /
 * `register` helpers.
 */
class TaskContainerExtensionsTest {

  @TempDir
  lateinit var projectDir: Path

  private val tasks by lazy { buildProject(projectDir).tasks }

  @Test
  fun `val task by registering`() {
    tasks.apply {
      val clean by registering

      assertInferredTypeOf(clean, typeOf<TaskProvider<Task>>())
      assertTrue("clean" in names)
      assertEquals("clean", clean.get().name)
    }
  }

  @Test
  fun `val task by registering { }`() {
    var configured = false

    tasks.apply {
      val clean by registering {
        assertInferredTypeOf(this, typeOf<Task>())
        configured = true
        group = "demo"
      }

      assertFalse(configured, "the configuration action must not run before the task is realized")
      assertEquals("demo", clean.get().group)
      assertTrue(configured)
      assertInferredTypeOf(clean, typeOf<TaskProvider<Task>>())
    }
  }

  @Test
  fun `val task by registering(type)`() {
    tasks.apply {
      val clean by registering(Delete::class)

      assertInferredTypeOf(clean, typeOf<TaskProvider<Delete>>())
      assertEquals("clean", clean.get().name)
    }
  }

  @Test
  fun `val task by registering(type) { }`() {
    var configured = false

    tasks.apply {
      val clean by registering(Delete::class) {
        assertInferredTypeOf(this, typeOf<Delete>())
        configured = true
        group = "demo"
      }

      assertFalse(configured, "the configuration action must not run before the task is realized")
      assertEquals("demo", clean.get().group)
      assertInferredTypeOf(clean, typeOf<TaskProvider<Delete>>())
    }
  }

  @Test
  fun `val task by existing { }`() {
    var configured = false
    tasks.register("clean", Delete::class.java)

    tasks.apply {
      val clean by existing {
        assertInferredTypeOf(this, typeOf<Task>())
        configured = true
        group = "demo"
      }

      assertFalse(configured, "the configuration action must not run before the task is realized")
      assertEquals("demo", clean.get().group)
      assertInferredTypeOf(clean, typeOf<TaskProvider<Task>>())
    }
  }

  @Test
  fun `val task by existing(type)`() {
    tasks.register("clean", Delete::class.java)

    tasks.apply {
      val clean by existing(Delete::class)

      assertInferredTypeOf(clean, typeOf<TaskProvider<Delete>>())
      assertEquals("clean", clean.get().name)
    }
  }

  @Test
  fun `task accessors can be made available via existing delegate provider`() {
    tasks.register("clean", Delete::class.java)

    tasks.apply {
      val clean by existing
      assertInferredTypeOf(clean, typeOf<TaskProvider<Task>>())

      var configured = false
      existing.clean.configure { cleanTask ->
        assertInferredTypeOf(cleanTask, typeOf<Delete>())
        configured = true
      }

      assertFalse(configured, "configuring through an accessor must stay lazy too")
      assertEquals("clean", clean.get().name)
      assertTrue(configured)
    }
  }

  @Test
  fun `provider delegate rejects an element of the wrong type`() {
    tasks.register("alpha")

    val failure = assertThrows<IllegalArgumentException> {
      val alpha: Delete by tasks.named("alpha")
      // the delegate is lazy: the cast only happens when the property is read
      alpha.name
    }

    assertTrue(
      failure.message.orEmpty().startsWith("Element 'alpha' of type "),
      "Unexpected message: ${failure.message}"
    )
    assertTrue(
      failure.message.orEmpty().endsWith("cannot be cast to 'org.gradle.api.tasks.Delete'."),
      "Unexpected message: ${failure.message}"
    )
  }

  /** Stands in for a generated task accessor, why delegate providers carry their container. */
  private val ExistingDomainObjectDelegateProvider<out TaskContainer>.clean: TaskProvider<Delete>
    get() = delegateProvider.named<Delete>("clean")
}
