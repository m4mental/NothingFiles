# NOTHING FILES (2.0)

> A high-end, power-user Android file manager designed in the authentic **Nothing OS** aesthetic (pure OLED Black `#000000`, striking Nothing Red `#D71921`, and 5x7 dot-matrix typography). Packed with **MiXplorer-grade** capabilities.

---

## 🔴 Design System (Nothing OS Aesthetic)

- **Pure AMOLED Black (`#000000`)**: Deep blacks with zero light bleed.
- **Iconic Nothing Red (`#D71921`)**: Selection highlights, pulsating status LEDs, category accents, and primary action triggers.
- **NDot Dot Matrix Typography**: 5x7 LED matrix glyph generator for storage meters, file badges, and technical readouts.
- **Tactile Brutalist Wireframes**: 1dp crisp borders (`#262626`), 12–16dp curved corners, and widget-like cards.
- **Edge-to-Edge Experience**: Translucent navigation bar and pure black status bar.

---

## ⚡ Power-User Features (MiXplorer Grade)

### 1. Multi-Tab Navigation & Dual-View Ready
- Instant multi-tab switcher along the top bar (`STORAGE`, `ROOT`, `DOWNLOAD`, etc.).
- Add unlimited tabs with `+` button, close individual tabs, and maintain independent forward/back history stacks per tab.

### 2. Comprehensive File Operations
- **Full All Files Access** (`MANAGE_EXTERNAL_STORAGE`) with SAF fallback.
- **Batch Operations**: Multi-select mode with select-all, invert, copy, cut, paste buffer with sticky paste bar, and recursive delete.
- **Batch Rename Engine**: Prefix, suffix, find & replace, and automatic index numbering (`_1`, `_2`, `_3`...) with live preview.
- **File Permissions (chmod)**: Visual Linux permission editor with octal calculation (`755`, `644`, `rwxr-xr-x`).

### 3. Archive Engine
- **In-Archive Browsing**: Browse contents of `.zip`, `.apk`, `.jar` files without extracting.
- **Compress to ZIP**: Multi-file and directory recursive ZIP archive creation.
- **Extract All**: Path-safe archive extraction with live progress tracking.

### 4. App Manager
- List installed User and System applications.
- Inspect Package Name, Version, Min SDK, Target SDK, UID, and APK Size.
- **1-Tap APK Extraction**: Backs up any installed APK to `/Download/NothingFiles/Apks/`.
- Quick launch, uninstall, and system App Info actions.

### 5. Storage Space Analyzer
- Visual disk usage breakdown (Images, Videos, Audio, Documents, APKs, Archives).
- Dot-matrix storage usage bar with percentage indicator.
- **Top 50 Largest Files** scanner for quick cleanup.

### 6. Built-in Viewers & Editors
- **Code & Text Editor**: Monospace code viewer/editor with line numbers, file statistics, UTF-8 encoding, and instant save.
- **Hex Viewer**: Byte-by-byte hexadecimal inspector with 16-byte rows and ASCII sidebar.
- **Checksum / Hash Engine**: Streaming simultaneous computation of **MD5**, **SHA-1**, **SHA-256**, and **CRC32** with 1-tap clipboard copying.

### 7. Embedded Wi-Fi Web Share Server
- Built-in HTTP server running on port `8080`.
- Wirelessly browse, download, and stream phone storage from any PC, Mac, or mobile browser on the same Wi-Fi.
- Live Terminal console showing real-time HTTP requests and response codes.
- Foreground service with persistent notification keeping the server active.

---

## 📦 APK Location

The compiled debug APK is located at:
```
app/build/outputs/apk/debug/NothingFiles-debug.apk
```
