# Usage Examples

## Example 1: Basic Geofence Setup

```kotlin
// Android
class MainActivity : AppCompatActivity() {
    private lateinit var geofenceManager: GeofenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        geofenceManager = GeofenceManager(this)
        setupGeofencing()
    }
    
    private fun setupGeofencing() {
        geofenceManager.setGeofenceEventListener(object : GeofenceEventListener {
            override fun onGeofenceEnter(event: GeofenceEvent) {
                showNotification("Arrived at ${event.geofenceId}")
            }
            
            override fun onGeofenceExit(event: GeofenceEvent) {
                showNotification("Left ${event.geofenceId}")
            }
        })
        
        // Add a geofence
        geofenceManager.addGeofence(
            id = "store_location",
            latitude = 37.7749,
            longitude = -122.4194,
            radius = 100f,
            onSuccess = { Log.d("Geofence", "Added successfully") },
            onFailure = { error -> Log.e("Geofence", "Error: $error") }
        )
    }
}
```

## Example 2: Multiple Geofences with Data Class

```kotlin
data class DeliveryLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 100f
)

class DeliveryManager(context: Context) {
    private val geofenceManager = GeofenceManager(context)
    private val activeDeliveries = mutableMapOf<String, DeliveryLocation>()
    
    init {
        geofenceManager.setGeofenceEventListener(object : GeofenceEventListener {
            override fun onGeofenceEnter(event: GeofenceEvent) {
                activeDeliveries[event.geofenceId]?.let { delivery ->
                    onArrival(delivery, event)
                }
            }
            
            override fun onGeofenceExit(event: GeofenceEvent) {
                activeDeliveries[event.geofenceId]?.let { delivery ->
                    onDeparture(delivery, event)
                }
            }
        })
    }
    
    fun addDeliveryLocation(location: DeliveryLocation) {
        geofenceManager.addGeofence(
            id = location.id,
            latitude = location.latitude,
            longitude = location.longitude,
            radius = location.radius,
            onSuccess = {
                activeDeliveries[location.id] = location
                println("Monitoring ${location.name}")
            },
            onFailure = { error ->
                println("Failed to monitor ${location.name}: $error")
            }
        )
    }
    
    fun removeDeliveryLocation(locationId: String) {
        geofenceManager.removeGeofences(
            ids = listOf(locationId),
            onSuccess = {
                activeDeliveries.remove(locationId)
                println("Stopped monitoring $locationId")
            },
            onFailure = { error ->
                println("Failed to remove $locationId: $error")
            }
        )
    }
    
    private fun onArrival(delivery: DeliveryLocation, event: GeofenceEvent) {
        println("Arrived at ${delivery.name}")
        // Send API call, update database, show notification, etc.
    }
    
    private fun onDeparture(delivery: DeliveryLocation, event: GeofenceEvent) {
        println("Left ${delivery.name}")
        // Send API call, update database, etc.
    }
}
```

## Example 3: Permission Handling

```kotlin
class PermissionHandler(private val activity: Activity) {
    private val geofenceManager = GeofenceManager(activity)
    
    fun checkAndRequestPermissions() {
        when (val status = geofenceManager.checkLocationPermissions()) {
            is PermissionStatus.Granted -> {
                onPermissionsGranted()
            }
            is PermissionStatus.Denied -> {
                handleDeniedPermissions(status)
            }
        }
    }
    
    private fun handleDeniedPermissions(status: PermissionStatus.Denied) {
        val permissions = mutableListOf<String>()
        
        status.missingPermissions.forEach { type ->
            when (type) {
                PermissionType.FINE_LOCATION -> {
                    permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                PermissionType.BACKGROUND_LOCATION -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }
                else -> {}
            }
        }
        
        ActivityCompat.requestPermissions(
            activity,
            permissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }
    
    private fun onPermissionsGranted() {
        // Start adding geofences
    }
    
    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
    }
}
```

## Example 4: iOS SwiftUI Integration

