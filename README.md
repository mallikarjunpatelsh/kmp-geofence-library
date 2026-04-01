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

### Gradle

Add the library to your project:

```kotlin
// In your shared module's build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.kmp.geofence:geofence:1.0.0")
            }
        }
    }
}
```

### Android Manifest

Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<application>
    <!-- Register the broadcast receiver -->
    <receiver
        android:name="com.kmp.geofence.GeofenceBroadcastReceiver"
        android:enabled="true"
        android:exported="false" />
</application>
```

### iOS Info.plist

Add the following keys to your `Info.plist`:

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

## Usage

### 1. Initialize (Android Only)

For Android, you must initialize `InjectorCommon` in your Application class:

```kotlin
import android.app.Application
import com.kmp.geofence.InjectorCommon

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize InjectorCommon with application context
        InjectorCommon.mContextArgs = InjectorCommon.ContextArgs(this)
    }
}
```

Don't forget to register your Application class in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    ...>
</application>
```

### 2. Create GeofenceManager

**Android:**
```kotlin
val geofenceManager = createGeofenceManager()
```

**iOS:**
```kotlin
val geofenceManager = createGeofenceManager()
```

### 3. Set Event Listener

```kotlin
geofenceManager.setGeofenceEventListener(object : GeofenceEventListener {
    override fun onGeofenceEnter(event: GeofenceEvent) {
        println("Entered geofence: ${event.geofenceId}")
        println("Location: ${event.latitude}, ${event.longitude}")
        
        // Handle your business logic here
        // e.g., update order status, send notification, etc.
    }
    
    override fun onGeofenceExit(event: GeofenceEvent) {
        println("Exited geofence: ${event.geofenceId}")
        println("Location: ${event.latitude}, ${event.longitude}")
        
        // Handle your business logic here
    }
})
```

### 4. Check Permissions

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

### 5. Add Geofence

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

### 6. Remove Geofences

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

## API Reference

### GeofenceManager

#### Methods

- `addGeofence(id: String, latitude: Double, longitude: Double, radius: Float, onSuccess: () -> Unit, onFailure: (String) -> Unit)`
  - Adds a circular geofence at the specified location
  
- `removeGeofences(ids: List<String>, onSuccess: () -> Unit, onFailure: (String) -> Unit)`
  - Removes geofences with the specified IDs
  
- `checkLocationPermissions(): PermissionStatus`
  - Checks if all required location permissions are granted
  
- `setGeofenceEventListener(listener: GeofenceEventListener?)`
  - Sets the listener for geofence enter/exit events

### GeofenceEventListener

```kotlin
interface GeofenceEventListener {
    fun onGeofenceEnter(event: GeofenceEvent)
    fun onGeofenceExit(event: GeofenceEvent)
}
```

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

## Example: Complete Implementation

```kotlin
class MyGeofenceHandler(context: Context) {
    private val geofenceManager = GeofenceManager(context)
    
    init {
        setupGeofencing()
    }
    
    private fun setupGeofencing() {
        // Set up event listener
        geofenceManager.setGeofenceEventListener(object : GeofenceEventListener {
            override fun onGeofenceEnter(event: GeofenceEvent) {
                handleGeofenceEnter(event)
            }
            
            override fun onGeofenceExit(event: GeofenceEvent) {
                handleGeofenceExit(event)
            }
        })
        
        // Check permissions
        when (val status = geofenceManager.checkLocationPermissions()) {
            is PermissionStatus.Granted -> {
                addDeliveryGeofences()
            }
            is PermissionStatus.Denied -> {
                // Request permissions from user
                requestLocationPermissions(status.missingPermissions)
            }
        }
    }
    
    private fun addDeliveryGeofences() {
        val deliveryLocations = listOf(
            Triple("delivery_1", 37.7749, -122.4194),
            Triple("delivery_2", 37.7849, -122.4094)
        )
        
        deliveryLocations.forEach { (id, lat, lng) ->
            geofenceManager.addGeofence(
                id = id,
                latitude = lat,
                longitude = lng,
                radius = 100f,
                onSuccess = {
                    println("Added geofence: $id")
                },
                onFailure = { error ->
                    println("Failed to add geofence $id: $error")
                }
            )
        }
    }
    
    private fun handleGeofenceEnter(event: GeofenceEvent) {
        // Your business logic here
        println("Driver arrived at ${event.geofenceId}")
        // e.g., update database, send notification, etc.
    }
    
    private fun handleGeofenceExit(event: GeofenceEvent) {
        // Your business logic here
        println("Driver left ${event.geofenceId}")
        // e.g., update database, send notification, etc.
    }
    
    private fun requestLocationPermissions(missing: List<PermissionType>) {
        // Implement permission request logic
    }
}
```

## Platform-Specific Notes

### Android

- Requires Google Play Services Location API
- Minimum SDK: 24 (Android 7.0)
- Background location permission required for Android 10+
- Geofences persist across device reboots

### iOS

- Uses CoreLocation framework
- Requires "Always" location permission for background monitoring
- Requires "Full Accuracy" for iOS 14+
- Maximum 20 geofences can be monitored simultaneously per app

## Building the Library

```bash
# Build for all platforms
./gradlew build

# Build Android AAR
./gradlew :geofence:assembleRelease

# Build iOS Framework
./gradlew :geofence:linkReleaseFrameworkIosArm64
```

## Publishing

To publish to your Git repository:

1. Update version in `build.gradle.kts`
2. Build the library
3. Commit and push to your repository
4. Tag the release: `git tag v1.0.0 && git push origin v1.0.0`

## License

This library is provided as-is for use in your projects.

## Contributing

Feel free to submit issues and enhancement requests!
