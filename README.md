# Cellstrap

<p align="center">
  <img src="docs/logo.png" width="150" alt="Cellstrap Logo"/>
</p>

<p align="center">
  <strong>Next-generation Roblox bootstrapper for Android</strong><br/>
  Inspired by Voidstrap • Neon green • Fully open-source
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Min%20SDK-26%20(Android%208)-green"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-orange?logo=kotlin"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-blue"/>
  <img src="https://img.shields.io/badge/License-MIT-green"/>
</p>

---

## What is Cellstrap?

Cellstrap is a **Roblox Android bootstrapper** — a companion app that lets you manage, inject, and organize **FastFlags (FFlags)** for your Roblox mobile client. Inspired by Voidstrap Android, it brings desktop-class flag management to your phone.

**FastFlags** are internal configuration variables used by Roblox's engine. Injecting custom values lets you:
- Unlock FPS beyond the default cap
- Improve graphics quality or reduce it for performance
- Reduce network latency
- Disable telemetry
- And much more

---

## Features

| Feature | Details |
|---|---|
| ⚡ **FFlag Injection** | Writes `ClientAppSettings.json` to Roblox's Android data directory |
| 📋 **Profile System** | Create, manage and switch between named flag profiles |
| 🎯 **Built-in Presets** | 10 curated presets (Max FPS, Potato, Low Latency, HQ Graphics, etc.) |
| 🔍 **FFlag Browser** | Browse 25+ known flags with descriptions, types, and categories |
| 🎨 **Neon Green UI** | Sleek dark theme with glow accents inspired by Voidstrap |
| 📁 **Import/Export** | Share profiles as JSON |
| 🔄 **Roblox Detection** | Auto-detects Roblox install and version |
| 🚀 **Quick Launch** | Launch Roblox directly from the app after injecting |

---

## Requirements

- Android 8.0+ (API 26)
- Roblox installed (`com.roblox.client`)
- **"All Files Access" permission** (required to write to `Android/data/`)

> **Android 11+ Note:** Due to scoped storage restrictions, you must grant `MANAGE_EXTERNAL_STORAGE` (All Files Access) in Settings → Apps → Cellstrap → Permissions for flag injection to work.

---

## How It Works

Roblox on Android reads FFlags from:
```
/sdcard/Android/data/com.roblox.client/files/ClientSettings/ClientAppSettings.json
```

Cellstrap writes a valid `ClientAppSettings.json` to that location with your chosen flags. When Roblox launches, it reads this file and applies the flags.

### Flag Format
```json
{
  "DFIntTaskSchedulerTargetFps": "9999",
  "DFFlagDebugRenderForceTechnologyVoxel": "False",
  "FIntRenderShadowIntensity": "0"
}
```

> **Note:** As of September 2025, Roblox enforces a FastFlag Allowlist for `ClientAppSettings.json`. Only flags on Roblox's whitelist will take effect. Cellstrap is kept up to date with working flags.

---

## Screenshots

> Coming soon — build the project and run it on your device!

---

## Building

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17+

### Steps

```bash
git clone https://github.com/yourusername/cellstrap.git
cd cellstrap
```

Open in Android Studio → Build → Run on device or emulator.

Or build from CLI:
```bash
./gradlew assembleRelease
```

The APK will be at `app/build/outputs/apk/release/app-release.apk`.

---

## Project Structure

```
app/src/main/
├── java/com/cellstrap/app/
│   ├── MainActivity.kt              # App entry, navigation
│   ├── ui/
│   │   ├── CellstrapViewModel.kt    # State management
│   │   ├── theme/
│   │   │   ├── Theme.kt             # Neon green color scheme
│   │   │   └── Typography.kt        # Text styles
│   │   ├── components/
│   │   │   └── Components.kt        # Reusable UI components
│   │   └── screens/
│   │       ├── HomeScreen.kt        # Dashboard
│   │       ├── FFlagsScreen.kt      # FFlag editor
│   │       ├── PresetsScreen.kt     # Preset browser
│   │       ├── ProfilesScreen.kt    # Profile manager
│   │       └── SettingsScreen.kt    # App settings
│   └── data/
│       ├── models/
│       │   ├── FFlag.kt             # Data models
│       │   └── AppSettings.kt
│       └── repository/
│           └── FFlagRepository.kt   # FFlag read/write logic
└── res/
    ├── xml/
    │   ├── file_paths.xml
    │   └── network_security_config.xml
    └── values/
        ├── strings.xml
        └── themes.xml
```

---

## Built-in Presets

| Preset | Description |
|---|---|
| ⚡ Max FPS | Removes the FPS cap (9999 target) |
| 🥔 Potato Mode | Minimum graphics for max performance |
| 📶 Low Latency | Reduces network ping |
| 🎨 High Quality | Maxes out visual fidelity |
| ⚖️ Balanced | Best mix of performance and quality |
| 🔇 Disable Telemetry | Disables all Roblox analytics |
| 💬 Disable Chat | Disables in-game chat |
| 🌑 Better Shadows | Improved shadow rendering |
| 🌿 Disable Grass | Removes grass for performance |
| 🚀 Fast Startup | Reduces Roblox startup time |

---

## Disclaimer

Cellstrap is a third-party tool not affiliated with or endorsed by Roblox Corporation. Modifying FastFlags may be against Roblox's Terms of Service. Use at your own risk. The developers are not responsible for account actions taken by Roblox.

---

## License

MIT License — see [LICENSE](LICENSE) for details.

---

## Credits

- Inspired by [Voidstrap](https://github.com/voidstrap) and [Bloxstrap](https://github.com/bloxstraplabs/bloxstrap)
- FFlag research by the Roblox modding community
- Built with Jetpack Compose and ❤️
