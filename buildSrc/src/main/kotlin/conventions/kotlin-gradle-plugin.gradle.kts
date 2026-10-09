package buildsrc.conventions

plugins {
  id("buildsrc.conventions.kotlin-jvm")
  `java-gradle-plugin`
  id("buildsrc.conventions.publishing")
  id("com.gradle.plugin-publish")
}

tasks.validatePlugins {
  enableStricterValidation = true
}
