# Yacht Anchor Guard - Development Notes

## Project Overview

**Yacht Anchor Guard** is a professional GPS-based automatic anchor dragging alarm app for yachts and boats. Built with Kotlin and Jetpack Compose for Android (minimum SDK 26, target SDK 34).

The app continuously monitors yacht position in the background and triggers audible, visual, and SMS alerts if the yacht drifts outside a user-set radius from the anchor point.

---

## Core Features

### GPS Tracking & Accuracy
- Real-time GPS tracking in background using Foreground Service
- **GPS Accuracy Filtering**: Only accept GPS data meeting user-defined accuracy threshold (default 15m, range 5-100m)
- GPS Status indicator: "Good" (within threshold), "Poor" (exceeds threshold), "Lost" (no signal)
- GPS updates every 5 seconds when tracking
- **GPS Data Loss Detection**: Trigger alarm if acceptable GPS data not received within configurable timeout (default 60 seconds, range 10-300 seconds)

### Alarm System
- **Drift Detection**: Alert when yacht drifts >50m from anchor point (configurable 10-500m radius)
- **GPS Signal Loss**: Alert if GPS signal lost for configured duration
- **Multiple Alert Types**: Audio + visual notification + foreground notification + SMS

### Alerts & Notifications
- **Audio Alarms**: User selects from system sounds or ringtones, with volume control
- **Audio Test Button**: Preview selected alarm sound before sailing
- **Visual Notifications**: Full-screen alerts with large text and acknowledge button
- **SMS Alerts**: Automatic SMS to configured phone number via cellular network (no internet required)
- **Acknowledge Button**: Quickly dismiss alerts while assessing situation

### Map Features
- **Multiple Background Options**:
  - **Blank** (default): Lightweight, no tiles, shows markers and radius overlay
  - **OpenStreetMap**: Free standard map tiles
  - **Bing Maps Satellite**: Free high-quality satellite/aerial imagery (requires user-provided API key)
  - **Navionics**: Optional (if available)
- **Map Overlays**:
  - Anchor location marker
  - Current position marker (only when GPS accurate)
  - Drift radius circle (visual representation)
  - Compass/bearing indicator
  - Scale/grid lines
- **Offline Caching**: Downloaded map tiles cached locally for offline use
- **GPS Status on Map**: Visual indicator showing Good/Poor/Lost signal

### Settings & Persistence
- **Set Anchor Point**: Manual input or auto-detect current position
- **Save Anchor on Exit**: Toggle to persist anchor position after app closes
- **Alarm Radius**: Configurable 10-500m (default 50m)
- **Required Accuracy Level**: Configurable 5-100m (default 15m)
- **GPS Data Loss Timeout**: Configurable 10-300 seconds (default 60 sec)
- **SMS Phone Number**: Input field with validation
- **SMS Toggle**: Enable/disable SMS alerts
- **Test SMS Button**: Verify cellular connection before sailing
- **Alarm Sound Selection**: Choose from system sounds, ringtones, or custom
- **Map Background Selection**: Choose preferred map layer
- **Bing API Key Input**: Optional field to enable Bing Satellite imagery
- **All settings persist** between app sessions via DataStore

---

## Technical Architecture

### Core Dependencies
- **Kotlin**: Language
- **Jetpack Compose**: UI framework
- **Room Database**: Local data persistence
- **DataStore**: User preferences/settings
- **osmdroid**: Map library with tile support
- **Android Foreground Service**: Background GPS tracking
- **RingtoneManager**: System sounds and ringtones
- **SmsManager**: SMS sending via cellular network

### Database Schema
- **Anchor Locations Table**: Store anchor points with timestamp
- **Alarm Events Table**: Log alarm events (timestamp, type, position)
- **GPS History Table** (optional): For debugging GPS data

### User Preferences (DataStore)
```
- alarmRadius (meters)
- smsPhoneNumber (string)
- selectedAlarmSoundUri (string)
- selectedMapBackground (string: "blank", "osm", "bing", "navionics")
- saveAnchorOnExit (boolean)
- requiredGpsAccuracy (meters)
- gpsDataLossTimeout (seconds)
- bingMapsApiKey (string, optional)
```

### Services
- **GpsTrackingService**: Foreground service for continuous GPS monitoring
  - Runs when screen off
  - Uses WakeLock to prevent CPU sleep
  - Filters GPS by accuracy threshold
  - Detects data loss timeout
  - Triggers alarms

### Receivers
- **SmsReceiver**: Listen for incoming SMS (future feature: remote queries)
- **AlarmReceiver**: Handle alarm events and notifications

