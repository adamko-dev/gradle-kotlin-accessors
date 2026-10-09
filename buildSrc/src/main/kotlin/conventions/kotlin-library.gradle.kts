package buildsrc.conventions

import org.gradle.api.attributes.Bundling.BUNDLING_ATTRIBUTE
import org.gradle.api.attributes.Bundling.EXTERNAL
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.DOCUMENTATION
import org.gradle.api.attributes.DocsType.*
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE

plugins {
  id("buildsrc.conventions.kotlin-jvm")
  id("buildsrc.conventions.publishing")
  `java-test-fixtures`
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
    }
  }
}

val sourcesElements by configurations.consumable {
  attributes {
    attribute(USAGE_ATTRIBUTE, objects.named(JAVA_RUNTIME))
    attribute(CATEGORY_ATTRIBUTE, objects.named(DOCUMENTATION))
    attribute(BUNDLING_ATTRIBUTE, objects.named(EXTERNAL))
    attribute(DOCS_TYPE_ATTRIBUTE, objects.named(SOURCES))
  }
  outgoing.artifact(tasks.kotlinSourcesJar)
}

val javadocJar by tasks.registering(Jar::class) {
  archiveClassifier.set("javadoc")
  from(
    resources.text.fromString(
      """
      |Intentionally empty.
      |""".trimMargin()
    )
  )
}

val javadocElements by configurations.consumable {
  attributes {
    attribute(USAGE_ATTRIBUTE, objects.named(JAVA_RUNTIME))
    attribute(CATEGORY_ATTRIBUTE, objects.named(DOCUMENTATION))
    attribute(BUNDLING_ATTRIBUTE, objects.named(EXTERNAL))
    attribute(DOCS_TYPE_ATTRIBUTE, objects.named(JAVADOC))
  }
  outgoing.artifact(javadocJar)
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.apply {
  addVariantsFromConfiguration(sourcesElements) {
    mapToMavenScope("runtime")
    mapToOptional()
  }
  addVariantsFromConfiguration(javadocElements) {
    mapToMavenScope("runtime")
    mapToOptional()
  }
}
