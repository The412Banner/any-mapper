# Any Mapper — Progress Log

## [init] — v1.0.0-pre — Initial project scaffold (2026-03-31)
**Commit:** TBD | **Tag:** v1.0.0-pre
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
