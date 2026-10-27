# T-UI Launcher Development & Deployment Guide

This document summarizes the essential commands for building, installing, and managing the T-UI Linux CLI Launcher fork.

## 🛠 Building the APK

To perform a clean build of the F-Droid version (includes SMS permissions):

```bash
# Ensure gradlew is executable
chmod +x gradlew

# Build the F-Droid Debug APK
./gradlew assembleFdroidDebug
```

**Output Path:** `app/build/outputs/apk/fdroid/debug/app-fdroid-debug.apk`

---

## 📱 Emulator Deployment (ADB)

To install the APK on your Pixel 9 Pro emulator (or any physical device connected via USB):

```bash
# 1. Start the emulator (if not already running)
emulator -avd Pixel_9_Pro -gpu host -accel on &

# 2. Wait for the device and install (overwriting existing)
adb wait-for-device
adb install -r app/build/outputs/apk/fdroid/debug/app-fdroid-debug.apk
```

---

## 🐧 BusyBox Management

The launcher now features a built-in BusyBox manager to enable standard Linux commands.

### Installation
In the T-UI terminal, type:
```bash
bbman -install
```
This downloads the architecture-specific ELF binary, verifies its **SHA-256 hash**, and sets up the environment.

### Usage
Once installed, you can use any Linux command directly:
```bash
ls -la
grep "search_term" file.txt
busybox --list
```

### Removal
To clean up the environment:
```bash
bbman -remove
```

---

## 📱 Termux Integration

T-UI supports executing Termux scripts and interacting with Tmux sessions via Termux's `RUN_COMMAND` Intent API (`com.termux.RUN_COMMAND`).

### Installation & Prerequisites
1. Install **Termux** from F-Droid or GitHub (avoid the deprecated Google Play Store version).
2. Install Termux packages required for your scripts (`pkg update && pkg install bash tmux`).
3. Enable external app execution in Termux by editing `~/.termux/termux.properties` and setting:
   ```properties
   allow-external-apps=true
   ```
4. Reload Termux settings: `termux-reload-settings`.

### Commands
- **Check Termux status:**
  ```bash
  termux status
  ```
- **Run a Termux script:**
  ```bash
  termux run script.sh
  ```
- **Connect/Attach to a Tmux session:**
  ```bash
  termux tmux
  ```
- **Create a persistent or floating script widget card:**
  ```bash
  visual script myscript.sh
  visual script myscript.sh -fixed
  ```

---

## 🗂 Storage Access Framework (SAF) & Modern Android (API 37)

To comply with Scoped Storage requirements on Android 10+ and modern API levels (API 37):
- File operations and document reading use SAF (`DocumentFile`, `ContentResolver`, and document tree intents) rather than raw legacy file system access.
- Users grant secure, granular directory permissions via Android's system document picker.

---

## ⚡ Gestures & Virtual Function Keys (Keybinds)

- **Gestures:**
  - **Swipe Up:** Opens search (`search`).
  - **Swipe Down:** Shows notifications (`notifications`).
  - **Swipe Left / Right:** Switches active workspace (`workspace next` / `workspace prev`).
- **Virtual Function Keys (`keybind`):**
  - Bind custom commands to virtual keys (F1 - F10):
    ```bash
    keybind F1 search google
    keybind F2 termux run backup.sh
    keybind list
    keybind remove F1
    ```
  - Simply type `F1`, `F2`, etc. in the terminal to execute the bound action.

---

## ⚙️ User-Defined Custom Commands (`command`)

Create powerful composite commands, sequential tasks separated by `;`, or Termux scripts:
- **Create a custom command:**
  ```bash
  command create G search google
  command create backup cp /sdcard/fotos /sdcard/backup
  command create ops clear; time; monitor
  ```
- **List custom commands:**
  ```bash
  command list
  ```
- **Remove a custom command:**
  ```bash
  command remove backup
  ```

---

## 📊 Comprehensive System Monitor (`monitor` / `visual sys`)

Real-time system readout monitoring CPU load, RAM usage, battery temperature, network I/O, and cellular signal:
- **Run monitor readout:**
  ```bash
  monitor
  monitor -crt
  monitor -nocrt
  ```
- **Create system monitor card:**
  ```bash
  visual sys
  visual sys -fixed
  ```

---

## 🔊 Text-to-Speech (TTS) & Optical Character Recognition (OCR)

- **Text-to-Speech (`tts`):**
  - Read text aloud using Android's system TTS engine:
    ```bash
    tts Hello from T-UI console launcher
    tts -stop
    ```
- **Optical Character Recognition (`ocr`):**
  - Extract text from an image (using file path or SAF content URI):
    ```bash
    ocr /sdcard/Download/document.jpg
    ocr content://com.android.providers.media.documents/document/image%3A1234
    ```
  - Extracted text is displayed in the terminal and automatically copied to the clipboard.

---

## 💬 Quick Assistant Floating Overlay (`overlay`)

Quickly access T-UI mini-terminal from any other app without returning to home screen:
- **Activate overlay floating bubble (`>_`):**
  ```bash
  overlay show
  ```
- **Deactivate overlay:**
  ```bash
  overlay hide
  ```
- **Permissions required:**
  - Android will prompt for "Draw over other apps" (`SYSTEM_ALERT_WINDOW`) permission when running `overlay show` for the first time.

---

## 🎨 Themes & Customization (`theme`)

Cythernoir Launcher includes state-of-the-art terminal color palettes inspired by community favorites in 2026:
- **Available Presets:**
  - `catppuccin_mocha` (Soft warm dark pastel)
  - `tokyo_night` (Smooth blue-purple OLED optimized)
  - `gruvbox_hard` (High contrast warm retro)
  - `catppuccin_latte` (Clean light mode)
  - `watchdogs`, `ghost_shell`, `cyberpunk`, `amber`, `nord`, `dracula`
- **Commands:**
  - **Preview a theme palette:**
    ```bash
    theme preview catppuccin_mocha
    ```
  - **Apply a theme preset:**
    ```bash
    theme preset catppuccin_mocha
    ```
- **Activate overlay floating bubble (`>_`):**
  ```bash
  overlay show
  ```
- **Deactivate overlay:**
  ```bash
  overlay hide
  ```
- **Permissions required:**
  - Android will prompt for "Draw over other apps" (`SYSTEM_ALERT_WINDOW`) permission when running `overlay show` for the first time.

---

## 🔍 Useful ADB Debugging Commands

```bash
# View real-time logs (filtered for T-UI)
adb logcat | grep ohi.andre

# Uninstall the launcher via ADB
adb uninstall ohi.andre.consolelauncher

# Push a file to the launcher's internal storage
adb push local_file.txt /data/user/0/ohi.andre.consolelauncher/files/
```

---

## 🛡 Security Note
All binaries are verified using hardcoded SHA-256 hashes found in `app/src/main/java/ohi/andre/consolelauncher/tuils/BusyBoxInstaller.java`. All network transport is forced over HTTPS.
