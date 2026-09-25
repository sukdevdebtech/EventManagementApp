# Event Manager (Kotlin + Firebase)

An Android app where users sign in, create events, manage them, and see live statistics - all synced with Firebase.
this assignment using **Kotlin**, **XML layouts**, **MVVM + Repository pattern** and **Firebase** (Authentication, Firestore, Cloud Messaging).

## Features

| Area | What is implemented |
|------|--------------------|
| **Authentication** | Email/password sign-up & login, password reset email, persistent session, friendly error messages for invalid credentials |
| **Events** | Add / edit / delete events (title, description, date & time, location), list in reverse chronological order, stored per user in Firestore, real-time updates |
| **Dashboard** | Total / upcoming / past counts + events-per-month bar chart (MPAndroidChart) |
| **Technical** | MVVM with `ViewModel` + `StateFlow`, Repository pattern, Firestore offline persistence, input validation (title required, date not in the past), Firebase exceptions mapped to readable messages |
| **Bonus** | FCM push reminders, search + Upcoming/Past filter, dark mode (follows system + manual toggle), unit tests |

## Tech stack

- Kotlin, Coroutines, Flow / StateFlow
- XML + ViewBinding, Material 3, Navigation Component (bottom navigation)
- Firebase Auth, Cloud Firestore, Cloud Messaging
- MPAndroidChart
- JUnit4 + kotlinx-coroutines-test

## Architecture

```
UI (Activity / Fragment, XML)
        |  observes StateFlow<UiState>, calls ViewModel functions
        v
ViewModel  (AuthViewModel, EventListViewModel, AddEditEventViewModel, DashboardViewModel)
        |  uses interfaces only
        v
Repository (AuthRepository, EventRepository)  -> returns Resource<T> (Success / Error)
        |
        v
Firebase (Auth, Firestore users/{uid}/events/{eventId}, FCM)
```

```
app/src/main/java/com/example/eventmanager/
|-- EventApp.kt                 Application: Firestore offline cache, theme, notification channel
|-- data/
|   |-- model/Event.kt          Firestore document model
|   `-- repository/             AuthRepository, EventRepository (+ Firebase implementations)
|-- di/ServiceLocator.kt        Manual DI + ViewModelFactory
|-- notification/               FCM service, token saver, notification helper
|-- ui/
|   |-- auth/                   Login, Register, ForgotPassword + AuthViewModel
|   |-- events/                 Event list, add/edit screen, adapter + ViewModels
|   |-- dashboard/              Statistics + chart
|   `-- MainActivity.kt         Hosts bottom navigation
`-- util/                       Resource, Validators, FirebaseErrorMapper, DateUtils, ThemeHelper
```

### Firestore data model

```
users/{uid}                     { fcmToken }
users/{uid}/events/{eventId}    { title, description, dateTime (Timestamp), location, createdAt, reminderSent }
```

## Setup

### 1. Prerequisites
- Android Studio (Koala or newer), JDK 17
- A Google account for Firebase

### 2. Firebase configuration
1. Go to the [Firebase Console](https://console.firebase.google.com) and **create a project**.
2. **Add an Android app** with package name `com.example.eventmanager`
   (if you change `applicationId` in `app/build.gradle.kts`, use the same value here).
3. Download **`google-services.json`** and place it in the **`app/`** folder
   (it is git-ignored; see `app/google-services.json.example`).
4. **Authentication -> Sign-in method** -> enable **Email/Password**.
5. **Firestore Database** -> *Create database* (production or test mode).
6. Publish the security rules from `firestore.rules`
   (Firestore -> Rules tab, or `firebase deploy --only firestore:rules`).
7. Cloud Messaging is enabled by default for the project.

### 3. Run
1. Open the project folder in Android Studio and let Gradle sync
   (Android Studio downloads the Gradle wrapper distribution automatically).
2. Select an emulator / device (API 24+) and press **Run**.

### 4. Run unit tests
```
./gradlew testDebugUnitTest
```
Tests cover `EventListViewModel` (ordering, filter, search, delete), `DashboardViewModel` (statistics) and `Validators`.

## Push notification reminders (bonus)

The app saves each device's FCM token to `users/{uid}.fcmToken` and shows incoming messages using the `event_reminders` channel.

- **Quick test:** Firebase Console -> Messaging -> *New campaign* -> *Firebase Notification messages* -> *Send test message* using the device token stored in Firestore at `users/{uid}.fcmToken`.
- **Automatic reminders:** the optional Cloud Function in `functions/` runs every 5 minutes and notifies users 30 minutes before an event starts.
  ```
  cd functions && npm install
  firebase deploy --only functions,firestore:indexes
  ```
  Scheduled functions require the Firebase **Blaze** plan.

## Notes / design decisions

- **Offline support:** Firestore persistent cache is enabled in `EventApp`. Firestore write `Task`s only complete after server acknowledgement, so `EventRepository` waits briefly and then treats the write as *queued locally* - the UI stays responsive offline and Firestore syncs later.
- **Real-time updates:** `observeEvents()` wraps a Firestore snapshot listener in a cold `Flow`; the list and dashboard both react to it.
- **Reverse chronological order:** `orderBy("dateTime", DESCENDING)` in the query.
- **Date validation:** a new event cannot be in the past. When editing an existing past event you may keep its original date.
- **Errors:** all repository calls return `Resource.Success/Error`; `FirebaseErrorMapper` converts exceptions to readable text.

## Possible improvements
Hilt for DI, Room-backed cache, swipe-to-delete with undo, event categories, calendar view, Compose UI.
