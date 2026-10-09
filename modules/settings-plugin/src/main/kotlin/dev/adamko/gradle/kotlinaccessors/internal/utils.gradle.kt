package dev.adamko.gradle.kotlinaccessors.internal

import org.gradle.util.GradleVersion

internal operator fun OpenEndRange<String>.contains(current: GradleVersion): Boolean {
  return current in GradleVersion.version(start)..<GradleVersion.version(endExclusive)
}
