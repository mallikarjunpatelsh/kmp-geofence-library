package com.kmp.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private var eventCallback: ((GeofenceEvent) -> Unit)? = null
        
        fun setEventListener(callback: (GeofenceEvent) -> Unit) {
            eventCallback = callback
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        
        if (geofencingEvent.hasError()) {
            return
        }
        
        val transitionType = geofencingEvent.geofenceTransition
        val geofenceList = geofencingEvent.triggeringGeofences ?: return

        val transition = when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> TransitionType.ENTER
            Geofence.GEOFENCE_TRANSITION_EXIT -> TransitionType.EXIT
            else -> return
        }

        geofenceList.forEach { geofence ->
            val event = GeofenceEvent(
                geofenceId = geofence.requestId,
                latitude = geofencingEvent.triggeringLocation?.latitude ?: 0.0,
                longitude = geofencingEvent.triggeringLocation?.longitude ?: 0.0,
                transitionType = transition
            )
            eventCallback?.invoke(event)
        }
    }
}
