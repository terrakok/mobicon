# MobiCon — Agent Instructions

## Architecture

4 Gradle modules + 1 unmanaged iOS Xcode project:

| Module | Target | Entry Point |
|---|---|---|
| `sharedUI` | Android, JVM, JS, WasmJS, iOS | `sharedUI/src/commonMain/kotlin/.../App.kt` |
| `androidApp` | Android native | `AppActivity.kt` |
| `desktopApp` | macOS/Windows/Linux | `desktopApp/src/main/kotlin/main.kt` |
| `webApp` | JS + WasmJS browser | `webApp/src/commonMain/kotlin/main.kt` |
| `iosApp` | iOS (Xcode) | `sharedUI/src/iosMain/kotlin/main.kt` → `MainViewController()` |

All platform modules delegate to `sharedUI`'s `App()` composable. Business logic, data layer, and UI live in `sharedUI/src/commonMain/`.

**Navigation flow:** `App` → `RootContent` → 5 screens (EventsList, Event/Schedule, Session, Speaker, EventInfo). On desktop (≥800dp) Session/Speaker/EventInfo render as Navigation3 dialog overlays via `DesktopDialogSceneStrategy`.

**DI:** Metro (`dev.zacsweers.metro` 0.11.0) — `@DependencyGraph`, `@Inject`, `@ContributesBinding`, `@ViewModelKey`. Not Dagger/Hilt.

**Data:** Firestore (events list) + Sessionize API (sessions/speakers per event, URL from `sessionizeDataUrl`).

## Key Quirks

- **Compose builds:** Uses `1.11.0-beta02` from `packages.jetbrains.team/maven/p/cmp/dev`. Kotlin `2.3.20`.
- **Web navigation:** Only `webMain` uses `navigation3-browser` (`HierarchicalBrowserNavigation`). Android/iOS/desktop `BrowserNavigation` are no-ops.
- **iOS is not a Gradle module:** `iosApp/` is a standalone Xcode project that links the `SharedUI` static framework from `sharedUI` compilation.
- **Web targets both JS and WasmJS** via dual `browser()` compilations in `sharedUI` and `webApp`.
- **Custom scrollbar** on desktop via `composeunstyled` — Compose Multiplatform has no built-in scrollbar.

## Commands

```
./gradlew :androidApp:assembleDebug          # Android APK
./gradlew :desktopApp:run                   # Desktop (dev mode)
./gradlew :desktopApp:assemble              # Desktop distributables (DMG/MSI/DEB)
./gradlew :webApp:composeCompatibilityBrowserDistribution  # Web build (CI uses this)
./gradlew :sharedUI:compileKotlinIosArm64  # iOS framework
./gradlew clean                              # Remove all build dirs
```

CI (`deploy.yaml`) runs `./gradlew :webApp:composeCompatibilityBrowserDistribution` and deploys to `gh-pages` branch. Uses `composeCompatibilityBrowserDistribution` (not `composeBrowserDistribution`).

## Tests

Test dependencies are declared in `sharedUI/commonTest` but **no test source files exist**. Running `:sharedUI:test` or `:desktopApp:test` will find nothing.

## Constraints

- JVM/compile: Java 17 minimum
- Android: minSdk 23, compileSdk 36
- `gradle.properties` sets `kotlin.native.binary.gc=cms` and `kotlin.incremental.wasm=true`
- CI requires `GRADLE_CACHE_ENCRYPTION_KEY` secret for Gradle cache
- Web deployment: output at `webApp/build/dist/composeWebCompatibility/productionExecutable`
