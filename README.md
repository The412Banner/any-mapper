# Any Mapper

**Any Mapper** is an Android app that maps physical gamepad buttons and analog sticks to keyboard and mouse events — no root required. Inspired by [AntiMicroX](https://github.com/AntiMicroX/antimicrox) for Windows, it runs as a background accessibility service and is designed for Wine-based gaming apps on Android.

---

## Target Apps

- [Winlator](https://github.com/brunodev85/winlator) (`com.winlator`)
- Winlator Ludashi (`com.ludashi.benchmark`)
- [GameNative](https://gamenative.app)
- [BannerHub / GameHub](https://github.com/The412Banner/bannerhub) (`banner.hub`, `gamehub.lite`)

---

## Features

### Controller Mapping
- Map any **gamepad button** to a keyboard key, key combo, or mouse button
- Map **analog sticks** to mouse movement (X/Y axes) or WASD-style threshold keys
- Map **D-pad** hat axes to directional keys
- Per-mapping settings: dead zone, sensitivity, response curve (Linear / Quadratic / Cubic), hold behavior (Held / Toggle / Single Shot)

### Profiles
- Create multiple named profiles
- **Auto-activate** a profile when a specific app comes to the foreground (by package name)
- Import profiles directly from `.icp` controller files (Winlator/GameNative touch overlay format)
- Duplicate and rename profiles

### ICP Import
Any Mapper can import `.icp` controller profile files used by Winlator and GameNative touch overlays. On import:
- **STICK** elements → left stick axis threshold mappings
- **D_PAD** elements → D-pad button mappings
- **BUTTON** elements → assigned to physical gamepad buttons in standard order (A, B, X, Y, LB, RB, LT, RT, LS, RS, Start, Select)
- After picking the file, a searchable list of installed apps lets you set which app auto-activates the profile

### Quick Setup
- **Right/Left Stick → Mouse** — one tap creates both X and Y axis mouse mappings
- **Winlator FPS Profile** — pre-built profile with WASD movement, mouse look, jump, crouch, shoot, aim

### Controller Test
- Live input display — shows which button/axis is being pressed in real time
- Visual mapping list with active-button highlighting

### Other
- Works with Xbox, PlayStation, and generic Bluetooth/USB OTG gamepads
- 9 languages: English, Spanish, French, German, Japanese, Chinese (Simplified), Portuguese, Russian, Korean
- Foreground notification with quick enable/disable toggle
- No root required

---

## Requirements

- Android 8.0+ (API 26)
- Analog stick mapping requires Android 13+ (API 33)
- A Bluetooth or USB OTG gamepad

---

## Installation

> **Download the latest release:** [Releases](https://github.com/The412Banner/any-mapper/releases/latest)

Download `any-mapper-vX.X.X.apk` (release build).

### First Launch — Android 13+

Android 13 and above blocks sideloaded apps from using Accessibility by default. The onboarding will guide you through this, but the steps are:

1. **Settings → Apps → Any Mapper → ⋮ → Allow restricted settings**
2. **Settings → Accessibility → Any Mapper → Enable**
3. Return to the app

### First Launch — Android 12 and below

1. **Settings → Accessibility → Any Mapper → Enable**

---

## Setup Guide

### 1. Create a Profile
Go to the **Profiles** tab → tap **+** → **New profile** (or import an `.icp` file).

Set the auto-activate package to the app you're gaming with:

| App | Package |
|-----|---------|
| Winlator | `com.winlator` |
| Winlator Ludashi | `com.ludashi.benchmark` |
| BannerHub | `banner.hub` |
| GameHub Lite | `gamehub.lite` |

### 2. Add Mappings
Go to the **Mappings** tab → tap **+**:

- **Left/Right Stick → Mouse** — instantly maps the stick to mouse movement
- **Map a button** — press any button on your controller, then choose the target key or action

### 3. Enable the Service
Tap the power toggle on the **Home** tab, or use the notification.

### 4. Play
Launch your Wine app. Any Mapper will auto-switch to the matching profile and start translating controller input to keyboard/mouse events.

---

## How It Works

Any Mapper uses Android's **AccessibilityService** with `FLAG_REQUEST_FILTER_KEY_EVENTS` to intercept gamepad input before it reaches other apps. Mapped events are re-injected as keyboard or mouse events via `InputManager.injectInputEvent` (reflection — no root needed). The original gamepad event is consumed so the target app only sees the injected keyboard/mouse event.

---

## Building

```bash
git clone https://github.com/The412Banner/any-mapper.git
cd any-mapper
# Requires JDK 17 and Gradle 8.9
gradle :app:assembleRelease
```

CI uses GitHub Actions — see [`.github/workflows/build.yml`](.github/workflows/build.yml).

---

## License

MIT
