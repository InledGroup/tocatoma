# 💊 TOCATOMA
### *Your Vibrant, 3D Medication Companion*

![Status](https://img.shields.io/badge/Status-BETA-orange?style=for-the-badge)
![UI Language](https://img.shields.io/badge/UI_Language-Spanish_Only-red?style=for-the-badge)
![Platform](https://img.shields.io/badge/Platform-Android-green?style=for-the-badge)

**Tocatoma** is a high-performance, visually stunning medication manager for Android. Built with a "Duolingo-inspired" aesthetic, it turns the chore of tracking your health into a tactile and engaging experience.

---

## ✨ Key Features

- **🎨 Duolingo-Style 3D UI:** Large, bouncy buttons and blocks with real depth. Every interaction feels satisfying.
- **⏰ Persistent Smart Alarm:** Unlike standard notifications, Tocatoma launches a full-screen "Wake-up" intent that rings for up to 1 minute until you take action.
- **💤 Advanced Snooze:** Not ready? Postpone your dose by 5, 10, or 15 minutes with a single tap.
- **📅 Flexible Scheduling:** Pick specific days of the week for each medication. Your list only shows what you need *today*.
- **📸 Visual Memory:** Attach photos of your medication boxes to avoid confusion.
- **📊 Daily Progress:** Interactive "Check/Circle" system to track your intakes. Cards turn green once you're done!
- **💾 Data Sovereignty:** Your data stays on your device in a local **SQLite** database. Export and Import your entire history as a JSON file anytime.

---

## 🛠 Tech Stack

Designed with **Clean Architecture** (Hexagonal/DDD) and **Screaming Architecture** principles:

- **Frontend:** [Astro](https://astro.build/) (Strictly component-based, zero React overhead).
- **Styling:** [Tailwind CSS 4.0](https://tailwindcss.com/) & Custom 3D CSS primitives.
- **Native Bridge:** [Capacitor](https://capacitorjs.com/) with a custom Java **AlarmPlugin**.
- **Icons:** [Lucide Icons](https://lucide.dev/).
- **Persistence:** `@capacitor-community/sqlite`.

---

## ⚠️ Important Notes

### 🧪 Beta Version
This app is currently in **Beta**. We are actively refining the alarm persistence and background task handling for the latest Android versions (API 33+).

### 🇪🇸 Language Support
Please note that the **User Interface is currently available in Spanish only**. Multi-language support is planned for future releases.

---

## 🚀 Deployment & CI/CD

Tocatoma features a fully automated build pipeline using **GitHub Actions**.

- **Automatic APK Generation:** Every time a commit message contains the tag `[RELEASE]`, a GitHub Action triggers a full build, generating an installable Android APK available in the "Actions" tab.

---

## 🏗 Development

```bash
# Install dependencies
npm install

# Run web dev server
npm run dev

# Build web assets
npm run build

# Sync with Android project
npx cap sync android

# Open in Android Studio
npx cap open android
```

---

Developed with ❤️ by **JaimeGH**
*© 2026 Inled Group® - MIT-INLED Licensed.*
