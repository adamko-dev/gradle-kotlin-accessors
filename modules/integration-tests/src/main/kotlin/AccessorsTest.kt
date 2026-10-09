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

import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Marks an integration test, which runs once per Gradle version under test.
 *
 * The test takes what it needs as parameters: an [AccessorsTestProject] for a scratch project
 * pinned to that version, a [TestedGradleVersion] for the version itself, or both.
 *
 * ```kotlin
 * @AccessorsTest
 * fun `accessors resolve`(project: AccessorsTestProject, gradle: TestedGradleVersion) { }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@TestTemplate
@ExtendWith(AccessorsTestExtension::class)
annotation class AccessorsTest
