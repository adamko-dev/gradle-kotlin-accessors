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

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.kotlin.dsl.typeOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import org.gradle.kotlin.dsl.*

/**
 * Ported from Gradle's [NamedDomainObjectCollectionExtensionsTest](https://github.com/gradle/gradle/blob/d74857e4d1341ac0ccc384d4522c67f4444ac362/platforms/core-configuration/kotlin-dsl/src/test/kotlin/org/gradle/kotlin/dsl/NamedDomainObjectCollectionExtensionsTest.kt).
 *
 * Changed from upstream:
 * - no `ExpectDeprecationExtension.intercept` wrapper;
 * - a real container rather than a Mockito mock, which also pins laziness;
 * - only the direct form, not the `container { ... }` scope;
 * - no tests for declarations this library does not republish: `withType`, the indexer, `named`,
 *   and `creating`.
 */
class NamedDomainObjectCollectionExtensionsTest {

  @TempDir
  lateinit var projectDir: Path

  private val project by lazy { buildProject(projectDir) }

  private val container by lazy {
    project.objects.domainObjectContainer(DomainObject::class.java)
  }

  private val polymorphicContainer by lazy {
    project.objects.polymorphicDomainObjectContainer(DomainObjectBase::class.java).apply {
      registerBinding(Foo::class.java, Foo::class.java)
    }
  }

  @Test
  fun `val domainObject by registering`() {
    container.apply {
      val domainObject by registering

      assertInferredTypeOf(
        domainObject,
        typeOf<NamedDomainObjectProvider<DomainObject>>()
      )
      assertEquals(listOf("domainObject"), names.toList())
      assertEquals("domainObject", domainObject.get().name)
    }
  }

  @Test
  fun `val domainObject by registering { }`() {
    var configured = false

    container.apply {
      val domainObject by registering {
        assertInferredTypeOf(this, typeOf<DomainObject>())
        configured = true
        foo = "foo"
      }

      assertFalse(configured, "the configuration action must not run before the element is realized")
      assertEquals("foo", domainObject.get().foo)
      assertTrue(configured)
      assertInferredTypeOf(
        domainObject,
        typeOf<NamedDomainObjectProvider<DomainObject>>()
      )
    }
  }

  @Test
  fun `val domainObject by registering(type)`() {
    polymorphicContainer.apply {
      val domainObject by registering(Foo::class)

      assertInferredTypeOf(
        domainObject,
        typeOf<NamedDomainObjectProvider<Foo>>()
      )
      assertEquals(listOf("domainObject"), names.toList())
      assertEquals("domainObject", domainObject.get().name)
    }
  }

  @Test
  fun `val domainObject by registering(type) { }`() {
    var configured = false

    polymorphicContainer.apply {
      val domainObject by registering(Foo::class) {
        assertInferredTypeOf(this, typeOf<Foo>())
        configured = true
        foo = "foo"
      }

      assertFalse(configured, "the configuration action must not run before the element is realized")
      assertEquals("foo", domainObject.get().foo)
      assertInferredTypeOf(
        domainObject,
        typeOf<NamedDomainObjectProvider<Foo>>()
      )
    }
  }

  @Test
  fun `can access named element via delegated property`() {
    container.register("domainObject")

    val collection: NamedDomainObjectCollection<DomainObject> = container
    val domainObject by collection

    assertInferredTypeOf(domainObject, typeOf<DomainObject>())
    assertEquals("domainObject", domainObject.name)
  }

  @Test
  fun `val domainObject by existing`() {
    container.register("domainObject")

    container.apply {
      val domainObject by existing

      assertInferredTypeOf(
        domainObject,
        typeOf<NamedDomainObjectProvider<DomainObject>>()
      )
      assertEquals("domainObject", domainObject.get().name)
    }
  }

  @Test
  fun `val domainObject by existing { }`() {
    var configured = false
    container.register("domainObject")

    container.apply {
      val domainObject by existing {
        assertInferredTypeOf(this, typeOf<DomainObject>())
        configured = true
        foo = "foo"
      }

      assertFalse(configured, "the configuration action must not run before the element is realized")
      assertEquals("foo", domainObject.get().foo)
      assertInferredTypeOf(
        domainObject,
        typeOf<NamedDomainObjectProvider<DomainObject>>()
      )
    }
  }

  @Test
  fun `val domainObject by existing(type)`() {
    polymorphicContainer.register("domainObject", Foo::class.java)

    polymorphicContainer.apply {
      val domainObject by existing(Foo::class)

      assertInferredTypeOf(
        domainObject,
        typeOf<NamedDomainObjectProvider<Foo>>()
      )
      assertEquals("domainObject", domainObject.get().name)
    }
  }

  @Test
  fun `val domainObject by existing(type) { }`() {
    var configured = false
    polymorphicContainer.register("domainObject", Foo::class.java)

    polymorphicContainer.apply {
      val domainObject by existing(Foo::class) {
        assertInferredTypeOf(this, typeOf<Foo>())
        configured = true
        foo = "foo"
      }

      assertFalse(configured, "the configuration action must not run before the element is realized")
      assertEquals("foo", domainObject.get().foo)
      assertInferredTypeOf(
        domainObject,
        typeOf<NamedDomainObjectProvider<Foo>>()
      )
    }
  }

  @Test
  fun `can access named element by getting`() {
    container.register("domainObject")

    container.apply {
      val domainObject by getting

      assertInferredTypeOf(domainObject, typeOf<DomainObject>())
      assertEquals("domainObject", domainObject.name)
    }
  }

  @Test
  fun `can configure named element by getting`() {
    container.register("domainObject")

    container.apply {
      val domainObject by getting { foo = "foo" }

      assertEquals("foo", domainObject.foo)
      assertEquals("foo", getByName("domainObject").foo)
    }
  }

  @Test
  fun `can access named element by getting with type`() {
    polymorphicContainer.register("domainObject", Foo::class.java)

    polymorphicContainer.apply {
      val domainObject by getting(Foo::class)

      assertInferredTypeOf(domainObject, typeOf<Foo>())
      assertEquals("domainObject", domainObject.name)
    }
  }

  @Test
  fun `can configure named element by getting with type`() {
    polymorphicContainer.register("domainObject", Foo::class.java)

    polymorphicContainer.apply {
      val domainObject by getting(Foo::class) { foo = "foo" }

      assertEquals("foo", domainObject.foo)
    }
  }
}
