package com.kmp.geofence

import android.content.Context

object GeofenceContext {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun get(): Context = appContext
}