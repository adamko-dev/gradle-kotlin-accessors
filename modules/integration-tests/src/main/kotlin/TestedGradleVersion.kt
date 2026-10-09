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

/** A Gradle version the integration tests run against, and the output the assertions match on. */
data class TestedGradleVersion(
  val version: String,
  /** Gradle's nag when a script uses its own deprecated delegated-property accessors. */
  val delegatedPropertyNag: Regex = Regex("""property delegate syntax has been deprecated"""),
  val kotlin: KotlinDiagnostics = KotlinDiagnostics(),
) {
  override fun toString(): String = version
}

/** Kotlin compiler diagnostics the tests match on. */
data class KotlinDiagnostics(
  val unusedReturnValue: Regex = Regex("""(?i)unused return value"""),
  val cannotAccessConstructor: Regex = Regex("""(?i)cannot access[^\n]*constructor"""),
  val becauseInternal: Regex = Regex("""it is internal"""),
  val unresolvedReference: Regex = Regex("""Unresolved reference"""),

  /** Gradle's heading for a build script that failed to compile, whatever the diagnostic. */
  val scriptCompilationError: Regex = Regex("""Script compilation error"""),
)

/** The Gradle versions every integration test runs against. */
internal val testedGradleVersions: List<TestedGradleVersion> = listOf(
  TestedGradleVersion("9.8.1"),
)
