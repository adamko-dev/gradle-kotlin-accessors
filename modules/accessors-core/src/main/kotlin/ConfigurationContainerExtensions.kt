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
@file:MustUseReturnValues
@file:Suppress("UnstableApiUsage")

package dev.adamko.gradle.kotlin.dsl

import kotlin.reflect.KProperty
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ConsumableConfiguration
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.ResolvableConfiguration

/**
 * Property delegate for registering a new dependency scope configuration.
 *
 * `val implementation by configurations.dependencyScope`
 */
@get:JvmSynthetic
val ConfigurationContainer.dependencyScope: RoleBasedConfigurationDelegateProvider<DependencyScopeConfiguration>
  get() = RoleBasedConfigurationDelegateProvider { name -> dependencyScope(name) }


/**
 * Property delegate for registering a new dependency scope configuration.
 *
 * ```kotlin
 * val implementation by configurations.dependencyScope {
 *     description = "implementation dependencies"
 * }
 * ```
 *
 * @param action the configuration action
 */
@JvmSynthetic
fun ConfigurationContainer.dependencyScope(
  action: DependencyScopeConfiguration.() -> Unit,
): RoleBasedConfigurationDelegateProvider<DependencyScopeConfiguration> =
  RoleBasedConfigurationDelegateProvider { name -> dependencyScope(name, action) }


/**
 * Property delegate for registering a new resolvable configuration.
 *
 * `val compileClasspath by configurations.resolvable`
 */
@get:JvmSynthetic
val ConfigurationContainer.resolvable: RoleBasedConfigurationDelegateProvider<ResolvableConfiguration>
  get() = RoleBasedConfigurationDelegateProvider { name -> resolvable(name) }


/**
 * Property delegate for registering a new resolvable configuration.
 *
 * ```kotlin
 * val compileClasspath by configurations.resolvable {
 *     extendsFrom(implementation.get())
 * }
 * ```
 *
 * @param action the configuration action
 */
@JvmSynthetic
fun ConfigurationContainer.resolvable(
  action: ResolvableConfiguration.() -> Unit,
): RoleBasedConfigurationDelegateProvider<ResolvableConfiguration> =
  RoleBasedConfigurationDelegateProvider { name -> resolvable(name, action) }


/**
 * Property delegate for registering a new consumable configuration.
 *
 * `val apiElements by configurations.consumable`
 */
@get:JvmSynthetic
val ConfigurationContainer.consumable: RoleBasedConfigurationDelegateProvider<ConsumableConfiguration>
  get() = RoleBasedConfigurationDelegateProvider { name -> consumable(name) }


/**
 * Property delegate for registering a new consumable configuration.
 *
 * ```kotlin
 * val apiElements by configurations.consumable {
 *     extendsFrom(api.get())
 * }
 * ```
 *
 * @param action the configuration action
 */
@JvmSynthetic
fun ConfigurationContainer.consumable(
  action: ConsumableConfiguration.() -> Unit,
): RoleBasedConfigurationDelegateProvider<ConsumableConfiguration> =
  RoleBasedConfigurationDelegateProvider { name -> consumable(name, action) }


/**
 * Registers a configuration in the role the accessor named, and provides a delegate with the
 * resulting [NamedDomainObjectProvider].
 */
@JvmSynthetic
operator fun <T : Any> RoleBasedConfigurationDelegateProvider<T>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<T>> =
  ExistingDomainObjectDelegate(
    register(property.name)
  )


/** Holds a role-based registration until `provideDelegate` supplies the property name. */
class RoleBasedConfigurationDelegateProvider<T : Any> internal constructor(
  internal val register: (name: String) -> NamedDomainObjectProvider<T>
)
