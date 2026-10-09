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

package dev.adamko.gradle.kotlinaccessors

import dev.adamko.gradle.kotlinaccessors.internal.contains
import org.gradle.api.provider.Property
import org.gradle.util.GradleVersion

/**
 * Settings for [AccessorsSettingsPlugin].
 *
 * ```kotlin
 * // settings.gradle.kts
 * import dev.adamko.gradle.kotlinaccessors.AccessorsSettingsExtension.ImplicitImportsMode
 *
 * gradleKotlinAccessors {
 *   implicitImports = ImplicitImportsMode.Disabled
 * }
 * ```
 */
abstract class AccessorsSettingsExtension {

  /**
   * Whether to add the accessors to the implicit imports of precompiled script plugins.
   *
   * Relies on Gradle internals, so can be disabled. Defaults to [ImplicitImportsMode.Default].
   */
  abstract val implicitImports: Property<ImplicitImportsMode>

  sealed class ImplicitImportsMode {
    companion object {
      val Disabled: ImplicitImportsMode = DisabledInternal
      val InjectKotlinDslImplicitImportsArgUsingReflection: ImplicitImportsMode =
        InjectKotlinDslImplicitImportsArgUsingReflectionInternal
      /**
       * [InjectKotlinDslImplicitImportsArgUsingReflection] from Gradle 9.6 until 10, otherwise
       * [Disabled]: only those versions have deprecated accessors to outrank.
       */
      val Default: ImplicitImportsMode
        get() {
          // Use baseVersion, so `10.0-rc-1` counts as 10.0 rather than as something below it.
          val implicitImportRequired = GradleVersion.current().baseVersion in "9.6"..<"10.0"
          return if (implicitImportRequired) {
            InjectKotlinDslImplicitImportsArgUsingReflection
          } else {
            Disabled
          }
        }
    }

    data object InjectKotlinDslImplicitImportsArgUsingReflectionInternal : ImplicitImportsMode()
    data object DisabledInternal : ImplicitImportsMode()
  }

}
