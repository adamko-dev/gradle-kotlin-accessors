@file:Suppress("UnstableApiUsage")

rootProject.name = "gradle-kotlin-accessors"

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
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include(":modules:accessors")
include(":modules:accessors-core")
include(":modules:accessors-root")
include(":modules:accessors-dsl")
include(":modules:settings-plugin")
include(":modules:integration-tests")

//region git versioning
val gitLog: Provider<String> =
  providers.exec {
    workingDir(rootDir)
    commandLine(
      "git", "log", "-1",
      // Format as
      // - %h - abbreviated commit
      // - %n - newline
      // - %D - HEAD's refs -- "HEAD -> <branch>", "tag: <name>", ...
      "--format=%h%n%D",
      // Hardcode decorate-refs so local per-user settings cannot quietly override them.
      "--decorate-refs=HEAD",
      "--decorate-refs=refs/heads/*",
      "--decorate-refs=refs/tags/*",
    )
    isIgnoreExitValue = true
  }.standardOutput.asText.map { it.trim() }

/**
 * `git diff-index` exits 1 when a tracked file differs from `HEAD`
 * (same as `git describe --dirty`.)
 */
val gitDirty: Provider<Boolean> =
  providers.exec {
    workingDir(rootDir)
    commandLine("git", "diff-index", "--quiet", "HEAD")
    isIgnoreExitValue = true
  }.result.map { it.exitValue != 0 }

/** Overrides [gitVersion]. Set with `./gradlew -Pversion=1.2.3`. */
val standardVersion: Provider<String> = providers.gradleProperty("version")

val gitVersion: Provider<String> =
  gitLog.flatMap { log ->

    //region utils
    val headMarker = "HEAD -> "

    /** Matches a decoration entry for a simple SemVer tag. The group is the `major.minor.patch` digits. */
    val semverTagRegex = Regex("""tag: v((?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*))""")

    /** Untagged builds are snapshots. */
    fun snapshot(base: String): String = "$base-SNAPSHOT"
    //endregion

    val lines = log.lines()
    val shortCommit = lines.firstOrNull().orEmpty()
    // Absent when nothing points at HEAD, which is a detached checkout of a plain commit.
    val refs = lines.getOrNull(1).orEmpty().split(", ").map { it.trim() }

    val branch = refs.firstOrNull { it.startsWith(headMarker) }?.removePrefix(headMarker)
    val taggedVersion = refs
      .mapNotNull { semverTagRegex.matchEntire(it)?.groupValues?.last() }
      .maxWithOrNull(
        // Pick highest `major.minor.patch`. A commit could have more than one tag.
        compareBy(
          { it.substringBefore('.').toInt() },
          { it.substringAfter('.').substringBefore('.').toInt() },
          { it.substringAfterLast('.').toInt() },
        )
      )

    when {
      taggedVersion != null ->
        // If HEAD is on a branch or detached and there's a release tag, use the tag.
        gitDirty.map { dirty -> if (dirty) snapshot(branch ?: shortCommit) else taggedVersion }

      branch != null        ->
        providers.provider { snapshot(branch) }

      else                  ->
        // Detached, or no git repository at all, e.g. a downloaded zip archive.
        providers.provider { snapshot(shortCommit) }
    }
  }
    .map { version ->
      // control chars and slashes aren't allowed in Maven Versions
      version
        .map { c -> if (c.isISOControl() || c == '/' || c == '\\') "_" else c }
        .joinToString("")
    }
    .filter { it.isNotBlank() }
    .orElse("unknown")

gradle.allprojects {
  extensions.add<Provider<String>>("gitVersion", standardVersion.orElse(gitVersion))
  version = object {
    val gitVersion: Provider<String> get() = project.extensions.getByName<Provider<String>>("gitVersion")
    override fun toString(): String = gitVersion.orNull.toString()
  }
}
//endregion
