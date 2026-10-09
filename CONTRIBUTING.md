# Contributing

How this project is built and tested. For what it is and how to use it, see
[README.md](README.md).

## Building

- `./gradlew check` all tests
- `./gradlew build` build everything

## Dogfooding

This project's own build scripts use a published release of the settings plugin, applied in
`buildSrc/settings.gradle.kts`.

After a release, bump that version to it. Each release is then built with the one before: 1.1.0
with 1.0.0.

## Versioning

From the git repository, in the `//region git versioning` block of `settings.gradle.kts`:

| git state                                 | version                                                 |
|-------------------------------------------|---------------------------------------------------------|
| clean, `v<major>.<minor>.<patch>` at HEAD | `<major>.<minor>.<patch>`                               |
| dirty, `v<major>.<minor>.<patch>` at HEAD | `<branch>-SNAPSHOT`, or `<hash>-SNAPSHOT` when detached |
| no tag at HEAD                            | `<branch>-SNAPSHOT`, or `<hash>-SNAPSHOT` when detached |
| no readable repository                    | `unknown-SNAPSHOT`                                      |

Override with `./gradlew -Pversion=1.2.3`.

## Publishing

The root project aggregates every module into one Maven Central deployment with
[nmcp](https://github.com/GradleUp/nmcp):

```shell
./gradlew nmcpPublish
```
