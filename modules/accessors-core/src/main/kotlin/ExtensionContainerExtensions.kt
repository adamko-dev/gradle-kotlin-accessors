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
 *
 * Modifications:
 * - Removed deprecations.
 * - Added MustUseReturnValues and JvmSynthetic.
 */
@file:MustUseReturnValues

package dev.adamko.gradle.kotlin.dsl

import kotlin.reflect.KProperty
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.getByName

/**
 * Delegated property getter that locates extensions.
 */
@JvmSynthetic
inline operator fun <reified T : Any> ExtensionContainer.getValue(thisRef: Any?, property: KProperty<*>): T =
  getByName<T>(property.name)
