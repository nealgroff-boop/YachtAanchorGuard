package com.nautical.yachtanchorguard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing the anchor position and related settings
 */
@Entity(tableName = "anchor_data")
data class AnchorData(
    @PrimaryKey
    val id: Int = 1,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val driftRadius: Float = 50f, // Default 50 meters
    val isArmed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class representing a GPS position fix
 */
@Entity(tableName = "gps_fixes")
data class GpsFix(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double,
    val bearing: Float,
    val speed: Float,
    val satellites: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class representing alarm events
 */
@Entity(tableName = "alarm_events")
data class AlarmEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val eventType: String, // "DRIFT", "GPS_LOSS", "TEST"
    val latitude: Double,
    val longitude: Double,
    val distance: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val acknowledged: Boolean = false
)

/**
 * Data class for app settings
 */
data class AppSettings(
    val units: String = "meters", // "meters" or "feet"
    val accuracyThreshold: Float = 15f, // meters
    val lossOfGpsTimeout: Int = 2, // minutes
    val gpsFixInterval: Int = 1, // seconds
    val minTrackDistance: Float = 5f, // meters
    val defaultDriftRadius: Float = 50f, // meters
    val alarmSoundUri: String = "",
    val smsPhoneNumber: String = "",
    val smsEnabled: Boolean = false,
    val smsKeyword: String = "POSITION",
    val testModeEnabled: Boolean = false
)
