---
name: dependency-updates
description: Update project dependencies to latest stable versions using the gradle-versions-plugin
---

# Dependency Updates Skill

When asked to update dependencies, follow this process:

## 1. Add the plugin

Add `com.github.ben-manes.versions` to `build.gradle.kts` (root):

```kotlin
plugins {
    // ... existing plugins ...
    id("com.github.ben-manes.versions") version "0.53.0"
}
```

## 2. Configure the task

Add this block to `build.gradle.kts` (root) to configure release-candidate filtering and parallel execution:

```kotlin
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}
```

## 3. Run dependencyUpdates

```bash
./gradlew dependencyUpdates -Drevision=release --no-parallel 2>&1
```

Parse the output. The "later release versions" section lists candidates. Filter:
- **Update**: stable releases used in `gradle/libs.versions.toml`
- **Skip**: pre-release (alpha/beta/RC/snapshot/dev), internal Android test plugins, versions requiring Gradle/AGP major bumps

## 4. Update versions

Edit `gradle/libs.versions.toml` `[versions]` section.

## 5. Align by Compose releases

When updating Compose Multiplatform or related AndroidX libraries:
1. Check the [Compose Multiplatform release page](https://github.com/JetBrains/compose-multiplatform/releases) for the target version
2. Extract the library versions from the "Libraries" table in the release notes
3. Update `libs.versions.toml` to match those versions exactly
4. Update related dependencies to their aligned versions (lifecycle, navigation3, adaptive, material3)

Example alignment from Compose v1.11.0-beta02:
- `compose-multiplatform = "1.11.0-beta02"`
- `androidx-lifecycle = "2.11.0-alpha03"` (from release table)
- `androidx-nav3 = "1.1.0-rc01"` (from release table)
- `androidx-adaptive = "1.3.0-alpha06"` (from release table)
- `material3 = "1.11.0-alpha06"` (from release table)

## 6. Handle migration breaking changes

Check for known breaking changes in major version bumps:

### Metro DI (@Assisted)
- **0.11.0 → 0.13.0+**: `@Assisted("identifier")` → `@Assisted` (name matching only)
- Fix all `@AssistedInject` classes: remove string identifiers from `@Assisted` annotations
- Fix all `@AssistedFactory` methods: remove string identifiers from `@Assisted` parameters
- Search for `@Assisted("` pattern in `src/` directories

### AGP version bumps
- Check if new version requires minimum Gradle version
- If so, update `gradle/wrapper/gradle-wrapper.properties`

### Kotlin version bumps
- Verify Compose Multiplatform compatibility (check release notes)
- May require `@OptIn(ExperimentalWasmDsl::class)` for `wasmJs` blocks

## 7. Verify the build

```bash
./gradlew :desktopApp:compileKotlin :androidApp:compileDebugKotlin --no-parallel
```

If compilation fails, read error logs and fix:
- Check for deprecation warnings that became errors
- Check for API changes in updated libraries
- Fix behavior changes (e.g., Metro's @Assisted migration)

## 8. Remove the plugin and task config

Delete the lines added in steps 1 and 2 from `build.gradle.kts`:
- Remove `id("com.github.ben-manes.versions") version "0.53.0"` from the plugins block
- Remove the `tasks.named<DependencyUpdatesTask>("dependencyUpdates")` configuration block

## 9. Report changes

Summarize what was updated in a table format:

| Library | Before | After |
|---|---|---|
| ktor | 3.4.0 | **3.4.2** |

## Special cases

- If only pre-release updates are available, inform the user and skip
- If AGP update requires Gradle bump, handle both together
- If Metro update requires migration, update all @Assisted usages across the codebase
- Never update to versions that break the build without explicit user approval
