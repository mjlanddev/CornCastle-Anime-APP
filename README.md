<div align="center">
  <h1>CornCastle Anime</h1>
  <p><i>A beautiful and feature-rich Android application that syncs with your AniList account. Track your anime watching progress, explore detailed anime information, and keep your favorites up-to-date!</i></p>

  <p align="center">
    <a href="https://kotlinlang.org/">
      <img src="https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white&style=flat-square">
    </a>
    <a href="https://developer.android.com/">
      <img src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white&style=flat-square">
    </a>
    <a href="https://developer.android.com/jetpack/compose">
      <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white&style=flat-square">
    </a>
    <a href="https://kotlinlang.org/docs/coroutines-overview.html">
      <img src="https://img.shields.io/badge/Coroutines-7F52FF?logo=kotlin&logoColor=white&style=flat-square">
    </a>
    <a href="https://developer.android.com/training/data-storage/room">
      <img src="https://img.shields.io/badge/Room%20Database-4285F4?logo=sqlite&logoColor=white&style=flat-square">
    </a>
    <a href="https://developer.android.com/guide/topics/media/exoplayer">
      <img src="https://img.shields.io/badge/ExoPlayer%20(Media3)-3DDC84?logo=vlcmediaplayer&logoColor=white&style=flat-square">
    </a>
    <a href="https://square.github.io/retrofit/">
      <img src="https://img.shields.io/badge/Retrofit-104217?logo=square&logoColor=white&style=flat-square">
    </a>
    <a href="https://coil-kt.github.io/coil/">
      <img src="https://img.shields.io/badge/Coil-000000?logo=android&logoColor=white&style=flat-square">
    </a>
  </p>

  <p align="center">
    <a href="https://github.com/mjlanddev/CornCastle-Anime-APP/releases/latest"><img src="https://img.shields.io/github/v/release/mjlanddev/CornCastle-Anime-APP?style=flat-square&color=blue"></a>
    <a href="https://github.com/mjlanddev/CornCastle-Anime-APP/"><img src="https://img.shields.io/github/stars/mjlanddev/CornCastle-Anime-APP?style=flat-square&color=yellow"></a>
    <a href="https://github.com/mjlanddev/CornCastle-Anime-APP/releases/latest"><img src="https://img.shields.io/github/downloads/mjlanddev/CornCastle-Anime-APP/total?style=flat-square&color=green"></a>
  </p>
</div>

<hr>

## 📸 Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center"><b>Home</b></td>
      <td align="center"><b>Search</b></td>
      <td align="center"><b>Airing Schedule</b></td>
    </tr>
    <tr>
      <td><img width="220" alt="Home" src="https://github.com/user-attachments/assets/94d7c1bf-a9a5-472a-b108-185290abcca0" /></td>
      <td><img width="220" alt="Search" src="https://github.com/user-attachments/assets/8ea3bb59-1d97-4406-a281-262685687918" /></td>
      <td><img width="220" alt="Airing Schedule" src="https://github.com/user-attachments/assets/b4eaf532-8555-4630-8406-452eca889b9b" /></td>
    </tr>
    <tr>
      <td align="center"><b>Details</b></td>
      <td align="center"><b>Watch</b></td>
      <td align="center"><b>Search Filters</b></td>
    </tr>
    <tr>
      <td><img width="220" alt="Details" src="https://github.com/user-attachments/assets/87973715-e6c2-469f-9637-0a1d4cc5ee34" /></td>
      <td><img width="220" alt="Watch" src="https://github.com/user-attachments/assets/ad91ea11-f714-4830-b8fc-a73ad2698271" /></td>
      <td><img width="220" alt="Search Filters" src="https://github.com/user-attachments/assets/8fc1a543-3224-4526-a27e-58fac665ba4f" /></td>
    </tr>
  </table>
</div>

## 🚀 Features
* **AniList Integration:** Authenticate with your AniList account securely to track everything you watch.
* **Sync Progress:** Automatically sync your episode watch history back to AniList.
* **Favorites & Watchlists:** Easily manage your favorites directly from the app interface.
* **Beautiful UI:** Built with Jetpack Compose featuring a modern, glassmorphic design, smooth micro-animations, and a premium aesthetic.
* **Smart Downloads:** Background downloading system with offline playback support.
* **Advanced Player:** Custom ExoPlayer implementation with AI subtitle translation, gesture controls, and dual-landscape auto-rotation.

## 📥 Download

The application is distributed as a side-loadable APK. You can download the latest stable version directly from the GitHub releases page.

<div align="center">
  <a href="https://github.com/mjlanddev/CornCastle-Anime-APP/releases/latest">
    <img src="https://img.shields.io/badge/Download-Latest_APK-blue?style=for-the-badge&logo=android">
  </a>
</div>

1. Go to the **[Releases](https://github.com/mjlanddev/CornCastle-Anime-APP/releases/latest)** page.
2. Download the `.apk` file from the assets section.
3. Open the downloaded file on your Android device and proceed with the installation.

## 🛠️ Setup & Development

If you're a developer wanting to build the project locally, follow these steps.

### Prerequisites
* Android Studio (Latest stable version recommended)
* JDK 17+ (Required by Gradle)
* An AniList Developer Account for API credentials

### Configuring API Keys
For security reasons, the AniList Client ID and Secret are **not** hardcoded in this repository. The app uses the Secrets Gradle Plugin to inject them at build time.

To build the app yourself:
1. Clone the repository.
2. Create a `local.properties` file in the root directory.
3. Add your AniList credentials to the `local.properties` file:
   ```properties
   ANILIST_API_KEY=your_client_id_here
   ANILIST_CLIENT_SECRET=your_client_secret_here
   ```
4. Build and run the app from Android Studio!

## 📄 License
This project is licensed under the [MIT License](LICENSE).

---

## ⚠️ Disclaimer

This project is an open-source anime streaming application created for **educational and personal use purposes only**.

### Content Disclaimer
* This application does **not host, store, upload, or distribute any copyrighted video content** on its own servers.
* Anime metadata, titles, descriptions, ratings, and poster images are obtained from third-party sources such as AniList and remain the property of their respective owners.
* Video content displayed through the application is provided by third-party services via external sources.
* The developers of this project do not control, own, or operate any external video sources linked through the application.

### Copyright
All trademarks, logos, anime titles, artwork, posters, and related intellectual property belong to their respective copyright holders. Their inclusion within this application is for informational and indexing purposes only.

### User Responsibility
Users are solely responsible for how they use this software and for complying with applicable copyright laws and regulations in their jurisdiction.

The developers, contributors, and maintainers of this project shall not be held liable for any misuse of the software, copyright infringement, or other unlawful activities carried out by users.

### DMCA / Content Removal
If you are a copyright owner and believe that any content accessible through this application infringes your rights, please contact the relevant content hosting provider directly. Since this application does not host the video content, removal requests should be directed to the source hosting the material.

### No Warranty
This software is provided "AS IS", without warranty of any kind, express or implied. The developers assume no responsibility for any damages, legal issues, or losses arising from the use of this project.
