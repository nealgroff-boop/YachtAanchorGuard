# Yacht Anchor Guard

A professional GPS-based automatic anchor dragging alarm for yachts and boats. Built with Kotlin and Jetpack Compose.

## Features
- **Real-time GPS Tracking**: Continuously monitors yacht position in the background.
- **Anchor Drag Alarm**: Triggers audible, visual, and SMS alerts if the yacht drifts outside a user-set radius.
- **Radar View**: Visual representation of the yacht's position relative to the anchor.
- **SMS Remote Query**: Send a keyword (e.g., "POSITION") to the boat's phone to receive current coordinates and distance.
- **Nautical UI**: Clean, high-contrast interface designed for maritime use.
- **Background Reliability**: Uses Foreground Services and Wake Locks to ensure the alarm stays active even when the screen is off.

## Project Structure
- `app/src/main/java/.../data`: Room database, DataStore preferences, and Repository.
- `app/src/main/java/.../service`: GpsTrackingService for background monitoring.
- `app/src/main/java/.../ui`: Jetpack Compose screens (Home, Map, Settings).
- `app/src/main/java/.../util`: GPS calculation utilities (Haversine formula, bearing, etc.).

## How to Build

### Prerequisites
- Android Studio (Hedgehog or newer recommended)
- Android SDK 34
- JDK 17

### Local Build Instructions
1. **Clone/Download** this project folder.
2. **Open Android Studio** and select "Open" -> Navigate to the `YachtAnchorGuard` folder.
3. **Wait for Gradle Sync** to complete.
4. **Connect an Android Device** (Physical device recommended for GPS testing).
5. **Click "Run"** (Green play button) to install on your device.

### Building the APK
1. In Android Studio, go to `Build` > `Build Bundle(s) / APK(s)` > `Build APK(s)`.
2. Once finished, a notification will appear with a link to the `app-debug.apk` file.

## Permissions Required
- `ACCESS_FINE_LOCATION` & `ACCESS_BACKGROUND_LOCATION`: For tracking position.
- `SEND_SMS` & `RECEIVE_SMS`: For automated alerts and remote queries.
- `POST_NOTIFICATIONS`: For the background service status.
- `WAKE_LOCK`: To prevent the CPU from sleeping during monitoring.

## Safety Disclaimer
This app is intended as a secondary safety aid. Always maintain a proper anchor watch and use primary navigation equipment.
