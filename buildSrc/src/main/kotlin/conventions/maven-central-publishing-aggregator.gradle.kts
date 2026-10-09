package buildsrc.conventions

import buildsrc.settings.MavenPublishingSettings

plugins {
  base
  id("com.gradleup.nmcp.aggregation")
}

val mavenPublishing =
  extensions.create<MavenPublishingSettings>(MavenPublishingSettings.EXTENSION_NAME, project)

nmcpAggregation {
  centralPortal {
    username = mavenPublishing.mavenCentralUsername
    password = mavenPublishing.mavenCentralPassword

    // publish manually from the portal
    publishingType = "USER_MANAGED"
  }
  allowDuplicateProjectNames.set(true)
}

tasks.nmcpPublishAggregationToCentralPortal {
  val isReleaseVersion = mavenPublishing.isReleaseVersion
  onlyIf("is release version") { _ -> isReleaseVersion.get() }
}

tasks.nmcpPublishAggregationToCentralPortalSnapshots {
  val isReleaseVersion = mavenPublishing.isReleaseVersion
  onlyIf("is snapshot version") { _ -> !isReleaseVersion.get() }
}

tasks.register("nmcpPublish") {
  group = org.gradle.api.publish.plugins.PublishingPlugin.PUBLISH_TASK_GROUP
  description =
    "Lifecycle task. Runs either 'publish release' or 'publish snapshots' tasks, depending on whether project has a release or snapshot version."
  dependsOn(tasks.nmcpPublishAggregationToCentralPortal)
  dependsOn(tasks.nmcpPublishAggregationToCentralPortalSnapshots)
}
