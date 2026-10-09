@file:Suppress("UnstableApiUsage")

rootProject.name = "buildSrc"

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

plugins {
  id("dev.adamko.gradle-kotlin-accessors") version "0.0.1"
}
