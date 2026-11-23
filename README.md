# بيتي | Beity — Real Estate Platform (Android)

**كل ما تحتاجه في عالم العقارات.**  
*Everything you need in the world of real estate.*

**بيتي | Beity** is a native Android real estate application built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Firebase (Firestore & Google Sign-In)**, designed in full **Arabic (RTL)** for the Egyptian and Arab property market.

---

## Visual Identity

- **Primary Navy:** `#071A3A`
- **Accent Gold:** `#D6AD55`
- **Clean Surface White:** `#FFFFFF`
- **Typography:** `Cairo` & `Readex Pro` (bundled locally in `app/src/main/res/font/`)
- **App Brand Icon:** Custom architectural house & modern towers adaptive launcher icon (`app/src/main/res/drawable/img_beity_icon.jpg` & `ic_launcher_foreground.xml`)

<p align="center">
  <img src="app/src/main/res/drawable/img_beity_icon.jpg" alt="Beity App Icon" width="140" />
</p>

---

## Screenshots & Demo Video

> **Note:** Capture live screenshots / screen recording from the **AI Studio Streaming Android Emulator** (or Android Studio's Device Manager), save them under `docs/media/`, and commit them to your GitHub repository.

### Demo Video
<!-- Drag & drop your .mp4 directly into the GitHub README editor, or commit docs/media/demo.mp4 and link it below -->
- **App Walkthrough Video:** `docs/media/beity_demo.mp4` *(add your recorded walkthrough here)*

### App Screenshots

| Splash & Sign-In | Home & Quick Search | Advanced Filters & Map |
| :---: | :---: | :---: |
| ![Splash](docs/media/01_splash_auth.png) | ![Home](docs/media/02_home.png) | ![Explore](docs/media/03_explore_map.png) |

| Property Details | Add / Edit Listing | Favorites & Compare |
| :---: | :---: | :---: |
| ![Details](docs/media/04_details.png) | ![Add Property](docs/media/05_add_property.png) | ![Compare](docs/media/06_favorites_compare.png) |

| In-App Chat & Alerts | Profile & My Listings | Admin Dashboard |
| :---: | :---: | :---: |
| ![Messages](docs/media/07_messages.png) | ![Profile](docs/media/08_profile.png) | ![Admin](docs/media/09_admin.png) |

---

## Core Features

### 1. Home & Navigation
- Animated luxury navy-and-gold **Splash Screen**.
- Custom top header with **BeityLogo**, quick navigation links (Home, Explore, Buy, Rent, Map, About, Contact), notifications badge, and quick Add Property CTA.
- Quick search card (Sale/Rent, Governorate, City, District, Property Type, Max Price).
- 7 real estate categories (Apartments, Villas, Lands, Commercial Shops, Administrative Offices, Chalets, Investment Properties).
- Live Firestore sections: **Featured**, **Latest**, **Best Value Opportunities**, **Suggested**, and **Most Viewed**.

### 2. Advanced Search & Interactive Map
- **16 real-time filter parameters**: query text, governorate, city, district, property type, listing type (sale/rent), min/max price, min/max area (m²), min bedrooms, min bathrooms, floor range, finishing type, furnished status, elevator, garage, balcony, and publication recency.
- **5 sorting modes**: Newest, Lowest Price, Highest Price, Most Viewed, Largest Area.
- **Search History** synced per user in Firestore.
- **Interactive Canvas Map** plotting Egyptian governorates and property pins with one-tap preview & filter.

### 3. Property Details & Engagement
- Swipeable photo gallery with fullscreen pinch-to-zoom lightbox and optional video link.
- Full specifications grid, features & amenities chips, and external map coordinate launcher (`geo:` URI).
- Direct **Call**, **WhatsApp**, **In-App Real-Time Chat**, **Share**, **Favorite**, **Compare (up to 3 properties side-by-side)**, **Reviews**, and **Report Listing**.

### 4. Accounts, Listings & Admin Moderation
- **Google Sign-In** via Jetpack `CredentialManager` (`GetSignInWithGoogleOption`).
- **Add/Edit Property** with zero-permission **Android Photo Picker** (`PickMultipleVisualMedia`) + JPEG compression, architectural presets, and automatic coordinate lookup.
- **My Properties Manager**: track moderation status (`pending`, `approved`, `rejected`, `paused`), edit, pause/republish, or delete listings.
- **Admin Dashboard** (restricted to verified admin account): KPI analytics, category distribution Canvas chart, listing approval/rejection/featuring, user account status & role management, report resolution, category/location management, and a one-tap **Demo Data Seeder** for testing.

### 5. Information, Support & Dark/Light Mode
- Persistent Dark/Light theme toggle stored via Jetpack **DataStore Preferences**.
- Dedicated pages for **About Us**, **Contact Us** (submits support tickets to Firestore), **FAQ**, **Privacy Policy**, **Terms & Conditions**, **Ad Publishing Policy**, and **Report an Issue**.

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (`compose-bom:2024.09.00`), Material Design 3, Navigation Compose, Coil
- **Architecture:** MVVM + Repository pattern (`ViewModel`, `StateFlow`, Kotlin Coroutines & Flow)
- **Backend & Auth:** Firebase Authentication + Google Identity (`androidx.credentials`, `googleid`), Cloud Firestore (named database)
- **Local Preferences:** Jetpack DataStore Preferences
- **Security & Testing:** Zero-trust `firestore.rules`, `@firebase/rules-unit-testing` emulator tests (`firestore.test.js`), JUnit4, and Robolectric

---

## Project Structure

```text
├── app/src/main/java/com/example/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── model/          # BeityModels.kt, EgyptCatalog.kt
│   │   └── repository/     # AuthManager, BeityRepository, AdminRepository, PreferencesRepository
│   └── ui/
│       ├── BeityApp.kt     # Root navigation shell (BottomBar / NavigationRail)
│       ├── components/     # BeityLogo, PropertyCard, ImageHelper, InteractiveMapCanvas
│       ├── screens/        # Splash, Auth, Home, Explore, Detail, AddEdit, FavoritesCompare,
│       │                   # Messages, Profile, AdminDashboard, InfoPages
│       ├── theme/          # Color.kt, Type.kt, Theme.kt
│       └── viewmodel/      # MainViewModel, AdminViewModel
├── firestore.rules         # Zero-trust Firestore security rules
├── firestore.test.js       # Local Firestore emulator security rule tests
└── firebase-blueprint.json # Firestore schema blueprint
```

---

## Build & Test Commands

```bash
# Compile & assemble debug APK
gradle :app:assembleDebug

# Run JVM unit & Robolectric tests
gradle :app:testDebugUnitTest

# Run Firestore security rules tests against local emulator
FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
```

---

## How to Push This Project to GitHub from Google AI Studio

1. Open the **Settings** menu in Google AI Studio.
2. Select **Push to GitHub** and authorize your GitHub repository.
3. Once pushed, add your recorded demo video (`docs/media/beity_demo.mp4`) or emulator screenshots (`docs/media/*.png`) directly in GitHub or locally via `git push`.
