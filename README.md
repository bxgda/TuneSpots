# TuneSpots 🎵📍

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-%237F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.0-%234285F4?style=for-the-badge&logo=google)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-33.1.0-%23FFCA28?style=for-the-badge&logo=firebase)](https://firebase.google.com/)
[![Spotify API](https://img.shields.io/badge/Spotify-API-%231DB954?style=for-the-badge&logo=spotify)](https://developer.spotify.com/documentation/web-api/)
[![Cloudinary](https://img.shields.io/badge/Cloudinary-2.4.0-%233448C5?style=for-the-badge&logo=cloudinary)](https://cloudinary.com/)

**TuneSpots** is a Mobile Crowd-Sensing application that turns the world into a collaborative musical map. Discover and share "TuneSpots"—locations on the map where you can attach a song that defines the moment, the place, or the mood.

This project implements the concept of Mobile Crowd-Sensing and Collaboration with spatio-temporal objects. In the domain of this application, users pin songs from Spotify to specific geographic locations, creating a shared musical experience tied to real-world places.

The system is architected with a **mobile client** and a **server backend**. The mobile part is built with the latest Android technologies like Jetpack Compose for a modern, declarative UI, while the server part is powered by Firebase services, ensuring real-time data synchronization and a scalable backend.

<br>

## ✨ Core Features

-   **👤 Full User Authentication**: A complete registration and login system. Users can sign up with a username, password, name, phone number, and a profile picture taken directly from the camera or selected from the gallery.

-   **🗺️ Real-Time Location & Mapping**: The app continuously tracks the user's location using both GPS and network-based providers, displaying their current position on an interactive Google Map. This serves as the canvas for all musical discoveries.

-   **📡 Real-Time Sync & Notifications**: The application maintains a constant connection with the Firebase backend. It periodically sends the user's current location and receives real-time updates and notifications about nearby TuneSpots or other users, fostering a sense of shared, ambient experience.

-   **🎵 Creating & Discovering TuneSpots**: The core feature allows users to add a new "TuneSpot" at their current location by searching for and selecting a song from Spotify's vast library. Each spot becomes a permanent part of the shared world map.

-   **🔍 Advanced Filtering & Searching**:
    -   **Attribute Filtering**: Filter all visible TuneSpots by their creator, song, artist, or a specific date range.
    -   **Proximity Search**: Search for TuneSpots within a specified radius (e.g., 1km, 5km) of your current location.
    -   **Dual View**: Browse all registered spots visually on the map or in a detailed, sortable list.

-   **🏆 Gamification & Leaderboard**: A public leaderboard ranks all users based on points earned through community interaction. Points are awarded for activities like adding a new spot, rating an existing one, or leaving a comment, encouraging active participation.

<br>

## 🛠️ Tech Stack & Architecture

This project is built with a modern, scalable, and maintainable tech stack, embracing the latest standards in Android development and cloud services.

### Mobile (Client-Side)

-   **[Kotlin](https://kotlinlang.org/)**: The primary programming language, utilizing features like Coroutines & Flow for asynchronous programming and reactive data streams.
-   **[Jetpack Compose](https://developer.android.com/jetpack/compose)**: Android's modern, declarative UI toolkit used for building the entire user interface. This allows for a more efficient and less error-prone UI development process.
-   **Architecture**: The app follows the **MVVM (Model-View-ViewModel)** architecture pattern, ensuring a clean separation of concerns between the UI, business logic, and data layers.
-   **[Hilt](https://dagger.dev/hilt/)**: A dependency injection library for Android that simplifies dependency management and improves testability by providing dependencies throughout the application.
-   **[Google Maps SDK for Android](https://developers.google.com/maps/documentation/android-sdk/overview)**: Integrated via the `maps-compose` library to provide a seamless and interactive map experience within a declarative UI.
-   **[FusedLocationProviderClient](https://developers.google.com/location-context/fused-location-provider)**: Used for efficient and battery-optimized location tracking by intelligently combining GPS and network signals.
-   **[Retrofit](https://square.github.io/retrofit/)**: A type-safe HTTP client for Android and Java, used to communicate with the Spotify Web API.
-   **[Coil](https://coil-kt.github.io/coil/)**: A fast, lightweight, and modern image loading library for Android backed by Kotlin Coroutines.

### Backend & Cloud Services

-   **[Firebase](https://firebase.google.com/)**: Serves as the complete serverless backend for the application.
    -   **[Firebase Authentication](https://firebase.google.com/docs/auth)**: Manages all user registration and login processes securely.
    -   **[Cloud Firestore](https://firebase.google.com/docs/firestore)**: A flexible, scalable NoSQL cloud database used to store all user data, TuneSpots, comments, and ratings. Its real-time listeners power the live synchronization between clients.
    -   **[Firebase Cloud Storage](https://firebase.google.com/docs/storage)**: Used for storing user-uploaded media, such as profile pictures.
-   **[Cloudinary](https://cloudinary.com/)**: A cloud-based service for image and video management. It is used for hosting, transforming, and delivering user profile photos with optimizations.

### External Services

-   **[Spotify Web API](https://developer.spotify.com/documentation/web-api)**: The backbone of the musical experience. It is used to search for songs and retrieve rich metadata like track name, artist, album art, and preview URLs.
