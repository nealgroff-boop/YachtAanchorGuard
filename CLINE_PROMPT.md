# Yacht Anchor Guard - Complete Cline Generation Prompt

## Project Specification

You are an expert Android developer. Generate a complete, production-ready Android application called **Yacht Anchor Guard** using Kotlin and Jetpack Compose. This is a GPS-based automatic anchor dragging alarm app for yachts and boats.

---

## Core Requirements

### 1. Project Setup
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Minimum SDK**: 26
- **Target SDK**: 34
- **Build System**: Gradle with Kotlin DSL (.kts files)
- **Package Name**: `com.nautical.yachtanchorguard`
- **App Name**: Yacht Anchor Guard
- **Icon/Branding**: Nautical theme with anchor imagery

### 2. Core Functionality

#### GPS Tracking & Monitoring
- **Real-time GPS Tracking**: Implement foreground service (`GpsTrackingService`) that continuously monitors yacht position in background
- **GPS Update Interval**: 5-second updates when tracking is active
- **Accuracy Filtering**: 
  - Only accept GPS data meeting user-defined accuracy threshold
  - Default: 15 meters
  - Configurable range: 5-100 meters
  - GPS Status indicator: "Good" (within threshold), "Poor" (exceeds threshold), "Lost" (no signal)
- **GPS Data Loss Detection**:
  - Trigger alarm if acceptable GPS data not received within configurable timeout
  - Default timeout: 60 seconds
  - Configurable range: 10-300 seconds

#### Drift Detection & Alarms
- **Drift Detection**: Alert when yacht drifts more than configured radius from anchor point
  - Default radius: 50 meters
  - Configurable range: 10-500 meters
- **Multiple Alert Types**:
  - Audio alarm (system sounds or ringtones)
  - Visual notification (full-screen alert with large text)
  - Foreground notification
  - SMS alert (to user-configured phone number)

#### Alert System
- **Audio Alarms**:
  - User selects from system sounds or ringtones via `RingtoneManager`
  - Volume control
  - Audio Test Button: Preview selected alarm sound before sailing
- **Visual Notifications**:
  - Full-screen alert dialogs with large text and acknowledge button
  - Shows position and drift distance
- **SMS Alerts**:
  - Uses native Android `SmsManager` (cellular network only, no internet required)
  - Two message formats:
    - **Drift Alert**: `YACHT ALARM: Anchor dragging detected! Position: [LAT],[LONG]. Distance from anchor: [DISTANCE]m`
    - **GPS Loss Alert**: `YACHT ALARM: GPS signal lost! Last known position: [LAT],[LONG]. Time: [TIMESTAMP]`
- **Acknowledge Button**: Quickly dismiss alerts while assessing situation

---

## User Interface

### Home Screen
- **Set Anchor Point**: 
  - Manual input (latitude/longitude)
  - Auto-detect current position button
  - Visual confirmation of anchor set
- **GPS Status Display**:
  - Real-time status: Good/Poor/Lost
  - Current position (latitude/longitude)
  - Distance from anchor
  - Signal accuracy (meters)
- **Tracking Controls**:
  - Start/Stop tracking button
  - Tracking status indicator
- **Quick Actions**:
  - Test Alarm Sound button
  - Test SMS button (sends test message)
- **Visual Alerts**:
  - Full-screen alarm notification if drift detected
  - Acknowledge button to dismiss
  - Shows current position and distance from anchor

### Map Screen
- **Multiple Background Options**:
  - **Blank** (default): Lightweight, no tiles, shows markers and radius overlay
  - **OpenStreetMap**: Free standard map tiles (via osmdroid)
  - **Bing Maps Satellite**: Free high-quality satellite/aerial imagery (requires user-provided API key)
  - **Navionics**: Optional (if available)
- **Map Overlays**:
  - Anchor location marker (fixed red/orange marker)
  - Current position marker (only when GPS accurate, blue marker)
  - Drift radius circle (visual representation of alert radius)
  - Compass/bearing indicator
  - Scale/grid lines
- **Map Features**:
  - Zoom and pan controls
  - Offline caching of downloaded tiles
  - GPS Status indicator on map
  - Responsive to GPS updates

### Settings Screen
- **Anchor Settings**:
  - Set Anchor Point: Manual input or current position
  - Save Anchor on Exit: Toggle to persist anchor position after app closes
  - View/Clear saved anchor
