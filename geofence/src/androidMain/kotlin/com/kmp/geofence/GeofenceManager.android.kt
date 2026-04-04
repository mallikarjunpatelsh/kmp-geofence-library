package com.kmp.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

actual class GeofenceManager {
    private val context: Context = GeofenceContext.get()
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        // NO action
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    actual fun checkLocationPermissions(): PermissionStatus {
        val missingPermissions = mutableListOf<PermissionType>()

        val fineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (!fineLocation) missingPermissions.add(PermissionType.FINE_LOCATION)
        if (!backgroundLocation) missingPermissions.add(PermissionType.BACKGROUND_LOCATION)

        return if (missingPermissions.isEmpty()) {
            PermissionStatus.Granted
        } else {
            PermissionStatus.Denied(
                missingPermissions,
                "Missing permissions: ${missingPermissions.joinToString(", ") { it.name }}"
            )
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
        try {
            val permissionStatus = checkLocationPermissions()
            if (permissionStatus !is PermissionStatus.Granted) {
                val deniedStatus = permissionStatus as PermissionStatus.Denied
                throw GeofencePermissionException(
                    code = "GEOFENCE_PERMISSION_DENIED",
                    message = "Location permissions are required. ${deniedStatus.message}"
                )
            }

            val geofence = Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .build()

            val request = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            geofencingClient.addGeofences(request, geofencePendingIntent)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception ->
                    onFailure(exception.message ?: "Failed to add geofence")
                }
        } catch (e: GeofencePermissionException) {
            throw e
        } catch (e: Exception) {
            onFailure(e.message ?: "Unknown error occurred")
        }
    }

    actual fun removeGeofences(
        ids: List<String>,
        onSuccess: () -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        geofencingClient.removeGeofences(ids)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Failed to remove geofences")
            }
    }

    // Now setGeofenceEventListener sets directly on BroadcastReceiver
    actual fun setGeofenceEventListener(listener: GeofenceEventListener?) {
        if (listener == null) {
            GeofenceBroadcastReceiver.setEventListener { }
        } else {
            GeofenceBroadcastReceiver.setEventListener { event ->
                when (event.transitionType) {
                    TransitionType.ENTER -> listener.onGeofenceEnter(event)
                    TransitionType.EXIT -> listener.onGeofenceExit(event)
                }
            }
        }
    }
}

actual fun createGeofenceManager(): GeofenceManager {
    return GeofenceManager()
}
