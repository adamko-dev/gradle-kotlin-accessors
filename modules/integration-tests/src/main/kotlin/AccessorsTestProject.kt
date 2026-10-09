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

import dev.adamko.gradle.dev_publish.devMavenRepo
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.writeText
import org.gradle.testkit.runner.GradleRunner

/**
 * One scratch Gradle project, to be built with one Gradle version.
 */
class AccessorsTestProject internal constructor(
  val projectDir: Path,
  val gradle: TestedGradleVersion,
) {

  init {
    write(
      "gradle.properties",
      """
      |org.gradle.jvmargs=-Dfile.encoding=UTF-8
      |org.gradle.caching=true
      |org.gradle.configuration-cache=true
      |org.gradle.logging.stacktrace=all
      |org.gradle.parallel=true
      |org.gradle.welcome=never
      |org.gradle.isolated-projects=true
      |""".trimMargin()
    )
  }

  /** A runner for this project, pinned to [gradle]. */
  fun runner(vararg arguments: String): GradleRunner =
    runnerIn(projectDir, *arguments)

  /** A runner for a build nested inside this one, pinned to [gradle]. */
  fun runnerIn(directory: Path, vararg arguments: String): GradleRunner =
    GradleRunner.create()
      .withProjectDir(directory.toFile())
      .withGradleVersion(gradle.version)
      .withArguments(*arguments)
      .forwardOutput()

  /**
   * A standalone build inside this one's directory:
   * `buildSrc`, an included build, or a library whose jar this project consumes.
   */
  fun nestedBuild(name: String): AccessorsTestProject =
    AccessorsTestProject(dir(name), gradle)

  fun dir(relative: String): Path = projectDir.resolve(relative).createDirectories()

  /** Writes [text] into a [projectDir] file at [relative], creating parent directories, with a trailing newline. */
  fun write(relative: String, text: String): Path =
    projectDir.resolve(relative)
      .also { it.parent.createDirectories() }
      .apply { writeText(text.trimEnd() + "\n") }

  fun writeSettingsScript(text: String): Path = write("settings.gradle.kts", text)

  fun writeBuildScript(text: String): Path = write("build.gradle.kts", text)

  /** A `buildscript` block putting the library on one script's classpath. */
  fun libraryOnBuildscriptClasspath(): String =
    """
    |buildscript {
    |  repositories { maven { url = uri("$devMavenRepo") } }
    |  dependencies { classpath("$accessorsCoordinates") }
    |}
    """.trimMargin()

  /** The settings `plugins` block that puts the library on every script's classpath. */
  fun settingsPluginBlock(): String =
    """plugins { id("$PLUGIN_ID") version "$accessorsVersion" }"""

  /** `pluginManagement` pointing at the dev repository, so the plugin id resolves. */
  fun pluginManagementBlock(): String =
    """
    |pluginManagement {
    |  repositories { maven { url = uri("$devMavenRepo") } }
    |}
    """.trimMargin()

  companion object {
    /** The build-local Maven repository that the dev-publish plugin published the library into. */
    val devMavenRepo: String by lazy { devMavenRepo().invariantSeparatorsPathString }

    private val testResourceProperties: Map<String, String> by lazy {
      val resourcePath = "/dev.adamko.gradle.kotlinaccessors.test-resources.properties"
      val resourceStream = {}::class.java.getResourceAsStream(resourcePath)
      requireNotNull(resourceStream) { "Could not find $resourcePath" }
      resourceStream.bufferedReader()
        .use { it.readText().trim() }
        .lines()
        .associate {
          it.split("=", limit = 2).run { first() to last() }
        }
    }

    val accessorsCoordinates: String by lazy {
      val projectVersion: String = testResourceProperties["projectVersion"]
        ?: error("Missing property 'projectVersion' in testResourceProperties.")
      "dev.adamko.gradle.kotlinaccessors:accessors:$projectVersion"
    }

    val accessorsVersion: String
      get() = accessorsCoordinates.substringAfterLast(':')

  }
}

const val PLUGIN_ID = "dev.adamko.gradle-kotlin-accessors"

/** The version catalog the settings plugin declares. */
const val CATALOG_NAME = "gradleKotlinAccessorsLibs"

const val ACCESSORS_GROUP = "dev.adamko.gradle.kotlinaccessors"

const val ACCESSORS_ARTIFACT = "accessors"

const val SETTINGS_PLUGIN_ARTIFACT = "settings-plugin"

/** The catalog aliases are the artifact ids, camel cased, so `settings-plugin` is one accessor. */
const val LIBRARY_ACCESSOR = "$CATALOG_NAME.$ACCESSORS_ARTIFACT"

const val SETTINGS_PLUGIN_ACCESSOR = "$CATALOG_NAME.settingsPlugin"

const val VERSION_ACCESSOR = "$CATALOG_NAME.versions.$CATALOG_NAME"
