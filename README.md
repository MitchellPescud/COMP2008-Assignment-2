# Pocket Library

An Android app for finding books, building a personal library and keeping track of favourites, even offline. Search the Open Library catalogue, save books to your device, add your own entries with photos, and sync your library and favourites to the cloud.

## Features
- **Search online** – general, title or author search against the Open Library API, with automatic retries on failed requests and cover art.
- **Local library** – save any result to an on-device library and browse or search it offline.
- **Favourites** – mark books as favourites and filter to them.
- **Add your own books** – enter details manually and attach a cover from the gallery or camera.
- **Book details** – description fetched on demand, with cover, author and first publish year.
- **Cloud sync** – library and favourites are backed up to Firestore through anonymous sign-in, and merged back into the local database without losing locally stored covers.

## Tech
- Kotlin and **Jetpack Compose**, Material 3 bottom navigation with Navigation Compose
- **MVVM** with a `ViewModel` and coroutines
- **Room** for the local database
- **Retrofit + Gson** for the Open Library API
- **Coil** for image loading
- **Firebase** Authentication (anonymous) and Cloud Firestore

## Architecture
| File | Role |
|---|---|
| `MainActivity` | navigation host, bottom bar, database and Firebase setup |
| `BookViewModel` | search, library/favourite state, description fetching, cloud sync |
| `OpenLibraryAPI` | Retrofit interface (`search.json` by query, title or author) |
| `BookEntity`, `BookDAO`, `AppDatabase` | Room persistence |
| `Firebase` | Auth and Firestore access |
| `BookSearchScreen`, `LocalLibraryScreen`, `FavouritesScreen`, `BookDetailScreen`, `ManualAddBookScreen` | Compose screens |

## Build and run
1. Open the project in Android Studio.
2. **Firebase setup:** create a Firebase project, enable Anonymous Authentication and Firestore, then download your own `google-services.json` into `app/`. It is deliberately not included in this repo.
3. Sync Gradle and run on an emulator or device.
