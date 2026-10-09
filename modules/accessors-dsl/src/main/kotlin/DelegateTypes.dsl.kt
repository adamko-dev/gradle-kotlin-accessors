/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.kotlin.dsl

// The delegate provider types returned by the accessors in this package, so that they can be named
// here too. Bounds are omitted because Kotlin does not allow them on type alias parameters; the
// underlying declarations still enforce them.

/** Alias for [dev.adamko.gradle.kotlin.dsl.ExistingDomainObjectDelegate] */
typealias ExistingDomainObjectDelegate<T> =
    dev.adamko.gradle.kotlin.dsl.ExistingDomainObjectDelegate<T>

/** Alias for [dev.adamko.gradle.kotlin.dsl.ExistingDomainObjectDelegateProvider] */
typealias ExistingDomainObjectDelegateProvider<T> =
    dev.adamko.gradle.kotlin.dsl.ExistingDomainObjectDelegateProvider<T>

/** Alias for [dev.adamko.gradle.kotlin.dsl.ExistingDomainObjectDelegateProviderWithAction] */
typealias ExistingDomainObjectDelegateProviderWithAction<C, T> =
    dev.adamko.gradle.kotlin.dsl.ExistingDomainObjectDelegateProviderWithAction<C, T>

/** Alias for [dev.adamko.gradle.kotlin.dsl.ExistingDomainObjectDelegateProviderWithType] */
typealias ExistingDomainObjectDelegateProviderWithType<T, U> =
    dev.adamko.gradle.kotlin.dsl.ExistingDomainObjectDelegateProviderWithType<T, U>

/** Alias for [dev.adamko.gradle.kotlin.dsl.ExistingDomainObjectDelegateProviderWithTypeAndAction] */
typealias ExistingDomainObjectDelegateProviderWithTypeAndAction<T, U> =
    dev.adamko.gradle.kotlin.dsl.ExistingDomainObjectDelegateProviderWithTypeAndAction<T, U>

/** Alias for [dev.adamko.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProvider] */
typealias RegisteringDomainObjectDelegateProvider<T> =
    dev.adamko.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProvider<T>

/** Alias for [dev.adamko.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithAction] */
typealias RegisteringDomainObjectDelegateProviderWithAction<C, T> =
    dev.adamko.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithAction<C, T>

/** Alias for [dev.adamko.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithType] */
typealias RegisteringDomainObjectDelegateProviderWithType<T, U> =
    dev.adamko.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithType<T, U>

/** Alias for [dev.adamko.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithTypeAndAction] */
typealias RegisteringDomainObjectDelegateProviderWithTypeAndAction<T, U> =
    dev.adamko.gradle.kotlin.dsl.RegisteringDomainObjectDelegateProviderWithTypeAndAction<T, U>

/** Alias for [dev.adamko.gradle.kotlin.dsl.NamedDomainObjectCollectionDelegateProvider] */
typealias NamedDomainObjectCollectionDelegateProvider<T> =
    dev.adamko.gradle.kotlin.dsl.NamedDomainObjectCollectionDelegateProvider<T>

/** Alias for [dev.adamko.gradle.kotlin.dsl.PolymorphicDomainObjectContainerGettingDelegateProvider] */
typealias PolymorphicDomainObjectContainerGettingDelegateProvider<T, U> =
    dev.adamko.gradle.kotlin.dsl.PolymorphicDomainObjectContainerGettingDelegateProvider<T, U>