---

## SMS Alert System

### How It Works
- **Cellular Network Only**: Uses native Android SmsManager for direct cellular delivery
- **No Internet Required**: Works offline with cellular signal alone
- **Each User's Key**: Users provide their own phone number in settings
- **Error Handling**: Toast/dialog if SMS fails (e.g., no cellular signal)

### SMS Message Formats

**Drift Detection Alert:**
```
YACHT ALARM: Anchor dragging detected! Position: [LAT],[LONG]. Distance from anchor: [DISTANCE]m
```

**GPS Signal Loss Alert:**
```
YACHT ALARM: GPS signal lost! Last known position: [LAT],[LONG]. Time: [TIMESTAMP]
```

---

## Map Implementation

### Blank Map (Default)
- Lightweight, minimal memory/battery usage
- Shows only overlays: markers, radius circle, compass
- No tile downloads
- Fastest rendering
- Ideal for extended maritime operations

### OpenStreetMap
- Free standard map tiles via osmdroid
- Good for general navigation
- Tiles cached locally
- Works offline once cached

### Bing Maps Satellite
- Free high-quality satellite/aerial imagery
- **User-Provided API Key Required**
- Ideal for maritime use: clear water/coastline visibility
- Tiles cached locally for offline use
- No billing required (free Bing Maps tier)

