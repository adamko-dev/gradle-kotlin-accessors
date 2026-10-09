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
 *
 * Modifications:
 * - Removed deprecations.
 * - Added MustUseReturnValues and JvmSynthetic.
 */

@file:MustUseReturnValues

package dev.adamko.gradle.kotlin.dsl

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.kotlin.dsl.named

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
  get() = ExistingDomainObjectDelegateProvider(this)


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
  ExistingDomainObjectDelegateProviderWithAction(this, action)


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
  ExistingDomainObjectDelegateProviderWithType(this, type)


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
  ExistingDomainObjectDelegateProviderWithTypeAndAction(this, type, action)


/**
 * Holds the delegate provider for the `existing` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class ExistingDomainObjectDelegateProvider<T> internal constructor(
  internal val delegateProvider: T
)


/**
 * Holds the delegate provider for the `existing` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class ExistingDomainObjectDelegateProviderWithAction<C, T> internal constructor(
  internal val delegateProvider: C,
  internal val action: T.() -> Unit
)


/**
 * Holds the delegate provider and expected element type for the `existing` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class ExistingDomainObjectDelegateProviderWithType<T, U : Any> internal constructor(
  internal val delegateProvider: T,
  internal val type: KClass<U>
)


/**
 * Holds the delegate provider and expected element type for the `existing` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class ExistingDomainObjectDelegateProviderWithTypeAndAction<T, U : Any> internal constructor(
  internal val delegateProvider: T,
  internal val type: KClass<U>,
  internal val action: U.() -> Unit
)


/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectCollection<T>> ExistingDomainObjectDelegateProvider<C>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>
) = ExistingDomainObjectDelegate(
  delegateProvider.named(property.name)
)


/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectCollection<T>> ExistingDomainObjectDelegateProviderWithAction<C, T>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>
) = ExistingDomainObjectDelegate(
  delegateProvider.named(property.name).apply { configure(action) }
)


/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectCollection<T>, U : T> ExistingDomainObjectDelegateProviderWithType<C, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>
) = ExistingDomainObjectDelegate(
  delegateProvider.named(property.name, type)
)


/**
 * Provides access to the [NamedDomainObjectProvider] for the element of the given
 * property name from the container via a delegated property.
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectCollection<T>, U : T> ExistingDomainObjectDelegateProviderWithTypeAndAction<C, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>
) = ExistingDomainObjectDelegate(
  delegateProvider.named(property.name, type, action)
)


/**
 * Holds a property delegate with the purpose of providing specialized implementations for the
 * `getValue` operator based on the static type of the delegate.
 */
class ExistingDomainObjectDelegate<T> internal constructor(
  internal val delegate: T
)


/**
 * Gets the delegate value.
 */
@JvmSynthetic
operator fun <T> ExistingDomainObjectDelegate<out T>.getValue(receiver: Any?, property: KProperty<*>): T =
  delegate


/**
 * Idiomatic way of referring to an existing element in a collection
 * via a delegate property.
 *
 * `tasks { val jar by getting }`
 */
@get:JvmSynthetic
val <T : Any, U : NamedDomainObjectCollection<out T>> U.getting
  get() = NamedDomainObjectCollectionDelegateProvider(this, configuration = null)


/**
 * Idiomatic way of referring and configuring an existing element in a collection
 * via a delegate property.
 *
 * `tasks { val jar by getting { group = "My" } }`
 */
@JvmSynthetic
fun <T : Any, U : NamedDomainObjectCollection<T>> U.getting(configuration: T.() -> Unit) =
  NamedDomainObjectCollectionDelegateProvider(this, configuration)


/**
 * Enables typed access to container elements via delegated properties.
 */
class NamedDomainObjectCollectionDelegateProvider<T : Any> internal constructor(
  internal val collection: NamedDomainObjectCollection<T>,
  internal val configuration: (T.() -> Unit)?
) {
  @JvmSynthetic
  operator fun provideDelegate(thisRef: Any?, property: KProperty<*>) =
    ExistingDomainObjectDelegate(
      when (configuration) {
        null -> collection.getByName(property.name)
        else -> collection.getByName(property.name, configuration)
      }
    )
}


/**
 * Allows a [NamedDomainObjectCollection] to be used as a property delegate.
 *
 * @see [NamedDomainObjectCollection.named]
 */
@JvmSynthetic
operator fun <T : Any> NamedDomainObjectCollection<T>.provideDelegate(thisRef: Any?, property: KProperty<*>) =
  named(property.name)


/**
 * Allows a [NamedDomainObjectProvider] to be used as a property delegate.
 *
 * @see [NamedDomainObjectProvider.get]
 */
@JvmSynthetic
inline operator fun <T : Any, reified U : T> NamedDomainObjectProvider<out T>.getValue(
  thisRef: Any?,
  property: KProperty<*>
): U {
  val element = get()
  require(element is U) {
    "Element '${property.name}' of type '${element::class.java.name}' from container '$this' " +
        "cannot be cast to '${U::class.qualifiedName}'."
  }
  return element
}
