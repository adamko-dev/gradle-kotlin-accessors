@file:Suppress("UnstableApiUsage")

plugins {
  id("buildsrc.conventions.kotlin-library")
}

description = "Gradle Kotlin DSL delegated-property accessors."

dependencies {
  compileOnly(gradleApi())
  compileOnly(gradleKotlinDsl())

  testFixturesCompileOnly(gradleApi())
  testFixturesCompileOnly(gradleKotlinDsl())
  testFixturesApi(platform(libs.junit.bom))
  testFixturesApi(libs.junit.jupiter)
}

testing {
  suites {
    withType<JvmTestSuite>().configureEach {
      useJUnitJupiter(libs.versions.junit)
    }

    // Unit tests: the accessors driven directly, against real containers from ProjectBuilder.
    val test by getting(JvmTestSuite::class) {
      dependencies {
        implementation(gradleApi())
        implementation(gradleKotlinDsl())
        implementation(project(":modules:accessors-root"))
        implementation(project(":modules:accessors-dsl"))
      }

      targets.configureEach {
        testTask.configure {
          // Required by ProjectBuilder
          jvmArgs(
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED"
          )
        }
      }
    }
  }
}
