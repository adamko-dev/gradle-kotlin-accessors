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
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectProvider
import dev.adamko.gradle.kotlin.dsl.existing as existingActual
import dev.adamko.gradle.kotlin.dsl.getValue as getValueActual
import dev.adamko.gradle.kotlin.dsl.getting as gettingActual
import dev.adamko.gradle.kotlin.dsl.provideDelegate as provideDelegateActual


/**
 * Idiomatic way of referring to the provider of a well-known element of a collection via a delegate property.
 *
 * `tasks { val jar by existing }`
 *
 * @param T the domain object type
 * @param C the concrete container type
 */
@get:JvmSynthetic
val <T : Any, C : NamedDomainObjectCollection<T>> C.existing: ExistingDomainObjectDelegateProvider<out C>
  get() = existingActual


/**
 * Idiomatic way of referring to the provider of a well-known element of a collection via a delegate property.
 *
 * `tasks { val jar by existing { ... } }`
 *
 * @param T the domain object type
 * @param C the concrete container type
 * @param action the configuration action
 */
@JvmSynthetic
fun <T : Any, C : NamedDomainObjectCollection<T>> C.existing(action: T.() -> Unit): ExistingDomainObjectDelegateProviderWithAction<out C, T> =
  existingActual(action)


/**
 * Idiomatic way of referring to the provider of a well-known element of a collection via a delegate property.
 *
 * `tasks { val jar by existing(Jar::class) }`
 *
 * @param T the domain object type
 * @param C the concrete container type
 * @param type the domain object type
 */
@JvmSynthetic
fun <T : Any, C : NamedDomainObjectCollection<T>, U : T> C.existing(type: KClass<U>): ExistingDomainObjectDelegateProviderWithType<out C, U> =
  existingActual(type)


/**
 * Idiomatic way of referring to the provider of a well-known element of a collection via a delegate property.
 *
 * `tasks { val jar by existing(Jar::class) { ... } }`
 *
 * @param T the domain object type
 * @param C the concrete container type
 * @param type the domain object type
 * @param action the configuration action
 */
@JvmSynthetic
fun <T : Any, C : NamedDomainObjectCollection<T>, U : T> C.existing(
  type: KClass<U>,
  action: U.() -> Unit
): ExistingDomainObjectDelegateProviderWithTypeAndAction<out C, U> =
  existingActual(type, action)

/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectCollection<T>> ExistingDomainObjectDelegateProvider<C>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<T>> =
  provideDelegateActual(receiver, property)


/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectCollection<T>> ExistingDomainObjectDelegateProviderWithAction<C, T>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<T>> =
  provideDelegateActual(receiver, property)

/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectCollection<T>, U : T> ExistingDomainObjectDelegateProviderWithType<C, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<U>> =
  provideDelegateActual(receiver, property)


/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectCollection<T>, U : T> ExistingDomainObjectDelegateProviderWithTypeAndAction<C, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<U>> =
  provideDelegateActual(receiver, property)


/**
 * Gets the delegate value.
 */
@JvmSynthetic
operator fun <T> ExistingDomainObjectDelegate<out T>.getValue(receiver: Any?, property: KProperty<*>): T =
  getValueActual(receiver, property)


/**
 * Idiomatic way of referring to an existing element in a collection
 * via a delegate property.
 *
 * `tasks { val jar by getting }`
 */
@get:JvmSynthetic
val <T : Any, U : NamedDomainObjectCollection<out T>> U.getting: NamedDomainObjectCollectionDelegateProvider<out T>
  get() = gettingActual


/**
 * Idiomatic way of referring and configuring an existing element in a collection
 * via a delegate property.
 *
 * `tasks { val jar by getting { group = "My" } }`
 */
@JvmSynthetic
fun <T : Any, U : NamedDomainObjectCollection<T>> U.getting(configuration: T.() -> Unit): NamedDomainObjectCollectionDelegateProvider<T> =
  gettingActual(configuration)


/**
 * Allows a [NamedDomainObjectCollection] to be used as a property delegate.
 *
 * @see [NamedDomainObjectCollection.named]
 */
@JvmSynthetic
operator fun <T : Any> NamedDomainObjectCollection<T>.provideDelegate(
  thisRef: Any?,
  property: KProperty<*>
): NamedDomainObjectProvider<T> =
  provideDelegateActual(thisRef, property)


/**
 * Allows a [NamedDomainObjectProvider] to be used as a property delegate.
 *
 * @see [NamedDomainObjectProvider.get]
 */
@JvmSynthetic
inline operator fun <T : Any, reified U : T> NamedDomainObjectProvider<out T>.getValue(
  thisRef: Any?,
  property: KProperty<*>,
): U =
  getValueActual(thisRef, property)