#### Getting Bing Maps API Key (User Instructions)
1. Visit [Bing Maps Portal](https://www.bingmapsportal.com)
2. Sign in or create free Microsoft account
3. Go to "My account" > "My keys"
4. Create new key:
   - Application name: "Yacht Anchor Guard"
   - Application type: "Public website"
   - URL: Leave blank or enter your GitHub repo
5. Copy API key and paste into app Settings > Map Background > Bing API Key
6. Bing Satellite now available in map selector

**Key Details:**
- Free tier provides generous quota for yacht app usage
- No billing required
- Each user has their own quota
- Complies with Microsoft Terms of Use

---

## Development Setup on Chromebook

### Prerequisites
- Chromebook with Linux support (optional, but recommended)
- VS Code (web or app from Play Store)
- Git (for cloning repo)
- GitHub account
- Claude API key (for Cline)

### Step-by-Step Setup

#### 1. Install VS Code
- **Option A (Web)**: Visit [code.visualstudio.com](https://code.visualstudio.com)
- **Option B (App)**: Search "Visual Studio Code" in Google Play Store

#### 2. Install Cline Extension
1. Open VS Code
2. Click Extensions icon (sidebar)
3. Search "Cline"
4. Install official Cline extension by Anthropic
5. Click Cline icon in sidebar

#### 3. Set Up Claude API Key
1. Sign up at [api.anthropic.com](https://api.anthropic.com)
2. Get your API key
3. In Cline: Click settings ⚙️ > paste API key

#### 4. Clone Repository
```bash
git clone https://github.com/nealgroff-boop/YachtAanchorGuard.git
cd YachtAanchorGuard
code .
```

#### 5. Generate Code with Cline
1. Open Cline in VS Code
2. Paste the Cline Prompt (see "CLINE_PROMPT.md" or below)
3. Let Cline generate all files
4. Review generated code

#### 6. Commit & Push
```bash
git add .
git commit -m "Generate yacht anchor alarm app with GPS tracking, SMS alerts, and maps"
git push origin main
```

---

## GitHub Actions Build Pipeline

### Automated APK Building
GitHub Actions automatically builds your APK every time you push code.

#### Workflow File
**Location**: `.github/workflows/build-apk.yml`

The workflow:
1. Checks out code
2. Sets up JDK 17
3. Runs Gradle build
4. Creates debug APK
5. Uploads APK as artifact

#### Download APK
1. Go to your repo: `https://github.com/nealgroff-boop/YachtAanchorGuard`
2. Click **Actions** tab
3. Click latest workflow run
4. Scroll to "Artifacts"
5. Download `app-debug.apk`

#### Sideload to Phone
```bash
# Connect phone via USB
adb devices

# Install APK
adb install app-debug.apk
```

---

## Cline Prompt Summary

The Cline prompt defines the complete specification for code generation:

### Key Specifications
- **Language**: Kotlin with Jetpack Compose
- **Min SDK**: 26, Target SDK: 34
- **GPS Updates**: Every 5 seconds when tracking
- **Accuracy Filtering**: Configurable threshold (default 15m)
- **Data Loss Timeout**: Configurable duration (default 60 sec)
- **Maps**: Blank (default), OpenStreetMap, Bing Satellite, Navionics
- **Alarm Sounds**: System sounds + ringtones
- **SMS**: Via native Android SmsManager (cellular only)
- **Persistence**: Room database + DataStore
- **Services**: Foreground GPS tracking service
- **Permissions**: Location, SMS, notifications, wake lock, battery optimization

### Full Cline Prompt
[See `CLINE_PROMPT.md` for complete detailed prompt]

---

## Permissions Required

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

```
YachtAnchorGuard/
├── app/
│   ├── src/main/
│   │   ├── java/com/nautical/yachtanchorguard/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   ├── MapScreen.kt
│   │   │   │   │   └── SettingsScreen.kt
│   │   │   ├── service/
│   │   │   │   └── GpsTrackingService.kt
│   │   │   ├── receiver/
│   │   │   │   ├── SmsReceiver.kt
│   │   │   │   └── AlarmReceiver.kt
│   │   │   ├── data/
│   │   │   │   ├── database/
│   │   │   │   │   ├── AnchorDatabase.kt
│   │   │   │   │   ├── AlarmEvent.kt
│   │   │   │   │   └── GpsRecord.kt
│   │   │   │   └── repository/
│   │   │   │       └── AnchorRepository.kt
│   │   │   ├── util/
│   │   │   │   ├── GpsCalculations.kt
│   │   │   │   ├── Constants.kt
│   │   │   │   └── PermissionHelper.kt
│   │   │   └── viewmodel/
│   │   │       └── AnchorViewModel.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── DEVELOPMENT_NOTES.md (this file)
```

---

## Next Steps (Tomorrow)

1. ✅ **Install VS Code** on Chromebook
2. ✅ **Install Cline extension**
3. ✅ **Add Claude API key**
4. ✅ **Clone repository**: `git clone https://github.com/nealgroff-boop/YachtAanchorGuard.git`
5. ✅ **Open in VS Code**: `code .`
6. ✅ **Paste Cline prompt** (from `CLINE_PROMPT.md`)
7. ✅ **Generate code** (let Cline run to completion)
8. ✅ **Review generated files** in VS Code
9. ✅ **Commit changes**: `git add . && git commit -m "Generate complete app"`
10. ✅ **Push to GitHub**: `git push origin main`
11. ✅ **Wait for GitHub Actions** to build APK (~5-10 minutes)
12. ✅ **Download APK** from Actions artifacts
13. ✅ **Sideload to phone**: `adb install app-debug.apk`
14. ✅ **Test on water!** 🛥️⚓

---

## Important Notes

### Before Sailing
- ✅ Test GPS tracking on ground
- ✅ Test SMS with test button
- ✅ Set anchor point correctly
- ✅ Verify alarm sounds audible
- ✅ Check accuracy threshold (15m recommended)
- ✅ Set GPS data loss timeout (60 sec recommended)
- ✅ Verify SMS phone number correct
- ✅ Have cellular signal

### Maritime Safety
- **This app is a secondary safety aid only**
- **Always maintain proper anchor watch**
- **Use primary navigation equipment**
- **SMS alerts are fast but not guaranteed**
- **GPS accuracy varies; 15m+ errors possible**
- **Cellular coverage may be limited offshore**

### Troubleshooting
- **GPS not starting**: Check location permissions, battery optimization settings
- **SMS not sending**: Verify cellular signal, SMS enabled on phone, valid number
- **Maps not loading**: Check INTERNET permission, Bing API key correct
- **Alarms not working**: Check notification permissions, volume settings
- **App crashes**: Check GitHub Actions build logs, review generated code

---

## Resources

- **GitHub Repository**: [YachtAanchorGuard](https://github.com/nealgroff-boop/YachtAanchorGuard)
- **Android Developers**: [developer.android.com](https://developer.android.com)
- **osmdroid Documentation**: [osmdroid GitHub](https://github.com/osmdroid/osmdroid)
- **Jetpack Compose**: [developer.android.com/jetpack/compose](https://developer.android.com/jetpack/compose)
- **Bing Maps Portal**: [bingmapsportal.com](https://www.bingmapsportal.com)
- **Claude AI / Cline**: [anthropic.com](https://www.anthropic.com)

---

## Questions or Issues?

If you encounter issues:
1. Check this DEVELOPMENT_NOTES.md
2. Review generated code in VS Code
3. Check GitHub Actions logs for build errors
4. Create a GitHub Issue in your repo
5. Ask Copilot or Cline for help

---

**Happy coding and safe sailing!** ⛵🛥️⚓📡

*Last Updated: 2026-05-21*
*Project Status: Ready for Development*
