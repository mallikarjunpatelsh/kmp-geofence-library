package com.kmp.geofence

data class GeofenceEvent(
    val geofenceId: String,
    val latitude: Double,
    val longitude: Double,
    val transitionType: TransitionType
)

enum class TransitionType {
    ENTER,
    EXIT
}
