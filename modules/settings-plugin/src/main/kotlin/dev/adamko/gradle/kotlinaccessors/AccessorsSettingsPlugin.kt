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

package dev.adamko.gradle.kotlinaccessors

import dev.adamko.gradle.kotlinaccessors.AccessorsSettingsExtension.ImplicitImportsMode
import dev.adamko.gradle.kotlinaccessors.internal.GradleKotlinAccessorsBuildMetadata
import java.lang.reflect.Proxy
import kotlin.jvm.optionals.getOrNull
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider

/**
 * Puts the accessors on every build script's classpath, via the settings classpath, and declares
 * the `gradleKotlinAccessorsLibs` version catalog.
 */
abstract class AccessorsSettingsPlugin : Plugin<Settings> {

  override fun apply(settings: Settings) {
    val extension = createExtension(settings)
    registerVersionCatalog(settings)
    addAccessorsToPrecompiledScriptPluginImports(settings, extension)
  }

  private fun createExtension(settings: Settings): AccessorsSettingsExtension {
    return settings.extensions.create(
      "gradleKotlinAccessors",
      AccessorsSettingsExtension::class.java,
    ).apply {
      implicitImports.convention(ImplicitImportsMode.Default)
    }
  }

  private fun registerVersionCatalog(settings: Settings) {
    settings.dependencyResolutionManagement.versionCatalogs.register(CATALOG_NAME) { catalog ->
      val gkaVersionRef = catalog.version(CATALOG_NAME, GradleKotlinAccessorsBuildMetadata.version)

      GradleKotlinAccessorsBuildMetadata.Module.entries.forEach { m ->
        catalog.library(aliasOf(m.artifact), m.group, m.artifact).versionRef(gkaVersionRef)
      }
    }
  }

  /**
   * For each `kotlin-dsl` project, adds the `accessors` dependency and, per
   * [AccessorsSettingsExtension.implicitImports], adds the accessors to the implicit imports of its
   * precompiled script plugins.
   *
   * Gradle passes those imports in one `-Xscript-resolver-environment` argument, with no hook to
   * change it. So this rebuilds the value with `resolverEnvironmentStringFor`, adds the accessors,
   * and appends it as a later argument, which wins.
   *
   * Reflective and best-effort: any failure leaves the build unchanged.
   */
  private fun addAccessorsToPrecompiledScriptPluginImports(
    settings: Settings,
    extension: AccessorsSettingsExtension,
  ) {
    settings.gradle.beforeProject { project ->
      project.pluginManager.withPlugin(KOTLIN_DSL_PLUGIN_ID) { _ ->
        addAccessorDependency(project)
        handleImplicitImports(project, extension)
      }
    }
  }

  private fun addAccessorDependency(project: Project) {
    val libs = project.extensions.getByType(VersionCatalogsExtension::class.java).named(CATALOG_NAME)
    val accessorsLib = libs.findLibrary(GradleKotlinAccessorsBuildMetadata.Module.Accessors.artifact).getOrNull()
    if (accessorsLib != null) {
      project.configurations
        .matching { it.name == "implementation" }
        .configureEach { c ->
          c.dependencies.addLater(accessorsLib)
        }
    }
  }

  private fun handleImplicitImports(
    project: Project,
    extension: AccessorsSettingsExtension,
  ) {
    when (extension.implicitImports.orNull) {
      null, ImplicitImportsMode.DisabledInternal                                   ->
        return

      ImplicitImportsMode.InjectKotlinDslImplicitImportsArgUsingReflectionInternal -> {
        runCatching { injectImplicitImports(project) }
          .onFailure { ex ->
            project.logger.info("[$PLUGIN_NAME] implicit imports not injected: $ex")
          }
      }
    }
  }

  private fun injectImplicitImports(project: Project) {
    val accessorsTask = project.tasks.named(ACCESSORS_METADATA_TASK).get()
    val loader = accessorsTask.javaClass.classLoader

    val resolverEnvironment = resolverEnvironmentStringFor(
      loader = loader,
      implicitImports = project.gradleService(loader, IMPLICIT_IMPORTS_SERVICE),
      pluginEntryCache = pluginEntryCacheProxy(loader),
      classPath = project.mainCompileClasspath(),
      metadataDir = accessorsTask.call("getMetadataOutputDir"),
    )

    @Suppress("UNCHECKED_CAST")
    val merged = (resolverEnvironment as Provider<String>)
      .map { "-Xscript-resolver-environment=" + mergeAccessorsPackage(it) }

    project.tasks.named(MAIN_KOTLIN_COMPILE_TASK).configure { task ->
      task.freeCompilerArgs().add(merged)
    }
  }

