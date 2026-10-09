package buildsrc.settings

import java.io.File
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType


/**
 * Settings for publishing.
 */
abstract class MavenPublishingSettings @Inject constructor(
  private val project: Project,
  private val providers: ProviderFactory,
) {

  /**
   * Whether the version is a SemVer release: `major.minor.patch`, optionally with a suffix.
   *
   * Matched by shape, not by a missing `-SNAPSHOT`, so a branch name or hash is never released.
   */
  val isReleaseVersion: Provider<Boolean> =
    providers.provider {
      val version = project.version.toString()
      !version.endsWith("-SNAPSHOT") && RELEASE_VERSION_REGEX.matches(version)
    }


  val sonatypeReleaseUrl: Provider<String> =
    isReleaseVersion.map { isRelease ->
      if (isRelease) {
        "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
      } else {
        "https://s01.oss.sonatype.org/content/repositories/snapshots/"
      }
    }
  val mavenCentralUsername: Provider<String> =
    gkaProp("mavenCentralUsername")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_USERNAME"))
  val mavenCentralPassword: Provider<String> =
    gkaProp("mavenCentralPassword")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_PASSWORD"))


  val adamkoDevReleaseUrl: Provider<String> =
    isReleaseVersion.map { isRelease ->
      if (isRelease) {
        "https://europe-west4-maven.pkg.dev/adamko-dev/adamko-dev-releases"
      } else {
        "https://europe-west4-maven.pkg.dev/adamko-dev/adamko-dev-snapshots"
      }
    }
  val adamkoDevUsername: Provider<String> =
    gkaProp("adamkoDevUsername")
      .orElse(providers.environmentVariable("MAVEN_ADAMKO_DEV_USERNAME"))
  val adamkoDevPassword: Provider<String> =
    gkaProp("adamkoDevPassword")
      .orElse(providers.environmentVariable("MAVEN_ADAMKO_DEV_PASSWORD"))


  val signingKeyId: Provider<String> =
    gkaProp("signing.keyId")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_SIGNING_KEY_ID"))
  val signingKey: Provider<String> =
    gkaProp("signing.key")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_SIGNING_KEY"))
  val signingPassword: Provider<String> =
    gkaProp("signing.password")
      .orElse(providers.environmentVariable("MAVEN_SONATYPE_SIGNING_PASSWORD"))


  private fun gkaProp(name: String): Provider<String> =
    providers.gradleProperty("dev.adamko.gradle.kotlinaccessors.$name")

  private fun <T : Any> gkaProp(name: String, convert: (String) -> T): Provider<T> =
    gkaProp(name).map(convert)

  companion object {
    const val EXTENSION_NAME = "mavenPublishing"

    /** @see isReleaseVersion */
    private val RELEASE_VERSION_REGEX = Regex("""\d+\.\d+\.\d+(-[\w.-]+)?""")

    /** Retrieve the [MavenPublishingSettings] extension. */
    internal val Project.mavenPublishing: MavenPublishingSettings
      get() = extensions.getByType()

    /** Configure the [MavenPublishingSettings] extension. */
    internal fun Project.mavenPublishing(configure: MavenPublishingSettings.() -> Unit) =
      extensions.configure(configure)
  }
}
