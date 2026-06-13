# CornCastle Anime

A beautiful and feature-rich Android application that syncs with your AniList account. Track your anime watching progress, explore detailed anime information, and keep your favorites up-to-date!

## 📸 Screenshots

|  |  |  |
|--|--|--|
| Home | Search | Airing schedule |
| <img width="200" alt="home" src="https://github.com/user-attachments/assets/a1bef6a7-4525-4961-a21e-83245121ec72" /> | <img width="200" alt="serach" src="https://github.com/user-attachments/assets/38ade7e4-4105-4dec-b04f-1259327288c2" /> | <img width="200" alt="airing-ch" src="https://github.com/user-attachments/assets/46fa4c5a-bd11-4e3e-a2ce-b4938782a2ac" /> |
| Details | Watch | Serach Filters |
| <img width="200"  alt="details" src="https://github.com/user-attachments/assets/0f297a5d-23ad-4767-8174-ab30075dc106" /> | <img width="200"  alt="watch" src="https://github.com/user-attachments/assets/a01fc2c6-1289-4c1f-9d8e-9b4a18c05661" /> | <img width="200" alt="serach-flt" src="https://github.com/user-attachments/assets/8fc1a543-3224-4526-a27e-58fac665ba4f" /> |


## 🚀 Features
- **AniList Integration:** Authenticate with your AniList account securely.
- **Sync Progress:** Automatically sync your episode watch history.
- **Favorites & Watchlists:** Easily manage your favorites directly from the app.
- **Beautiful UI:** Built with Jetpack Compose featuring a modern, glassmorphic design and smooth animations.

## 📥 Download

Simply Click The Download APK Bwlow.

[⬇️ Download APK](https://github.com/mjlanddev/CornCastle-Anime-APP/releases/latest)



Or Manually go to the latest stable APK is available in the **[Releases](../../releases)** page. Simply download the `.apk` file and install it on your Android device.


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


# Disclaimer

This project is an open-source anime streaming application created for educational and personal use purposes only.

## Content Disclaimer

* This application does **not host, store, upload, or distribute any copyrighted video content** on its own servers.
* Anime metadata, titles, descriptions, ratings, and poster images are obtained from third-party sources such as AniList and remain the property of their respective owners.
* Video content displayed through the application is provided by third-party services via embedded players (iframes) or external sources.
* The developers of this project do not control, own, or operate any external video sources linked through the application.

## Copyright

All trademarks, logos, anime titles, artwork, posters, and related intellectual property belong to their respective copyright holders. Their inclusion within this application is for informational and indexing purposes only.

## User Responsibility

Users are solely responsible for how they use this software and for complying with applicable copyright laws and regulations in their jurisdiction.

The developers, contributors, and maintainers of this project shall not be held liable for any misuse of the software, copyright infringement, or other unlawful activities carried out by users.

## DMCA / Content Removal

If you are a copyright owner and believe that any content accessible through this application infringes your rights, please contact the relevant content hosting provider directly. Since this application does not host the video content, removal requests should be directed to the source hosting the material.

## No Warranty

This software is provided "AS IS", without warranty of any kind, express or implied. The developers assume no responsibility for any damages, legal issues, or losses arising from the use of this project.

