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
</p>

<hr>

## ⚠️ Project Discontinued

**Due to legal reasons, all releases and updates for this application have been stopped, and existing releases are being removed.** Thank you to everyone who supported this project.

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
      <td><img width="220" alt="Home" src="https://github.com/user-attachments/assets/d177bbe9-1f8d-49e9-96db-8960ce5fabd9" /></td>
      <td><img width="220" alt="Search" src="https://github.com/user-attachments/assets/b60b8179-f443-454d-bda5-2956a7bb1be1" /></td>
      <td><img width="220" alt="Airing Schedule" src="https://github.com/user-attachments/assets/d51260a6-9b4b-4694-a132-f12d10fe6c15" /></td>
    </tr>
    <tr>
      <td align="center"><b>Details</b></td>
      <td align="center"><b>Watch</b></td>
      <td align="center"><b>Search Filters</b></td>
    </tr>
    <tr>
      <td><img width="220" alt="Details" src="https://github.com/user-attachments/assets/975ef57c-ede9-41ca-bafb-2b86b3056526"" /></td>
      <td><img width="220" alt="Watch" src="https://github.com/user-attachments/assets/bdc56d1a-f1d6-45f1-bbd7-dc74d746869c" /></td>
      <td><img width="220" alt="My Space" src="https://github.com/user-attachments/assets/d735f850-995f-4922-9eea-92b28a6bc208" /></td>
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

> **Note:** As noted above, releases are being discontinued and removed due to legal reasons. The information below is kept for historical reference.

The application was previously distributed as a side-loadable APK via the GitHub releases page.

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

This project was an open-source anime streaming application created for **educational and personal use purposes only**.

### Content Disclaimer
* This application does **not host, store, upload, or distribute any copyrighted video content** on its own servers.
* Anime metadata, titles, descriptions, ratings, and poster images are obtained from third-party sources such as AniList and remain the property of their respective owners.
* Video content displayed through the application was provided by third-party services via external sources.
* The developers of this project did not control, own, or operate any external video sources linked through the application.

### Copyright
All trademarks, logos, anime titles, artwork, posters, and related intellectual property belong to their respective copyright holders. Their inclusion within this application was for informational and indexing purposes only.

### User Responsibility
Users are solely responsible for how they used this software and for complying with applicable copyright laws and regulations in their jurisdiction.

The developers, contributors, and maintainers of this project shall not be held liable for any misuse of the software, copyright infringement, or other unlawful activities carried out by users.

### DMCA / Content Removal
If you are a copyright owner and believe that any content previously accessible through this application infringed your rights, please contact the relevant content hosting provider directly. Since this application did not host the video content, removal requests should be directed to the source hosting the material.

### No Warranty
This software was provided "AS IS", without warranty of any kind, express or implied. The developers assume no responsibility for any damages, legal issues, or losses arising from the use of this project.
