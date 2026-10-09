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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.kotlin.dsl.getByName

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
  get() = RegisteringDomainObjectDelegateProvider(this)


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
  RegisteringDomainObjectDelegateProviderWithAction(this, action)


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
  RegisteringDomainObjectDelegateProviderWithType(this, type)


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
  RegisteringDomainObjectDelegateProviderWithTypeAndAction(this, type, action)


/**
 * Registers an element and provides a delegate with the resulting [NamedDomainObjectProvider].
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectContainer<T>> RegisteringDomainObjectDelegateProvider<C>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<T>> = ExistingDomainObjectDelegate(
  delegateProvider.register(property.name)
)


/**
 * Registers an element and provides a delegate with the resulting [NamedDomainObjectProvider].
 */
@JvmSynthetic
operator fun <T : Any, C : NamedDomainObjectContainer<T>> RegisteringDomainObjectDelegateProviderWithAction<C, T>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<T>> = ExistingDomainObjectDelegate(
  delegateProvider.register(property.name, action)
)


/**
 * Registers an element and provides a delegate with the resulting [NamedDomainObjectProvider].
 */
@JvmSynthetic
operator fun <T : Any, C : PolymorphicDomainObjectContainer<T>, U : T> RegisteringDomainObjectDelegateProviderWithType<C, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<U>> = ExistingDomainObjectDelegate(
  delegateProvider.register(property.name, type.java)
)


/**
 * Registers an element and provides a delegate with the resulting [NamedDomainObjectProvider].
 */
@JvmSynthetic
operator fun <T : Any, C : PolymorphicDomainObjectContainer<T>, U : T> RegisteringDomainObjectDelegateProviderWithTypeAndAction<C, U>.provideDelegate(
  receiver: Any?,
  property: KProperty<*>,
): ExistingDomainObjectDelegate<NamedDomainObjectProvider<U>> = ExistingDomainObjectDelegate(
  delegateProvider.register(property.name, type.java, action)
)


/**
 * Holds the delegate provider for the `registering` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class RegisteringDomainObjectDelegateProvider<T> internal constructor(
  internal val delegateProvider: T
)


/**
 * Holds the delegate provider for the `registering` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class RegisteringDomainObjectDelegateProviderWithAction<C, T> internal constructor(
  internal val delegateProvider: C,
  internal val action: T.() -> Unit
)


/**
 * Holds the delegate provider and expected element type for the `registering` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class RegisteringDomainObjectDelegateProviderWithType<T, U : Any> internal constructor(
  internal val delegateProvider: T,
  internal val type: KClass<U>
)


/**
 * Holds the delegate provider and expected element type for the `registering` property delegate with
 * the purpose of providing specialized implementations for the `provideDelegate` operator
 * based on the static type of the provider.
 */
class RegisteringDomainObjectDelegateProviderWithTypeAndAction<T, U : Any> internal constructor(
  internal val delegateProvider: T,
  internal val type: KClass<U>,
  internal val action: U.() -> Unit
)


/**
 * Provides a property delegate that gets elements of the given [type] and applies the given [configuration].
 */
@JvmSynthetic
fun <T : Any, U : T> NamedDomainObjectContainer<T>.getting(
  type: KClass<U>,
  configuration: U.() -> Unit
): PolymorphicDomainObjectContainerGettingDelegateProvider<T, U> =
  PolymorphicDomainObjectContainerGettingDelegateProvider(this, type, configuration)


/**
 * Provides a property delegate that gets elements of the given [type].
 */
@JvmSynthetic
fun <T : Any, U : T> NamedDomainObjectContainer<T>.getting(type: KClass<U>): PolymorphicDomainObjectContainerGettingDelegateProvider<T, U> =
  PolymorphicDomainObjectContainerGettingDelegateProvider(this, type, configuration = null)


/**
 * A property delegate that gets elements of the given [type] from the given [container]
 * and applies the given [configuration].
 */
class PolymorphicDomainObjectContainerGettingDelegateProvider<T : Any, U : T> internal constructor(
  internal val container: NamedDomainObjectContainer<T>,
  internal val type: KClass<U>,
  internal val configuration: (U.() -> Unit)? = null
) {
  @JvmSynthetic
  operator fun provideDelegate(thisRef: Any?, property: KProperty<*>) =
    ExistingDomainObjectDelegate(
      when (configuration) {
        null -> container.getByName(property.name, type)
        else -> container.getByName(property.name, type, configuration)
      }
    )
}
