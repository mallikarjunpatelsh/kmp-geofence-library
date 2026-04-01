package com.kmp.geofence

import android.content.Context

object InjectorCommon {

    data class ContextArgs(val mContext: Context) {
        init {
            // Ensure we always use application context to avoid memory leaks
            require(mContext.applicationContext != null) {
                "Context must have an application context"
            }
        }
        
        val applicationContext: Context
            get() = mContext.applicationContext
    }
}
