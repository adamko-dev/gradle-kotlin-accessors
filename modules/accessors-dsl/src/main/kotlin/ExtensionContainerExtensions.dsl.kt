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
@file:MustUseReturnValues

package org.gradle.kotlin.dsl

import kotlin.reflect.KProperty
import org.gradle.api.plugins.ExtensionContainer

import dev.adamko.gradle.kotlin.dsl.getValue  as getValueActual

/**
 * Delegated property getter that locates extensions.
 */
@JvmSynthetic
inline operator fun <reified T : Any> ExtensionContainer.getValue(thisRef: Any?, property: KProperty<*>): T =
  getValueActual(thisRef, property)