- **Alarm Settings**:
  - Alarm Radius: Slider (10-500m, default 50m)
  - Alarm Sound Selection: Dropdown showing system sounds and ringtones
  - Test Alarm Sound: Button to preview selected sound
- **GPS Settings**:
  - Required Accuracy Level: Slider (5-100m, default 15m)
  - GPS Data Loss Timeout: Slider (10-300 sec, default 60 sec)
- **SMS Settings**:
  - SMS Phone Number: Input field with validation
  - SMS Toggle: Enable/disable SMS alerts
  - Test SMS Button: Verify cellular connection before sailing
  - Status display: Last SMS sent, success/failure
- **Map Settings**:
  - Map Background Selection: Radio buttons (Blank, OpenStreetMap, Bing, Navionics)
  - Bing API Key Input: Optional field (only shown when Bing selected)
  - Map Tile Cache Size: Display/clear cache
- **Persistence**: All settings saved automatically via DataStore

---

## Technical Architecture

### Core Dependencies
Include in `build.gradle.kts`:
- **Kotlin**: Latest stable
- **Jetpack Compose**: Latest stable (UI framework)
- **Jetpack Lifecycle**: For lifecycle management
- **Room Database**: For local data persistence
- **DataStore**: For user preferences/settings
- **osmdroid**: Map library with tile support
- **Coil**: Image loading library
- **Google Location Services**: For GPS
- **AndroidX Core**: Utility functions

### Database Schema (Room)
Create the following tables:

**AnchorLocation Table**:
```
- id (PRIMARY KEY, Int)
- latitude (Double)
- longitude (Double)
- timestamp (Long)
- name (String, optional)
```

**AlarmEvent Table**:
```
- id (PRIMARY KEY, Int)
- eventType (String: "DRIFT" or "GPS_LOSS")
- latitude (Double)
- longitude (Double)
- distance (Float, optional)
- timestamp (Long)
- acknowledged (Boolean)
```

**GpsRecord Table** (optional, for debugging):
```
- id (PRIMARY KEY, Int)
- latitude (Double)
- longitude (Double)
- accuracy (Float)
- timestamp (Long)
```

### User Preferences (DataStore)
Store the following preferences:
```
- alarmRadius: Int (default 50, range 10-500)
- smsPhoneNumber: String (empty by default)
- selectedAlarmSoundUri: String (system default ringtone)
- selectedMapBackground: String ("blank", "osm", "bing", "navionics", default "blank")
- saveAnchorOnExit: Boolean (default false)
- requiredGpsAccuracy: Int (default 15, range 5-100)
- gpsDataLossTimeout: Int (default 60, range 10-300)
- bingMapsApiKey: String (empty by default)
- smsAlertsEnabled: Boolean (default true)
- lastSavedAnchorLat: Double (optional)
- lastSavedAnchorLon: Double (optional)
```

### Services

#### GpsTrackingService (Foreground Service)
- Runs continuously when tracking is active, even with screen off
- Uses WakeLock to prevent CPU sleep
- Requests GPS updates every 5 seconds
- Filters GPS data by accuracy threshold (user-configured)
- Detects GPS data loss timeout
- Triggers alarms for drift detection and GPS loss
- Sends SMS alerts when conditions met
- Posts foreground notification with current status
- Receives commands via Intent (START, STOP)

#### Broadcast Receivers
- **AlarmReceiver**: Handle alarm events and trigger notifications/SMS
- **SmsReceiver** (optional): Listen for incoming SMS (future feature)

