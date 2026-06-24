# 🚀 MTProxy-Finder

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**Smart Telegram MTProxy manager with speed test, bulk import, and multi-language support**

[Report Bug](https://github.com/iwnull/MTProxy-Finder/issues) · [Request Feature](https://github.com/iwnull/MTProxy-Finder/issues)

</div>

---

## 📱 About The Project

**MTProxy-Finder** is a fully native, modern Android application built with **Jetpack Compose** and **Material 3** design. It's specifically designed to test, fetch, and manage Telegram MTProxy servers efficiently.

Whether you're looking for the fastest connection or the most stable proxies, this app provides a seamless experience with intelligent filtering and real-time speed testing.

---

## ✨ Key Features

### 🧠 Smart Speed Test & Proxy Classification

| Feature | Description |
|---------|-------------|
| **Most Stable** | Intelligently filters and displays only **domain-based proxies** that are fully online and active. Temporary proxies and direct IPs are automatically filtered out. |
| **Fastest** | All active online proxies are sorted by **lowest ping** to ensure the best possible connection speed. |

### 🔄 Intelligent Incremental Update System

- When you tap the **"Get Proxies"** button, the app compares GitHub server content with your local database
- If no new updates exist, shows **"No new updates available"** to save your mobile data
- When new proxies are found, it fetches them and **automatically cleans outdated/inactive proxies** from your database

### 🌍 Multi-Language Support

| Language | Status |
|----------|--------|
| 🇮🇷 Persian (Farsi) | ✅ Full Support |
| 🇬🇧 English | ✅ Full Support |
| 🇷🇺 Russian | ✅ Full Support |

> **Dynamic RTL/LTR** switching based on selected language!

### 📦 Comprehensive Proxy Management

- **Bulk Import**: Copy and paste multiple proxy links or texts at once
- **Manual Single**: Define server, port, and secret code individually
- **Clean Database**: One-click cleanup with security confirmation
- **Bento Card Dashboard**: Real-time network stability percentage, active counts, and average ping with dynamic bar charts

### 🎨 Custom Modern Icon

A beautifully designed icon combining:
- ✈️ Telegram paper plane
- ⚡ Lightning bolt (speed indicator)
- 🛡️ Security shield

Set against a dark navy gradient background for a professional look.

---

## 📸 Screenshots

| Dashboard | Proxy List | Settings |
|-----------|------------|----------|
| *(Add screenshot here)* | *(Add screenshot here)* | *(Add screenshot here)* |

---

## 🛠️ Built With

- **[Kotlin](https://kotlinlang.org/)** - Primary programming language
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** - Modern UI toolkit
- **[Material 3](https://m3.material.io/)** - Design system
- **[Room Database](https://developer.android.com/training/data-storage/room)** - Local storage
- **[Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html)** - Asynchronous operations
- **[GitHub API](https://docs.github.com/en/rest)** - Proxy update fetching

---

## 🚀 Getting Started

### Prerequisites

- Android Studio **Flamingo** (2022.2.1) or higher
- JDK **11** or higher
- Android SDK **API 24+** (Android 7.0)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/iwnull/MTProxy-Finder.git
   Open in Android Studio

File → Open → Select the project folder

Build the project

Wait for Gradle sync to complete

Build → Make Project

Run the app

Select an emulator or physical device

Click Run ▶️

📥 Download APK
Option 1: Direct Download
Download Latest APK

Option 2: Build Yourself
bash
./gradlew assembleDebug
# or for release build
./gradlew assembleRelease
The APK will be generated at:

text
app/build/outputs/apk/debug/app-debug.apk
🎯 Roadmap
Core proxy management

Speed testing system

Multi-language support

Bulk import/export

Proxy sharing feature

Dark/Light theme toggle

Background update service

Widget support

🤝 Contributing
Contributions are what make the open-source community such an amazing place! Any contributions you make are greatly appreciated.

Fork the Project

Create your Feature Branch (git checkout -b feature/AmazingFeature)

Commit your Changes (git commit -m 'Add some AmazingFeature')

Push to the Branch (git push origin feature/AmazingFeature)

Open a Pull Request

📄 License
Distributed under the MIT License. See LICENSE file for more information.

text
MIT License

Copyright (c) 2026 iwnull

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
📞 Contact & Support
GitHub: @iwnull

Project Link: https://github.com/iwnull/MTProxy-Finder

Issues: Report Bug

🙏 Acknowledgments
Telegram MTProto Proxy - Protocol documentation

Jetpack Compose Documentation

All contributors and testers

<div align="center">
Made with ❤️ by iwnull

⭐ Star this repo if you find it useful!

</div>

