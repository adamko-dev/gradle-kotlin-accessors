package buildsrc.conventions

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("org.gradle.kotlin.embedded-kotlin")
  id("dev.adamko.dev-publish")
}

group = "dev.adamko.gradle.kotlinaccessors"

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
    optIn.addAll(
      "kotlin.io.path.ExperimentalPathApi",
    )
    freeCompilerArgs.addAll(
      "-Xjsr305=strict",
      "-Xreturn-value-checker=check",
    )
  }
}

pluginManager.withPlugin("java") {
  configure<JavaPluginExtension> {
    toolchain {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
    }
  }
}

sourceSets {
  configureEach {
    java.setSrcDirs(emptyList<File>())
  }
}
