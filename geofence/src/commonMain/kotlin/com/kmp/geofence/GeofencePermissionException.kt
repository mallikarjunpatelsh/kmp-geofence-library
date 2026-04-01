package com.kmp.geofence

class GeofencePermissionException(
    val code: String,
    message: String
) : Exception(message)
