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

package org.gradle.kotlin.dsl

import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KProperty
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ConsumableConfiguration
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.ResolvableConfiguration
import dev.adamko.gradle.kotlin.dsl.consumable as consumableActual
import dev.adamko.gradle.kotlin.dsl.dependencyScope as dependencyScopeActual
import dev.adamko.gradle.kotlin.dsl.provideDelegate as provideDelegateActual
import dev.adamko.gradle.kotlin.dsl.resolvable as resolvableActual


/**
 * Property delegate for registering a new dependency scope configuration.
 *
 * `val implementation by configurations.dependencyScope`
 */
@get:JvmSynthetic
val ConfigurationContainer.dependencyScope: RoleBasedConfigurationDelegateProvider<DependencyScopeConfiguration>
  get() = dependencyScopeActual


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
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
fun ConfigurationContainer.dependencyScope(
  action: DependencyScopeConfiguration.() -> Unit,
): RoleBasedConfigurationDelegateProvider<DependencyScopeConfiguration> =
  dependencyScopeActual(action)


/**
 * Property delegate for registering a new resolvable configuration.
 *
 * `val compileClasspath by configurations.resolvable`
 */
@get:JvmSynthetic
val ConfigurationContainer.resolvable: RoleBasedConfigurationDelegateProvider<ResolvableConfiguration>
  get() = resolvableActual


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
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
fun ConfigurationContainer.resolvable(
  action: ResolvableConfiguration.() -> Unit,
): RoleBasedConfigurationDelegateProvider<ResolvableConfiguration> =
  resolvableActual(action)


/**
 * Property delegate for registering a new consumable configuration.
 *
 * `val apiElements by configurations.consumable`
 */
@get:JvmSynthetic
val ConfigurationContainer.consumable: RoleBasedConfigurationDelegateProvider<ConsumableConfiguration>
  get() = consumableActual


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
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
fun ConfigurationContainer.consumable(
  action: ConsumableConfiguration.() -> Unit,
): RoleBasedConfigurationDelegateProvider<ConsumableConfiguration> =
  consumableActual(action)


/**
 * Registers a configuration in the role the accessor named, and provides a delegate with the
 * resulting [NamedDomainObjectProvider].
 */
@JvmSynthetic
operator fun <T : Any> RoleBasedConfigurationDelegateProvider<T>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<T>> =
  provideDelegateActual(receiver, property)

/** Alias for [dev.adamko.gradle.kotlin.dsl.RoleBasedConfigurationDelegateProvider] */
typealias RoleBasedConfigurationDelegateProvider<T> =
    dev.adamko.gradle.kotlin.dsl.RoleBasedConfigurationDelegateProvider<T>
