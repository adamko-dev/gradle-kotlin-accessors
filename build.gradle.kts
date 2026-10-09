@file:Suppress("UnstableApiUsage")

plugins {
  base
  id("buildsrc.conventions.maven-central-publishing-aggregator")
}

dependencies {
  nmcpAggregation(projects.modules.accessors)
  nmcpAggregation(projects.modules.accessorsCore)
  nmcpAggregation(projects.modules.accessorsDsl)
  nmcpAggregation(projects.modules.accessorsRoot)
  nmcpAggregation(projects.modules.settingsPlugin)
}
