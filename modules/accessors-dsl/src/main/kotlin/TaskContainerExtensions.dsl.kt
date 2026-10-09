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
@file:MustUseReturnValues

package org.gradle.kotlin.dsl

import kotlin.reflect.KProperty
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import dev.adamko.gradle.kotlin.dsl.provideDelegate as provideDelegateActual

/**
 * Provides a [TaskProvider] delegate for the task named after the property.
 */
@JvmSynthetic
operator fun ExistingDomainObjectDelegateProvider<out TaskContainer>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<TaskProvider<Task>> =
  provideDelegateActual(receiver, property)


/**
 * Provides a [TaskProvider] delegate for the task named after the property after configuring it with the given action.
 */
@JvmSynthetic
operator fun ExistingDomainObjectDelegateProviderWithAction<out TaskContainer, Task>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<TaskProvider<Task>> =
  provideDelegateActual(receiver, property)


/**
 * Provides a [TaskProvider] delegate for the task of the given type named after the property.
 */
@JvmSynthetic
operator fun <U : Task> ExistingDomainObjectDelegateProviderWithType<out TaskContainer, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<TaskProvider<U>> =
  provideDelegateActual(receiver, property)


/**
 * Provides a [TaskProvider] delegate for the task of the given type named after the property after configuring it with the given action.
 */
@JvmSynthetic
operator fun <U : Task> ExistingDomainObjectDelegateProviderWithTypeAndAction<out TaskContainer, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<TaskProvider<U>> =
  provideDelegateActual(receiver, property)


/**
 * Registers a task and provides a delegate with the resulting [TaskProvider].
 */
@JvmSynthetic
operator fun RegisteringDomainObjectDelegateProvider<out TaskContainer>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<TaskProvider<Task>> =
  provideDelegateActual(receiver, property)


/**
 * Registers a task that gets configured with the given action and provides a delegate with the resulting [TaskProvider].
 */
@JvmSynthetic
operator fun RegisteringDomainObjectDelegateProviderWithAction<out TaskContainer, Task>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<TaskProvider<Task>> =
  provideDelegateActual(receiver, property)


/**
 * Registers a task of the given type and provides a delegate with the resulting [TaskProvider].
 */
@JvmSynthetic
operator fun <U : Task> RegisteringDomainObjectDelegateProviderWithType<out TaskContainer, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<TaskProvider<U>> =
  provideDelegateActual(receiver, property)


/**
 * Registers a task of the given type that gets configured with the given action and provides a delegate with the resulting [TaskProvider].
 */
@JvmSynthetic
operator fun <U : Task> RegisteringDomainObjectDelegateProviderWithTypeAndAction<out TaskContainer, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<TaskProvider<U>> =
  provideDelegateActual(receiver, property)
