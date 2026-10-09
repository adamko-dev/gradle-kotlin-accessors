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

import java.nio.file.Path
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.reflect.TypeOf
import org.gradle.kotlin.dsl.typeOf
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertNotNull

/**
 * Support for the unit tests, which use real [ProjectBuilder] containers where Gradle's mock them.
 *
 * Public, as `internal` does not cross from `testFixtures` to `test`.
 */

fun buildProject(projectDir: Path): Project =
  ProjectBuilder.builder().withProjectDir(projectDir.toFile()).build()

/** A minimal element type for a container that is not one of Gradle's own. */
open class DomainObject(private val objectName: String) : Named {

  var foo: String? = null

  var bar: String? = null

  override fun getName() = objectName
}

/** The element hierarchy for a polymorphic container that is not a `TaskContainer`. */
abstract class DomainObjectBase(private val objectName: String) : Named {
  override fun getName() = objectName
}

open class Foo(objectName: String) : DomainObjectBase(objectName) {
  var foo: String? = null
}

/**
 * Asserts that the *static* type the compiler inferred for [value] is exactly [expectedType].
 *
 * Unlike `assertInstanceOf`, this sees type arguments erased at runtime, so it tells
 * `TaskProvider<Delete>` from `TaskProvider<Task>`.
 *
 * Adapted from Gradle's `assertInferredTypeOf(value: T, expectedType: TypeOf<T>)`, which infers `T`
 * from `expectedType` and so can never fail. Taking `TypeOf<*>` pins `T` to [value]'s static type.
 */
inline fun <reified T : Any> assertInferredTypeOf(
  value: T,
  expectedType: TypeOf<*>
) {
  assertNotNull(value) // dummy check, mostly just to avoid warning about `value` not being used.
  assertEquals(expectedType, typeOf<T>())
}
