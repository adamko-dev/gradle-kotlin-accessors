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

import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

/** Asserts that nothing in this build nagged about deprecation. */
fun BuildResult.assertNoDeprecationNagging(gradle: TestedGradleVersion) {
  assertNoDelegatedPropertyNag(gradle)
  assertNoUnexpectedDeprecations(gradle)
}

/** Asserts the script resolved this library's accessors rather than Gradle's deprecated ones. */
fun BuildResult.assertNoDelegatedPropertyNag(gradle: TestedGradleVersion) {
  assertFalse(gradle.delegatedPropertyNag.containsMatchIn(output)) {
    "Expected the script to resolve this library's accessors, not Gradle's, on Gradle $gradle:\n$output"
  }
}

/** Asserts this build emitted no deprecation warnings. */
fun BuildResult.assertNoUnexpectedDeprecations(gradle: TestedGradleVersion) {
  val unexpected = output.lineSequence()
    .filter { it.contains("deprecat", ignoreCase = true) }
    .toList()

  assertTrue(unexpected.isEmpty()) {
    """
    |Unexpected deprecation warnings on Gradle $gradle:
    |${unexpected.joinToString("\n")}
    """.trimMargin()
  }
}

/**
 * Asserts that Gradle nags about its own delegated properties, in a build without this library.
 *
 * Calibrates [assertNoDelegatedPropertyNag], which passes vacuously if
 * [TestedGradleVersion.delegatedPropertyNag] stops matching.
 */
fun BuildResult.assertGradleNagsAboutDelegatedProperties(gradle: TestedGradleVersion) {
  assertTrue(gradle.delegatedPropertyNag.containsMatchIn(output)) {
    """
    |Gradle $gradle did not nag about the delegated property syntax.
    |Either Gradle stopped deprecating these accessors -- in which case this library's premise has
    |changed -- or the wording moved and `TestedGradleVersion.delegatedPropertyNag` needs updating.
    |Until it is, every `assertNoDelegatedPropertyNag` in this suite passes vacuously.
    |$output
    """.trimMargin()
  }
}
