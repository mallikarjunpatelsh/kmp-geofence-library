package com.kmp.geofence

interface GeofenceEventListener {
    fun onGeofenceEnter(event: GeofenceEvent)
    fun onGeofenceExit(event: GeofenceEvent)
}