  companion object {
    private const val CATALOG_NAME = "gradleKotlinAccessorsLibs"

    private const val PLUGIN_NAME = "gradle-kotlin-accessors"

    private const val KOTLIN_DSL_PLUGIN_ID = "org.gradle.kotlin.kotlin-dsl"

    private const val MAIN_KOTLIN_COMPILE_TASK = "compileKotlin"

    private const val ACCESSORS_METADATA_TASK = "generatePrecompiledScriptPluginAccessors"

    private const val IMPLICIT_IMPORTS_SERVICE = "org.gradle.kotlin.dsl.support.ImplicitImports"

    private const val PLUGIN_ENTRY_CACHE = "org.gradle.kotlin.dsl.internal.sharedruntime.codegen.PluginEntryCache"

    private const val RESOLVER_ENVIRONMENT_FACADE =
      "org.gradle.kotlin.dsl.provider.plugins.precompiled.tasks.ConfigurePrecompiledScriptDependenciesResolverKt"

    private const val ACCESSORS_PACKAGE = "dev.adamko.gradle.kotlin.dsl"

    /**
     * Imported by name rather than as `$ACCESSORS_PACKAGE.*`: a star import only ties with
     * Gradle's own, while a single-name import outranks it.
     */
    private val ACCESSOR_NAMES = listOf(
      "registering", "existing", "getting",
      "provideDelegate", "getValue",
      "dependencyScope", "resolvable", "consumable",
    )

    private val IMPLICIT_IMPORTS_ENTRY = Regex("""kotlinDslImplicitImports="([^"]*)"""")

    /** Appends the accessors to the implicit imports, after Gradle's own so they win. */
    private fun mergeAccessorsPackage(resolverEnvironment: String): String =
      IMPLICIT_IMPORTS_ENTRY.replace(resolverEnvironment) { match ->
        val ours = ACCESSOR_NAMES.joinToString(":") { "$ACCESSORS_PACKAGE.$it" }
        """kotlinDslImplicitImports="${match.groupValues[1]}:$ours""""
      }

    /** Calls a no-arg method reflectively. */
    private fun Any.call(method: String): Any =
      javaClass.methods.first { it.name == method && it.parameterCount == 0 }
        .apply { isAccessible = true }
        .invoke(this)!!

    /** A build service, by class name, from the project's service registry. */
    private fun Project.gradleService(loader: ClassLoader, className: String): Any {
      val services = call("getServices")
      val type = loader.loadClass(className)
      return services.javaClass.methods
        .first { it.name == "get" && it.parameterCount == 1 && it.parameterTypes[0] == Class::class.java }
        .apply { isAccessible = true }
        .invoke(services, type)!!
    }

    /** The compile classpath Gradle itself passes when building the resolver environment. */
    private fun Project.mainCompileClasspath(): Any {
      val sourceSets = extensions.getByName("sourceSets")
      val main = sourceSets.javaClass.methods
        .first { it.name == "getByName" && it.parameterCount == 1 }
        .apply { isAccessible = true }
        .invoke(sourceSets, "main")!!
      return main.call("getCompileClasspath")
    }

    /** A pass-through: the cache only avoids rescanning jars, and this runs once. */
    private fun pluginEntryCacheProxy(loader: ClassLoader): Any {
      val type = loader.loadClass(PLUGIN_ENTRY_CACHE)
      return Proxy.newProxyInstance(loader, arrayOf(type)) { proxy, method, args ->
        when (method.name) {
          "computeIfAbsent" -> {
            val file = args!![0]
            val compute = args[1]!!
            compute.javaClass.methods
              .first { it.name == "invoke" && it.parameterCount == 1 }
              .apply { isAccessible = true }
              .invoke(compute, file)
          }

          "toString"        -> "$PLUGIN_NAME PluginEntryCache"
          "hashCode"        -> System.identityHashCode(proxy)
          "equals"          -> proxy === args?.get(0)
          else              -> null
        }
      }
    }

    private fun resolverEnvironmentStringFor(
      loader: ClassLoader,
      implicitImports: Any,
      pluginEntryCache: Any,
      classPath: Any,
      metadataDir: Any,
    ): Any =
      loader.loadClass(RESOLVER_ENVIRONMENT_FACADE).methods
        .first { it.name == "resolverEnvironmentStringFor" && it.parameterCount == 4 }
        .apply { isAccessible = true }
        .invoke(null, implicitImports, pluginEntryCache, classPath, metadataDir)!!

    /** `compilerOptions.freeCompilerArgs` on a `KotlinCompile`, which lives in a child loader. */
    @Suppress("UNCHECKED_CAST")
    private fun Task.freeCompilerArgs(): ListProperty<String> =
      call("getCompilerOptions").call("getFreeCompilerArgs") as ListProperty<String>


    private val ALIAS_SEPARATOR = Regex("[-_]")

    /** Camel cased, so `settings-plugin` becomes `settingsPlugin`, not `settings.plugin`. */
    private fun aliasOf(artifact: String): String =
      artifact
        .split(ALIAS_SEPARATOR)
        .reduce { alias, part -> alias + part.replaceFirstChar { it.uppercaseChar() } }
  }
}
