# CornCastle Anime / AniList Sync

A beautiful and feature-rich Android application that syncs with your AniList account. Track your anime watching progress, explore detailed anime information, and keep your favorites up-to-date!

## 📸 Screenshots
You can find the latest screenshots and download the release APK from the **[Releases](../../releases)** section of this repository.

## 🚀 Features
- **AniList Integration:** Authenticate with your AniList account securely.
- **Sync Progress:** Automatically sync your episode watch history.
- **Favorites & Watchlists:** Easily manage your favorites directly from the app.
- **Beautiful UI:** Built with Jetpack Compose featuring a modern, glassmorphic design and smooth animations.

## 📥 Download
The latest stable APK is available in the **[Releases](../../releases)** page. Simply download the `.apk` file and install it on your Android device.

## 🛠️ Setup & Development

### Prerequisites
- Android Studio (latest recommended)
- JDK 11+
- An AniList Developer Account

### Configuring API Keys
For security reasons, the AniList Client ID and Secret are **not** hardcoded in this repository. The app uses the Secrets Gradle Plugin to inject them at build time.

To build the app yourself:
1. Clone the repository.
2. Create a `.env` file in the root directory (you can copy `.env.example` if it exists).
3. Add your AniList credentials to the `.env` file:
   ```properties
   ANILIST_API_KEY=your_client_id_here
   ANILIST_CLIENT_SECRET=your_client_secret_here
   ```
4. Build and run the app from Android Studio!

## 📄 License
This project is licensed under the MIT License.
