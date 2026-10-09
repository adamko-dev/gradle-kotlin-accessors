/*
 * Copyright 2016 the original author or authors.
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

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.PolymorphicDomainObjectContainer
import dev.adamko.gradle.kotlin.dsl.getting as gettingActual
import dev.adamko.gradle.kotlin.dsl.provideDelegate as provideDelegateActual
import dev.adamko.gradle.kotlin.dsl.registering as registeringActual


/**
 * Property delegate for registering new elements in the container.
 *
 * `tasks { val rebuild by registering }`
 *
 * @param T the domain object type
 * @param C the concrete container type
 */
@get:JvmSynthetic
val <T : Any, C : NamedDomainObjectContainer<T>> C.registering: RegisteringDomainObjectDelegateProvider<out C>
  get() = registeringActual


/**
 * Property delegate for registering new elements in the container.
 *
 * ```kotlin
 * tasks {
 *    val rebuild by registering {
 *        dependsOn("clean", "build")
 *    }
 * }
 * ```
 *
 * @param T the domain object type
 * @param C the concrete container type
 * @param action the configuration action
 */
@JvmSynthetic
fun <T : Any, C : NamedDomainObjectContainer<T>> C.registering(action: T.() -> Unit): RegisteringDomainObjectDelegateProviderWithAction<out C, T> =
  registeringActual(action)


/**
 * Property delegate for registering new elements in the container.
 *
 * `tasks { val jar by registering(Jar::class) }`
 *
 * @param T the domain object type
 * @param C the concrete container type
 * @param type the domain object type
 */
@JvmSynthetic
fun <T : Any, C : PolymorphicDomainObjectContainer<T>, U : T> C.registering(type: KClass<U>): RegisteringDomainObjectDelegateProviderWithType<out C, U> =
  registeringActual(type)


/**
 * Property delegate for registering new elements in the container.
 *
 * `tasks { val jar by registering(Jar::class) { } }`
 *
 * @param T the container element type
 * @param C the container type
 * @param U the desired domain object type
 * @param type the domain object type
 * @param action the configuration action
 */
@JvmSynthetic
fun <T : Any, C : PolymorphicDomainObjectContainer<T>, U : T> C.registering(
  type: KClass<U>,
  action: U.() -> Unit
): RegisteringDomainObjectDelegateProviderWithTypeAndAction<out C, U> =
  registeringActual(type, action)


/**
 * Registers an element and provides a delegate with the resulting [NamedDomainObjectProvider].
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectContainer<T>> RegisteringDomainObjectDelegateProvider<C>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<T>> =
  provideDelegateActual(receiver, property)


/**
 * Registers an element and provides a delegate with the resulting [NamedDomainObjectProvider].
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectContainer<T>> RegisteringDomainObjectDelegateProviderWithAction<C, T>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<T>> =
  provideDelegateActual(receiver, property)


/**
 * Registers an element and provides a delegate with the resulting [NamedDomainObjectProvider].
 */
@JvmSynthetic
operator fun <T : Any, C : PolymorphicDomainObjectContainer<T>, U : T> RegisteringDomainObjectDelegateProviderWithType<C, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<U>> =
  provideDelegateActual(receiver, property)


/**
 * Registers an element and provides a delegate with the resulting [NamedDomainObjectProvider].
 */
@JvmSynthetic
operator fun <T : Any, C : PolymorphicDomainObjectContainer<T>, U : T> RegisteringDomainObjectDelegateProviderWithTypeAndAction<C, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<U>> =
  provideDelegateActual(receiver, property)


/**
 * Provides a property delegate that gets elements of the given [type] and applies the given [configuration].
 */
@JvmSynthetic
fun <T : Any, U : T> NamedDomainObjectContainer<T>.getting(
  type: KClass<U>,
  configuration: U.() -> Unit
): PolymorphicDomainObjectContainerGettingDelegateProvider<T, U> =
  gettingActual(type, configuration)


/**
 * Provides a property delegate that gets elements of the given [type].
 */
@JvmSynthetic
fun <T : Any, U : T> NamedDomainObjectContainer<T>.getting(type: KClass<U>): PolymorphicDomainObjectContainerGettingDelegateProvider<T, U> =
  gettingActual(type)
