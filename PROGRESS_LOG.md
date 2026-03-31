# Any Mapper — Progress Log

## [init] — v1.0.0-pre — Initial project scaffold (2026-03-31)
**Commit:** `ac8692e` | **Tag:** v1.0.0-pre
### What changed
- Full project scaffold: all phases P0-P5 implemented in initial commit
- Room DB: Mapping + Profile entities, DAOs, Repository
- InputInjector: reflection-based key + mouse event injection
- KeyCodeHelper: full button/axis/key name maps
- ControllerDetector: BT/USB gamepad detection
- MapperAccessibilityService: digital button + analog axis (API 33+) interception
- AxisProcessor: dead zone, curves, threshold-to-key
- MouseCursorManager: virtual cursor at 60fps
- Profiles: CRUD, auto-activate by package name
- Foreground notification: toggle + profile name
- UI: Onboarding, Home, MappingList, MappingEditor, ControllerTest, Profiles, Settings
- NavGraph: bottom nav 5 tabs
- 9 language string files: ES, FR, DE, JA, ZH, PT, RU, KO
- CI: build.yml with debug + release APK, pre-release + stable release publishing

## [fix] — v1.0.1-pre — Fix foreground service crash + Android 13+ restricted settings (2026-03-31)
**Commit:** `ad8bb07` + `722fc0f` + `1a849c2` | **Tag:** v1.0.1-pre
### What changed
- Removed `android:foregroundServiceType="specialUse"` — AccessibilityService cannot call startForeground, was crashing on API 35
- Replaced `startForeground()` with `NotificationManager.notify()` in MapperAccessibilityService
- Added Android 13+ restricted settings onboarding step (Settings → Apps → App Info → ⋮ → Allow restricted settings)
- Total onboarding steps: 4 on API 33+, 3 on lower
### Files touched
- `AndroidManifest.xml`
- `MapperAccessibilityService.kt`
- `OnboardingScreen.kt`

## [fix] — v1.0.2-pre — Fix B button backing out during mapping detection (2026-03-31)
**Commit:** `0f17004` | **Tag:** v1.0.2-pre
### What changed
- Added `isListeningForInput` companion object flag to MapperAccessibilityService
- While flag is true: onKeyEvent returns true for ALL events (consumes them), only emits ACTION_DOWN
- MappingEditorScreen sets flag true before `vm.detectedInput.first()`, clears on detect + DisposableEffect dispose
- Prevents KEYCODE_BACK from navigating away while waiting for a button press
### Files touched
- `MapperAccessibilityService.kt`
- `MappingEditorScreen.kt`

## [chore] — v1.0.3-pre — Test key signing + APK rename with version (2026-03-31)
**Commit:** `31304de` + `31bbf5a` | **Tag:** v1.0.3-pre
### What changed
- Added testkey.jks (RSA 2048, 10000 days); v1+v2+v3 signing in release + debug buildTypes
- CI renames APKs to `any-mapper-{version}.apk` / `any-mapper-{version}-debug.apk`
- `draft: false` on all release steps to prevent draft accumulation
### Files touched
- `app/build.gradle.kts`
- `app/testkey.jks` (new)
- `.github/workflows/build.yml`

## [feat] — v1.0.4-pre — Quick stick→mouse + ICP import with installed app picker (2026-03-31)
**Commit:** `3f84840` + `fbb7723` + `00ea9a8` + `50d48ec` + `e919e9a` + `43a9dc3` | **Tag:** v1.0.4-pre
### What changed
- MappingListScreen FAB expanded to mini-menu: Left Stick → Mouse, Right Stick → Mouse, Map a button
- `quickMapStickToMouse(rightStick)` creates two AXIS_FULL → MOUSE_MOVE_X/Y mappings at 2x sensitivity
- IcpImporter.kt (new): parses Winlator/GameNative `.icp` JSON → Mapping list
  - STICK → 4 axis threshold mappings; D_PAD → 4 DPAD button mappings; BUTTON → BUTTON_ORDER list
  - `icpKeyNameToKeyCode()` handles KEY_SPACE/BKSP/ESC/SHIFT/CTRL/ALT/arrows/F1-F12/letters
- ProfileScreen FAB mini-menu: "Import .icp file" + "New profile"
- After ICP load: app picker dialog (queryIntentActivities CATEGORY_LAUNCHER, searchable, sorted by label)
- Profile editor quick-select chips: Winlator, Winlator Ludashi (com.ludashi.benchmark), BannerHub, GHL
- createQuickSetupProfile packageName made nullable
### Files touched
- `MappingListScreen.kt`
- `MappingViewModel.kt`
- `ProfileScreen.kt`
- `IcpImporter.kt` (new)
- `MappingRepository.kt`

## [docs] — v1.0.5-pre — Add README (2026-03-31)
**Commit:** `3492182` | **Tag:** v1.0.5-pre
### What changed
- README.md: full feature list, Android 13+ install guide (restricted settings), setup guide with package name table, ICP import docs, how it works, build instructions, MIT license
### Files touched
- `README.md` (new)
