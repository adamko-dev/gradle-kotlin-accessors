import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion

plugins {
  `kotlin-dsl`
}

dependencies {
  compileOnly(embeddedKotlin("gradle-plugin"))
  implementation("org.gradle.kotlin:gradle-kotlin-dsl-plugins:${expectedKotlinDslPluginsVersion}")

  implementation(libs.gradlePlugin.pluginPublishPlugin)
  implementation(libs.gradlePlugin.nmcp)
  implementation(libs.gradlePlugin.devPublish)
}
