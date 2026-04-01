package com.kmp.geofence

expect class GeofenceManager {
    fun addGeofence(
        id: String,
        latitude: Double,
        longitude: Double,
        radius: Float,
        onSuccess: () -> Unit,
        onFailure: (error: String) -> Unit
    )
    
    fun removeGeofences(
        ids: List<String>,
        onSuccess: () -> Unit,
        onFailure: (error: String) -> Unit
    )
    
    fun checkLocationPermissions(): PermissionStatus
    
    fun setGeofenceEventListener(listener: GeofenceEventListener?)
}

expect fun createGeofenceManager(): GeofenceManager
