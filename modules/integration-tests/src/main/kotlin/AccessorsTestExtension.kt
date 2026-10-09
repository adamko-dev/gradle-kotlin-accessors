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

import java.nio.file.FileSystemException
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import org.junit.jupiter.api.extension.*
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import org.junit.platform.commons.support.AnnotationSupport.isAnnotated

/** Runs each [AccessorsTest] once per Gradle version in [testedGradleVersions]. */
class AccessorsTestExtension : TestTemplateInvocationContextProvider {

  override fun supportsTestTemplate(context: ExtensionContext): Boolean =
    isAnnotated(context.testMethod, AccessorsTest::class.java)

  override fun provideTestTemplateInvocationContexts(
    context: ExtensionContext,
  ): Stream<out TestTemplateInvocationContext> =
    testedGradleVersions.map(::GradleVersionInvocation).stream()

  private class GradleVersionInvocation(
    private val gradle: TestedGradleVersion,
  ) : TestTemplateInvocationContext {

    override fun getDisplayName(invocationIndex: Int): String = "Gradle $gradle"

    override fun getAdditionalExtensions(): List<Extension> =
      listOf(ProjectParameterResolver(gradle), VersionParameterResolver(gradle))
  }

  private class ProjectParameterResolver(
    private val gradle: TestedGradleVersion,
  ) : ParameterResolver {

    override fun supportsParameter(
      parameterContext: ParameterContext,
      extensionContext: ExtensionContext,
    ): Boolean = parameterContext.parameter.type == AccessorsTestProject::class.java

    override fun resolveParameter(
      parameterContext: ParameterContext,
      extensionContext: ExtensionContext,
    ): AccessorsTestProject {
      val directory = extensionContext.getStore(NAMESPACE)
        .computeIfAbsent(
          PROJECT_DIRECTORY,
          { ProjectDirectory(extensionContext, gradle) },
          ProjectDirectory::class.java,
        )
      return AccessorsTestProject(directory.path, gradle)
    }
  }

  private class VersionParameterResolver(
    private val gradle: TestedGradleVersion,
  ) : ParameterResolver {

    override fun supportsParameter(
      parameterContext: ParameterContext,
      extensionContext: ExtensionContext,
    ): Boolean = parameterContext.parameter.type == TestedGradleVersion::class.java

    override fun resolveParameter(
      parameterContext: ParameterContext,
      extensionContext: ExtensionContext,
    ): TestedGradleVersion = gradle
  }

  /**
   * A re-implementation of `@TempDir`.
   */
  private class ProjectDirectory(
    private val context: ExtensionContext,
    gradle: TestedGradleVersion,
  ) : AutoCloseable {

    val path: Path = createTempDirectory(
      "${context.displayName.replace(Regex("[^A-Za-z0-9]+"), "-").take(60)}-$gradle-"
    )

    override fun close() {
      val mode = context
        .getConfigurationParameter(TempDir.DEFAULT_CLEANUP_MODE_PROPERTY_NAME, CleanupMode::valueOf)
        .orElse(CleanupMode.ALWAYS)
        ?: return

      fun cleanup() {
        try {
          path.deleteRecursively()
        } catch (ex: FileSystemException) {
          if (System.getProperty("os.name").lowercase().contains("windows")) {
            // Workaround for Gradle bug https://github.com/gradle/gradle/issues/39441
            System.err.println("Could not fully delete $path: $ex")
          } else {
            throw ex
          }
        }
      }

      when (mode) {
        CleanupMode.DEFAULT,
        CleanupMode.NEVER      -> return

        CleanupMode.ALWAYS     -> cleanup()
        CleanupMode.ON_SUCCESS -> {
          if (context.executionException.isPresent) return
          cleanup()
        }
      }
    }
  }

  private companion object {

    val NAMESPACE: Namespace = Namespace.create(AccessorsTestExtension::class.java)

    const val PROJECT_DIRECTORY = "project.directory"
  }
}
