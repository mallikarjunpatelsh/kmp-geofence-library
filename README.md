# KMP Geofence Library

A Kotlin Multiplatform library for geofencing on Android and iOS with callback-based event handling.

## Features

- ✅ Cross-platform geofencing for Android and iOS
- ✅ Lambda-based callbacks for geofence events
- ✅ Permission status checking
- ✅ Add/remove geofences dynamically
- ✅ No organization-specific dependencies
- ✅ Easy to integrate and use

## Installation

### Step 1: Add dependency
In your `build.gradle.kts`:
```kotlin
commonMain.dependencies {
    implementation("io.github.mallikarjunpatelsh:geofence:1.0.2")
}
```

`mavenCentral()` is already included by default so no extra repository setup needed.

---

### Android Setup

#### AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<application
    android:name=".MyApplication">

    <receiver
        android:name="com.kmp.geofence.GeofenceBroadcastReceiver"
        android:enabled="true"
        android:exported="false" />

</application>
```

#### Application class

Initialize `GeofenceContext` and set the event listener in your Application class. Setting the listener here ensures callbacks work even in the background:
```kotlin
import android.app.Application
import com.kmp.geofence.GeofenceBroadcastReceiver
import com.kmp.geofence.GeofenceContext
import com.kmp.geofence.TransitionType

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize context
        GeofenceContext.init(this)

        // Set listener here — Application is always alive
        GeofenceBroadcastReceiver.setEventListener { event ->
            when (event.transitionType) {
                TransitionType.ENTER -> {
                    println("✅ ENTERED: ${event.geofenceId}")
                    // handle enter — call API, save to DB, send notification, etc.
                }
                TransitionType.EXIT -> {
                    println("✅ EXITED: ${event.geofenceId}")
                    // handle exit
                }
            }
        }
    }
}
```

---

### iOS Setup

No additional initialization needed.

#### Info.plist
```xml
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>Give the description</string>
<key>NSLocationWhenInUseUsageDescription</key>
<string>Give the description</string>
<key>UIBackgroundModes</key>
<array>
    <string>location</string>
</array>
```

---

## Usage

### Create GeofenceManager
```kotlin
val geofenceManager = createGeofenceManager()
```

### Set Event Listener (iOS only — Android uses Application class)
```kotlin
geofenceManager.setGeofenceEventListener(object : GeofenceEventListener {
    override fun onGeofenceEnter(event: GeofenceEvent) {
        println("✅ ENTERED: ${event.geofenceId}")
        println("Location: ${event.latitude}, ${event.longitude}")
    }
    override fun onGeofenceExit(event: GeofenceEvent) {
        println("✅ EXITED: ${event.geofenceId}")
        println("Location: ${event.latitude}, ${event.longitude}")
    }
})
```

### Check Permissions
```kotlin
when (val status = geofenceManager.checkLocationPermissions()) {
    is PermissionStatus.Granted -> {
        println("All permissions granted")
    }
    is PermissionStatus.Denied -> {
        println("Missing permissions: ${status.missingPermissions}")
        println("Message: ${status.message}")
        // Request permissions from user
    }
}
```

### Add Geofence
```kotlin
geofenceManager.addGeofence(
    id = "delivery_location_123",
    latitude = 37.7749,
    longitude = -122.4194,
    radius = 100f, // meters
    onSuccess = {
        println("Geofence added successfully")
    },
    onFailure = { error ->
        println("Failed to add geofence: $error")
    }
)
```

### Remove Geofences
```kotlin
geofenceManager.removeGeofences(
    ids = listOf("delivery_location_123", "delivery_location_456"),
    onSuccess = {
        println("Geofences removed successfully")
    },
    onFailure = { error ->
        println("Failed to remove geofences: $error")
    }
)
```

---

## Complete Implementation

### Android
```kotlin
// MyApplication.kt
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GeofenceContext.init(this)

        GeofenceBroadcastReceiver.setEventListener { event ->
            when (event.transitionType) {
                TransitionType.ENTER -> println("✅ ENTERED: ${event.geofenceId}")
                TransitionType.EXIT -> println("✅ EXITED: ${event.geofenceId}")
            }
        }
    }
}

// MyActivity.kt
class MyActivity : ComponentActivity() {
    private val geofenceManager = createGeofenceManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (val status = geofenceManager.checkLocationPermissions()) {
            is PermissionStatus.Granted -> addGeofences()
            is PermissionStatus.Denied -> requestPermissions(status.missingPermissions)
        }
    }

    private fun addGeofences() {
        geofenceManager.addGeofence(
            id = "delivery_1",
            latitude = 37.7749,
            longitude = -122.4194,
            radius = 100f,
            onSuccess = { println("Geofence added!") },
            onFailure = { error -> println("Failed: $error") }
        )
    }
}
```

### iOS
```kotlin
val geofenceManager = createGeofenceManager()

// Step 1: Set listener BEFORE addGeofence
geofenceManager.setGeofenceEventListener(object : GeofenceEventListener {
    override fun onGeofenceEnter(event: GeofenceEvent) {
        println("✅ ENTERED: ${event.geofenceId}")
    }
    override fun onGeofenceExit(event: GeofenceEvent) {
        println("✅ EXITED: ${event.geofenceId}")
    }
})

// Step 2: Check permissions
when (val status = geofenceManager.checkLocationPermissions()) {
    is PermissionStatus.Granted -> {
        // Step 3: Add geofence
        geofenceManager.addGeofence(
            id = "delivery_1",
            latitude = 37.7749,
            longitude = -122.4194,
            radius = 100f,
            onSuccess = { println("Geofence added!") },
            onFailure = { error -> println("Failed: $error") }
        )
    }
    is PermissionStatus.Denied -> {
        println("Missing: ${status.missingPermissions}")
        // request permissions
    }
}
```

---

## API Reference

### GeofenceManager

- `addGeofence(id: String, latitude: Double, longitude: Double, radius: Float, onSuccess: () -> Unit, onFailure: (String) -> Unit)`
  - Adds a circular geofence at the specified location

- `removeGeofences(ids: List<String>, onSuccess: () -> Unit, onFailure: (String) -> Unit)`
  - Removes geofences with the specified IDs

- `checkLocationPermissions(): PermissionStatus`
  - Checks if all required location permissions are granted

- `setGeofenceEventListener(listener: GeofenceEventListener?)`
  - Sets the listener for geofence enter/exit events (iOS only — Android uses Application class)

### GeofenceEvent
```kotlin
data class GeofenceEvent(
    val geofenceId: String,
    val latitude: Double,
    val longitude: Double,
    val transitionType: TransitionType
)
```

### PermissionStatus
```kotlin
sealed class PermissionStatus {
    object Granted : PermissionStatus()
    data class Denied(
        val missingPermissions: List<PermissionType>,
        val message: String
    ) : PermissionStatus()
}
```

### PermissionType
```kotlin
enum class PermissionType {
    FINE_LOCATION,
    BACKGROUND_LOCATION,
    PRECISE_LOCATION
}
```

---

## Platform-Specific Notes

### Android

- Requires Google Play Services Location API
- Minimum SDK: 24 (Android 7.0)
- Background location permission required for Android 10+
- Set listener in `Application` class for background support
- Geofences persist across device reboots

### iOS

- Uses CoreLocation framework
- Requires "Always" location permission for background monitoring
- Requires "Full Accuracy" for iOS 14+
- Listener must be set **before** calling `addGeofence`
- Maximum 20 geofences can be monitored simultaneously per app

---

## License

This library is provided as-is for use in your projects.

## Contributing

Feel free to submit issues and enhancement requests!