```swift
import SwiftUI
import geofence

class GeofenceViewModel: ObservableObject {
    private let geofenceManager: GeofenceManager
    @Published var status: String = ""
    
    init() {
        geofenceManager = GeofenceManagerKt.createGeofenceManager()
        setupListener()
    }
    
    private func setupListener() {
        geofenceManager.setGeofenceEventListener(listener: GeofenceListener())
    }
    
    func addGeofence(id: String, lat: Double, lng: Double) {
        geofenceManager.addGeofence(
            id: id,
            latitude: lat,
            longitude: lng,
            radius: 100.0,
            onSuccess: {
                self.status = "Geofence added: \(id)"
            },
            onFailure: { error in
                self.status = "Error: \(error)"
            }
        )
    }
}

class GeofenceListener: GeofenceEventListener {
    func onGeofenceEnter(event: GeofenceEvent) {
        print("Entered: \(event.geofenceId)")
        // Handle enter event
    }
    
    func onGeofenceExit(event: GeofenceEvent) {
        print("Exited: \(event.geofenceId)")
        // Handle exit event
    }
}

struct ContentView: View {
    @StateObject private var viewModel = GeofenceViewModel()
    
    var body: some View {
        VStack {
            Text(viewModel.status)
            Button("Add Geofence") {
                viewModel.addGeofence(
                    id: "location_1",
                    lat: 37.7749,
                    lng: -122.4194
                )
            }
        }
    }
}
```

## Example 5: Compose Multiplatform

```kotlin
@Composable
fun GeofenceScreen() {
    val context = LocalContext.current
    val geofenceManager = remember { GeofenceManager(context) }
    var status by remember { mutableStateOf("Ready") }
    
    LaunchedEffect(Unit) {
        geofenceManager.setGeofenceEventListener(object : GeofenceEventListener {
            override fun onGeofenceEnter(event: GeofenceEvent) {
                status = "Entered: ${event.geofenceId}"
            }
            
            override fun onGeofenceExit(event: GeofenceEvent) {
                status = "Exited: ${event.geofenceId}"
            }
        })
    }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = status)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = {
            geofenceManager.addGeofence(
                id = "test_location",
                latitude = 37.7749,
                longitude = -122.4194,
                radius = 100f,
                onSuccess = { status = "Geofence added" },
                onFailure = { error -> status = "Error: $error" }
            )
        }) {
            Text("Add Geofence")
        }
    }
}
```

## Example 6: Background Service Integration (Android)

```kotlin
class GeofenceService : Service() {
    private lateinit var geofenceManager: GeofenceManager
    
    override fun onCreate() {
        super.onCreate()
        geofenceManager = GeofenceManager(this)
        
        geofenceManager.setGeofenceEventListener(object : GeofenceEventListener {
            override fun onGeofenceEnter(event: GeofenceEvent) {
                // Handle in background
                sendToServer(event, "ENTER")
            }
            
            override fun onGeofenceExit(event: GeofenceEvent) {
                // Handle in background
                sendToServer(event, "EXIT")
            }
        })
    }
    
    private fun sendToServer(event: GeofenceEvent, type: String) {
        // Your API call logic here
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // api.sendGeofenceEvent(event, type)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
```

## Example 7: Error Handling

```kotlin
class RobustGeofenceManager(context: Context) {
    private val geofenceManager = GeofenceManager(context)
    private val maxRetries = 3
    
    fun addGeofenceWithRetry(
        id: String,
        latitude: Double,
        longitude: Double,
        radius: Float,
        retryCount: Int = 0
    ) {
        geofenceManager.addGeofence(
            id = id,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            onSuccess = {
                println("Geofence added successfully: $id")
            },
            onFailure = { error ->
                if (retryCount < maxRetries) {
                    println("Retry ${retryCount + 1}/$maxRetries for $id")
                    // Exponential backoff
                    Handler(Looper.getMainLooper()).postDelayed({
                        addGeofenceWithRetry(id, latitude, longitude, radius, retryCount + 1)
                    }, (1000L * (retryCount + 1)))
                } else {
                    println("Failed to add geofence after $maxRetries attempts: $error")
                }
            }
        )
    }
}
```
