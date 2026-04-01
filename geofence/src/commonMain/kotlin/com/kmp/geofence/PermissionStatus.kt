package com.kmp.geofence

sealed class PermissionStatus {
    object Granted : PermissionStatus()
    
    data class Denied(
        val missingPermissions: List<PermissionType>,
        val message: String
    ) : PermissionStatus()
}

enum class PermissionType {
    FINE_LOCATION,
    BACKGROUND_LOCATION,
    PRECISE_LOCATION
}
