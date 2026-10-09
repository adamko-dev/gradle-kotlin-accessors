plugins {
  id("buildsrc.conventions.kotlin-library")
}

description = "Umbrella for the un-deprecated Gradle Kotlin DSL delegated-property accessors."

dependencies {
  api(project(":modules:accessors-core"))
  api(project(":modules:accessors-root"))
  api(project(":modules:accessors-dsl"))
}
