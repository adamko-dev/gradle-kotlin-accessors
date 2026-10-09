package buildsrc.conventions

import buildsrc.settings.MavenPublishingSettings

plugins {
  `maven-publish`
  signing
  id("com.gradleup.nmcp")
}

val mavenPublishing =
  extensions.create<MavenPublishingSettings>(MavenPublishingSettings.EXTENSION_NAME, project)

group = "dev.adamko.gradle.kotlinaccessors"
version = object {
  private val version: Provider<String> get() = project.extensions.getByName<Provider<String>>("gitVersion")
  override fun toString(): String = version.orNull ?: "unknown"
}

val projectUrl = "https://github.com/adamko-dev/gradle-kotlin-accessors"

publishing {
  publications.withType<MavenPublication>().configureEach {
    pom {
      name = "Gradle Kotlin Delegate Accessors"
      description = provider { project.description }
      url = projectUrl
      licenses {
        license {
          name = "The Apache License, Version 2.0"
          url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
        }
      }
      developers {
        developer {
          id = "aSemy"
          name = "aSemy"
          url = "https://github.com/aSemy"
        }
      }
      scm {
        url = projectUrl
        connection = "scm:git:$projectUrl.git"
        developerConnection = "scm:git:ssh://git@github.com/adamko-dev/gradle-kotlin-accessors.git"
      }
    }
  }
}

signing {
  val signingKeyId = mavenPublishing.signingKeyId.orNull
  val signingKey = mavenPublishing.signingKey.orNull
  val signingPassword = mavenPublishing.signingPassword.orNull

  if (signingKey != null && signingPassword != null) {
    if (signingKeyId != null) {
      useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    } else {
      useInMemoryPgpKeys(signingKey, signingPassword)
    }
    sign(publishing.publications)
  }
}

pluginManager.withPlugin("java-test-fixtures") {
  val javaComponent = components["java"] as AdhocComponentWithVariants
  javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
  javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
}