### Permissions (AndroidManifest.xml)
```xml
<!-- Location Permissions -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- SMS Permissions -->
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />

<!-- Notification Permission -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Other Permissions -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

---

## Project Structure

Generate the following directory structure:

```
YachtAnchorGuard/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/nautical/yachtanchorguard/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   ├── MapScreen.kt
│   │   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   │   └── AlarmScreen.kt
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── GpsStatusCard.kt
│   │   │   │   │   │   ├── AlarmRadiusSlider.kt
│   │   │   │   │   │   ├── AnchorMarker.kt
│   │   │   │   │   │   └── FullScreenAlertDialog.kt
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       ├── Type.kt
│   │   │   │   │       └── Theme.kt
│   │   │   │   ├── service/
│   │   │   │   │   └── GpsTrackingService.kt
│   │   │   │   ├── receiver/
│   │   │   │   │   ├── AlarmReceiver.kt
│   │   │   │   │   └── SmsReceiver.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── AnchorDatabase.kt
│   │   │   │   │   │   ├── AnchorLocation.kt
│   │   │   │   │   │   ├── AlarmEvent.kt
│   │   │   │   │   │   └── GpsRecord.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── AnchorRepository.kt
│   │   │   │   │   ├── datastore/
│   │   │   │   │   │   └── UserPreferences.kt
│   │   │   │   ├── util/
│   │   │   │   │   ├── GpsCalculations.kt
│   │   │   │   │   ├── Constants.kt
│   │   │   │   │   ├── PermissionHelper.kt
│   │   │   │   │   └── SmsManager.kt
│   │   │   │   └── viewmodel/
│   │   │   │       ├── AnchorViewModel.kt
│   │   │   │       ├── MapViewModel.kt
│   │   │   │       └── SettingsViewModel.kt
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── dimens.xml
│   │   │   │   └── mipmap/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── DEVELOPMENT_NOTES.md
```

---

## Implementation Details

### GPS Calculations
Create `GpsCalculations.kt` with utility functions:
- `calculateDistance(lat1, lon1, lat2, lon2): Float` - Haversine formula for distance between two coordinates
- `calculateBearing(lat1, lon1, lat2, lon2): Float` - Bearing/heading calculation
- `validateGpsAccuracy(accuracy, threshold): Boolean` - Check if GPS accuracy meets threshold

### Permission Handling
Create `PermissionHelper.kt`:
- Request fine location permission at runtime
- Request background location permission (for API 30+)
- Request SMS permission
- Request notification permission
- Handle permission denial gracefully

### SMS Sending
Create `SmsManager.kt`:
- Send SMS via native `android.telephony.SmsManager`
- Validate phone number format
- Handle SMS send failures with user feedback
- No internet dependency (cellular only)

### Map Implementation
- **Blank Map**: Simple overlay renderer, no tile downloads
- **OpenStreetMap**: Integrate osmdroid with standard tile source
- **Bing Maps Satellite**: Support user-provided API key, tile source configuration
- **Navionics**: Placeholder for future implementation

### ViewModels
Create ViewModels using Jetpack Architecture:
- **AnchorViewModel**: Manages anchor location, GPS state, alarm logic
- **MapViewModel**: Manages map rendering, overlays, camera state
- **SettingsViewModel**: Manages user preferences, persistence

### Foreground Service Setup
- Service runs with foreground notification
- Notification shows current GPS status and distance from anchor
- Service continues after app closes (if tracking enabled)
- Use WakeLock to prevent CPU sleep during GPS tracking

---

## UI/UX Details

### Color Scheme (Nautical Theme)
- Primary: Deep Ocean Blue (#1B5E7F)
- Secondary: Bright Aqua (#00A3E0)
- Accent: Nautical Gold (#D4A64E)
- Success: Sea Green (#2ECC71)
- Alert: Warning Red (#E74C3C)
- Neutral: Light Gray (#F5F5F5), Dark Gray (#333333)

### Typography
- Headlines: Bold, large size
- Body: Regular, readable size
- Code: Monospace for technical info

### Navigation
- Bottom navigation with 3 tabs: Home, Map, Settings
- Smooth transitions between screens
- Back navigation support

### Responsiveness
- Support portrait and landscape orientations
- Responsive layouts for different screen sizes
- Touch-friendly button sizes (48dp minimum)

---

## Data Persistence

### Room Database
- Create entities and DAOs for AnchorLocation, AlarmEvent, GpsRecord
- Implement repository pattern for database access
- Use Flow for reactive data updates

### DataStore
- Create PreferencesDataStore for user settings
- Persist all user configurations
- Handle defaults gracefully
- Support encrypted storage for sensitive data (phone number)

---

## Error Handling & Validation

### GPS Validation
- Validate GPS coordinates are within valid ranges
- Check accuracy against threshold
- Detect and handle GPS signal loss
- Log GPS errors for debugging

### SMS Validation
- Validate phone number format (10-15 digits)
- Check for null/empty phone number before sending
- Handle SMS send failures with user-friendly messages
- Display SMS sending status to user

### Permission Validation
- Check all required permissions before using features
- Request permissions at appropriate times
- Handle permission denials gracefully
- Show user-friendly prompts for permission requests

### Input Validation
- Validate latitude/longitude inputs (range -90 to 90, -180 to 180)
- Validate numeric inputs for sliders
- Trim and sanitize string inputs

---

## Testing & Debugging

### Debug Features
- GPS mock location testing
- Manual alarm trigger button
- GPS history logging table
- Test SMS sending with test number
- Test notification display

### Logging
- Log GPS updates (non-verbose in release)
- Log alarm triggers with full context
- Log SMS send attempts and results
- Log app lifecycle events

---

## Performance Considerations

### Battery Optimization
- Use efficient GPS polling (5-second intervals, not continuous)
- Implement WakeLock wisely (only when necessary)
- Optimize map tile rendering
- Use lightweight Blank map option by default
- Batch database writes

### Memory Management
- Limit GPS history table size (keep last 1000 records)
- Cache map tiles locally (but limit cache size)
- Use efficient data structures
- Release resources in lifecycle methods

### Network (when using Bing/OSM tiles)
- Download tiles on demand
- Cache tiles locally for offline use
- Handle network failures gracefully
- Use appropriate compression

---

## Safety & Compliance

### Maritime Safety Notice
- This app is a secondary safety aid only
- Always maintain proper anchor watch
- Use primary navigation equipment
- SMS alerts are fast but not guaranteed
- GPS accuracy varies; errors of 15m+ are possible
- Cellular coverage may be limited offshore

### Legal Compliance
- Comply with SMS regulations in user's country
- Respect user's location privacy
- Do not share location data externally
- Follow Android best practices for permissions
- Use Bing Maps API according to Microsoft Terms of Use

---

## Build & Deployment

### Gradle Configuration
- Use Kotlin DSL (.kts files)
- Configure build types (debug, release)
- Set up ProGuard/R8 for release builds
- Configure signing for APK

### APK Building
- Ensure GitHub Actions workflow is configured (`.github/workflows/build-apk.yml`)
- Build debug APK for testing
- Support sideloading via `adb install`

---

## Code Quality Standards

### Kotlin Style
- Follow Google's Kotlin style guide
- Use meaningful variable names
- Add kdoc comments for public functions
- Avoid deeply nested code
- Use coroutines for async operations

### Compose Best Practices
- Break UI into small, reusable composables
- Use state management patterns (ViewModel, DataStore)
- Avoid recomposition inefficiencies
- Use lazy layouts for long lists

### Error Handling
- Use try-catch for exceptions
- Provide meaningful error messages to users
- Log exceptions for debugging
- Never crash silently

---

## Summary

Generate a complete, production-ready Android app that:
1. ✅ Continuously monitors GPS in background
2. ✅ Triggers audible/visual/SMS alarms on drift or GPS loss
3. ✅ Displays anchor point and current position on interactive map
4. ✅ Allows users to configure all settings (radius, accuracy, timeout, SMS number, sounds, maps)
5. ✅ Persists all data between app sessions
6. ✅ Runs as a foreground service for reliability
7. ✅ Works with or without internet (SMS via cellular only)
8. ✅ Includes comprehensive testing and validation
9. ✅ Follows Android best practices and Jetpack guidelines
10. ✅ Includes detailed error handling and user feedback

All code should be well-organized, documented, and ready for production deployment to real yachts and boats.

---

## Generation Instructions for Cline

1. Start by creating the complete file structure
2. Generate database entities and DAOs first
3. Generate repository and ViewModel classes
4. Generate all UI screens and composables
5. Generate services and receivers
6. Generate utility classes and helpers
7. Generate AndroidManifest.xml with all permissions and components
8. Generate build.gradle.kts files
9. Generate resource files (strings, colors, dimens)
10. Generate GitHub Actions workflow if not present
11. Add comprehensive comments and documentation
12. Ensure all imports are correct and no unresolved references
13. Verify the app structure matches the specification exactly

**When complete**, the user can immediately:
1. Commit and push to GitHub
2. Build via GitHub Actions (APK automatically generated)
3. Download and sideload APK to phone
4. Test on water with real anchor points
