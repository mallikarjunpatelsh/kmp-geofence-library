package com.kmp.geofence

import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.CoreLocation.*
import platform.UIKit.UIDevice
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual class GeofenceManager {
    private var eventListener: GeofenceEventListener? = null
    
    @Suppress("CONFLICTING_OVERLOADS")
    private val locationDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        
        override fun locationManager(manager: CLLocationManager, didEnterRegion: CLRegion) {
            val region = didEnterRegion as? CLCircularRegion ?: return
            
            val event = GeofenceEvent(
                geofenceId = region.identifier,
                latitude = region.center.useContents { latitude },
                longitude = region.center.useContents { longitude },
                transitionType = TransitionType.ENTER
            )
            
            eventListener?.onGeofenceEnter(event)
        }

        override fun locationManager(manager: CLLocationManager, didExitRegion: CLRegion) {
            val region = didExitRegion as? CLCircularRegion ?: return
            
            val event = GeofenceEvent(
                geofenceId = region.identifier,
                latitude = region.center.useContents { latitude },
                longitude = region.center.useContents { longitude },
                transitionType = TransitionType.EXIT
            )
            
            eventListener?.onGeofenceExit(event)
        }

        override fun locationManager(
            manager: CLLocationManager,
            didChangeAuthorizationStatus: CLAuthorizationStatus
        ) {
            when (didChangeAuthorizationStatus) {
                kCLAuthorizationStatusAuthorizedAlways -> {
                    manager.startUpdatingLocation()
                }
            }
        }
        
        override fun locationManager(
            manager: CLLocationManager,
            didDetermineState: CLRegionState,
            forRegion: CLRegion
        ) {
            if (didDetermineState == CLRegionState.CLRegionStateInside) {
                val region = forRegion as? CLCircularRegion ?: return
                
                val event = GeofenceEvent(
                    geofenceId = region.identifier,
                    latitude = region.center.useContents { latitude },
                    longitude = region.center.useContents { longitude },
                    transitionType = TransitionType.ENTER
                )
                
                eventListener?.onGeofenceEnter(event)
            }
        }
    }

    private val locationManager: CLLocationManager by lazy {
        CLLocationManager().apply {
            delegate = locationDelegate
            desiredAccuracy = kCLLocationAccuracyBest
            distanceFilter = 1.0
            
            if (isIOSVersionAtLeast(UIDevice.currentDevice.systemVersion, 9, 0)) {
                allowsBackgroundLocationUpdates = true
            }
            pausesLocationUpdatesAutomatically = false
        }
    }

    actual fun checkLocationPermissions(): PermissionStatus {
        val authStatus = locationManager.authorizationStatus()
        val isBackgroundGranted = authStatus == kCLAuthorizationStatusAuthorizedAlways
        
        val isPreciseGranted = if (isIOSVersionAtLeast(UIDevice.currentDevice.systemVersion, 14, 0)) {
            val accuracy = locationManager.accuracyAuthorization
            accuracy == CLAccuracyAuthorization.CLAccuracyAuthorizationFullAccuracy
        } else {
            true
        }

        val missingPermissions = mutableListOf<PermissionType>()
        
        if (!isBackgroundGranted) {
            missingPermissions.add(PermissionType.BACKGROUND_LOCATION)
        }
        
        if (!isPreciseGranted) {
            missingPermissions.add(PermissionType.PRECISE_LOCATION)
        }

        return if (missingPermissions.isEmpty()) {
            PermissionStatus.Granted
        } else {
            val message = buildString {
                append("Missing permissions: ")
                append(missingPermissions.joinToString(", ") { it.name })
            }
            PermissionStatus.Denied(missingPermissions, message)
        }
    }

    actual fun addGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        radius: Float,
        onSuccess: () -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val permissionStatus = checkLocationPermissions()
                if (permissionStatus !is PermissionStatus.Granted) {
                    val deniedStatus = permissionStatus as PermissionStatus.Denied
                    throw GeofencePermissionException(
                        code = "GEOFENCE_PERMISSION_DENIED",
                        message = "Location permissions are required for geofencing. ${deniedStatus.message}"
                    )
                }

                val isAlreadyMonitored = locationManager.monitoredRegions.any { region ->
                    (region as? CLRegion)?.identifier == id
                }

                if (isAlreadyMonitored) {
                    onFailure("Geofence with id '$id' is already being monitored")
                    return@launch
                }

                val region = CLCircularRegion(
                    center = CLLocationCoordinate2DMake(latitude, longitude),
                    radius = radius.toDouble(),
                    identifier = id
                ).apply {
                    notifyOnEntry = true
                    notifyOnExit = true
                }

                locationManager.startMonitoringForRegion(region)
                
                checkIfAlreadyInsideGeofence(region, latitude, longitude)
                
                onSuccess()
            } catch (e: GeofencePermissionException) {
                onFailure("[${e.code}] ${e.message}")
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to add geofence")
            }
        }
    }

    private fun checkIfAlreadyInsideGeofence(
        region: CLCircularRegion,
        latitude: Double,
        longitude: Double
    ) {
        val currentLocation = locationManager.location
        if (currentLocation != null) {
            val distance = currentLocation.distanceFromLocation(
                CLLocation(latitude = latitude, longitude = longitude)
            )

            if (distance <= region.radius) {
                val event = GeofenceEvent(
                    geofenceId = region.identifier,
                    latitude = latitude,
                    longitude = longitude,
                    transitionType = TransitionType.ENTER
                )
                eventListener?.onGeofenceEnter(event)
            }
        } else {
            locationManager.requestStateForRegion(region)
        }
    }

    actual fun removeGeofences(
        ids: List<String>,
        onSuccess: () -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        try {
            ids.forEach { id ->
                val regionToRemove: CLRegion? = locationManager.monitoredRegions.firstOrNull { region ->
                    (region as? CLRegion)?.identifier == id
                } as CLRegion?

                regionToRemove?.let { region ->
                    locationManager.stopMonitoringForRegion(region)
                }
            }
            onSuccess()
        } catch (e: Exception) {
            onFailure(e.message ?: "Failed to remove geofences")
        }
    }

    actual fun setGeofenceEventListener(listener: GeofenceEventListener?) {
        this.eventListener = listener
    }
}

actual fun createGeofenceManager(): GeofenceManager = GeofenceManager()

private fun isIOSVersionAtLeast(versionString: String, majorVersion: Int, minorVersion: Int): Boolean {
    return try {
        val versionParts = versionString.split(".")
        if (versionParts.isEmpty()) return false

        val major = versionParts[0].toIntOrNull() ?: return false
        val minor = if (versionParts.size > 1) versionParts[1].toIntOrNull() ?: 0 else 0
        
        when {
            major > majorVersion -> true
            major < majorVersion -> false
            else -> minor >= minorVersion
        }
    } catch (e: Exception) {
        false
    }
}
