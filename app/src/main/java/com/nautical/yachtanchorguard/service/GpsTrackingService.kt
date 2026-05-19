package com.nautical.yachtanchorguard.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nautical.yachtanchorguard.MainActivity
import com.nautical.yachtanchorguard.R
import com.nautical.yachtanchorguard.data.db.AppDatabase
import com.nautical.yachtanchorguard.data.preferences.PreferencesManager
import com.nautical.yachtanchorguard.data.repository.AnchorRepository
import com.nautical.yachtanchorguard.util.GpsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GpsTrackingService : Service(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var repository: AnchorRepository
    private lateinit var preferencesManager: PreferencesManager
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var lastGpsLossAlarmTime = 0L
    private var lastLocationTime = System.currentTimeMillis()

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val database = AppDatabase.getDatabase(this)
        repository = AnchorRepository(database)
        preferencesManager = PreferencesManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        serviceScope.launch {
            try {
                val settings = preferencesManager.appSettingsFlow.first()
                
                // Request location updates
                val providers = listOf(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER
                )

                for (provider in providers) {
                    if (locationManager.isProviderEnabled(provider)) {
                        try {
                            locationManager.requestLocationUpdates(
                                provider,
                                (settings.gpsFixInterval * 1000).toLong(),
                                0f,
                                this@GpsTrackingService
                            )
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                }

                startForeground(NOTIFICATION_ID, createNotification("GPS Tracking Active"))

                // Monitor GPS loss
                monitorGpsLoss(settings.lossOfGpsTimeout * 60 * 1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopTracking() {
        try {
            locationManager.removeUpdates(this)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onLocationChanged(location: Location) {
        lastLocationTime = System.currentTimeMillis()
        
        serviceScope.launch {
            try {
                val settings = preferencesManager.appSettingsFlow.first()
                val anchor = repository.getAnchorOnce() ?: return@launch

                // Filter by accuracy threshold
                if (location.accuracy > settings.accuracyThreshold) {
                    return@launch
                }

                // Store GPS fix
                repository.insertGpsFix(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    altitude = location.altitude,
                    bearing = location.bearing,
                    speed = location.speed,
                    satellites = 0
                )

                // Check if within drift radius
                val distance = GpsUtils.calculateDistance(
                    location.latitude,
                    location.longitude,
                    anchor.latitude,
                    anchor.longitude
                )

                if (anchor.isArmed && distance > anchor.driftRadius) {
                    // Trigger alarm
                    triggerDriftAlarm(location.latitude, location.longitude, distance)
                }

                // Update notification
                val distanceStr = GpsUtils.formatDistance(distance, settings.units)
                updateNotification("Distance to anchor: $distanceStr")

                // Clean up old data
                repository.deleteOldGpsFixes(24 * 60 * 60 * 1000) // 24 hours
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun monitorGpsLoss(timeoutMillis: Int) {
        serviceScope.launch {
            while (true) {
                delay(5000) // Check every 5 seconds
                val timeSinceLastUpdate = System.currentTimeMillis() - lastLocationTime
                
                if (timeSinceLastUpdate > timeoutMillis) {
                    if (System.currentTimeMillis() - lastGpsLossAlarmTime > timeoutMillis) {
                        triggerGpsLossAlarm()
                        lastGpsLossAlarmTime = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    private suspend fun triggerDriftAlarm(latitude: Double, longitude: Double, distance: Float) {
        repository.insertAlarmEvent("DRIFT", latitude, longitude, distance)
        sendBroadcast(Intent(ACTION_DRIFT_ALARM).apply {
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("distance", distance)
        })
    }

    private suspend fun triggerGpsLossAlarm() {
        val lastFix = repository.getLatestGpsFix()
        if (lastFix != null) {
            repository.insertAlarmEvent("GPS_LOSS", lastFix.latitude, lastFix.longitude, 0f)
        }
        sendBroadcast(Intent(ACTION_GPS_LOSS_ALARM))
    }

    private fun createNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Yacht Anchor Guard")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GPS Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            locationManager.removeUpdates(this)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "gps_tracking_channel"
        const val ACTION_START = "com.nautical.yachtanchorguard.START_TRACKING"
        const val ACTION_STOP = "com.nautical.yachtanchorguard.STOP_TRACKING"
        const val ACTION_DRIFT_ALARM = "com.nautical.yachtanchorguard.DRIFT_ALARM"
        const val ACTION_GPS_LOSS_ALARM = "com.nautical.yachtanchorguard.GPS_LOSS_ALARM"
    }
}
