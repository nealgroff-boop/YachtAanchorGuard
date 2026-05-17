package com.nautical.yachtanchorguard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nautical.yachtanchorguard.service.GpsTrackingService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Start GPS tracking service if not already running
        val serviceIntent = Intent(context, GpsTrackingService::class.java)
        serviceIntent.action = GpsTrackingService.ACTION_START
        context.startService(serviceIntent)
    }
}
