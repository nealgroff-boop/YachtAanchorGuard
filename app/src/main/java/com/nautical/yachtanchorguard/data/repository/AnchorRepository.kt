package com.nautical.yachtanchorguard.data.repository

import com.nautical.yachtanchorguard.data.db.AppDatabase
import com.nautical.yachtanchorguard.data.model.AnchorData
import com.nautical.yachtanchorguard.data.model.GpsFix
import com.nautical.yachtanchorguard.data.model.AlarmEvent
import kotlinx.coroutines.flow.Flow

class AnchorRepository(private val database: AppDatabase) {
    
    // Anchor operations
    fun getAnchorFlow(): Flow<AnchorData?> = database.anchorDao().getAnchor()

    suspend fun getAnchor(): AnchorData? = database.anchorDao().getAnchorOnce()

    suspend fun setAnchor(latitude: Double, longitude: Double, driftRadius: Float) {
        val anchor = AnchorData(
            latitude = latitude,
            longitude = longitude,
            driftRadius = driftRadius,
            isArmed = true,
            timestamp = System.currentTimeMillis()
        )
        val existing = database.anchorDao().getAnchorOnce()
        if (existing != null) {
            database.anchorDao().updateAnchor(anchor)
        } else {
            database.anchorDao().insertAnchor(anchor)
        }
    }

    suspend fun updateAnchorDriftRadius(radius: Float) {
        val anchor = database.anchorDao().getAnchorOnce() ?: return
        database.anchorDao().updateAnchor(anchor.copy(driftRadius = radius))
    }

    suspend fun updateAnchorArmedStatus(armed: Boolean) {
        val anchor = database.anchorDao().getAnchorOnce() ?: return
        database.anchorDao().updateAnchor(anchor.copy(isArmed = armed))
    }

    suspend fun clearAnchor() {
        database.anchorDao().deleteAnchor()
    }

    // GPS Fix operations
    suspend fun getLatestGpsFix(): GpsFix? = database.gpsFixDao().getLatestFix()

    suspend fun getRecentGpsFixesSince(since: Long): List<GpsFix> =
        database.gpsFixDao().getFixesSince(since)

    suspend fun getRecentGpsFixes(limit: Int = 100): List<GpsFix> =
        database.gpsFixDao().getRecentFixes(limit)

    suspend fun insertGpsFix(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        altitude: Double,
        bearing: Float,
        speed: Float,
        satellites: Int
    ) {
        val fix = GpsFix(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            altitude = altitude,
            bearing = bearing,
            speed = speed,
            satellites = satellites,
            timestamp = System.currentTimeMillis()
        )
        database.gpsFixDao().insertFix(fix)
    }

    suspend fun deleteOldGpsFixes(olderThanMillis: Long) {
        database.gpsFixDao().deleteOldFixes(System.currentTimeMillis() - olderThanMillis)
    }

    suspend fun clearAllGpsFixes() {
        database.gpsFixDao().deleteAllFixes()
    }

    // Alarm Event operations
    suspend fun getLatestAlarmEvent(): AlarmEvent? = database.alarmEventDao().getLatestEvent()

    suspend fun getRecentAlarmEvents(limit: Int = 50): List<AlarmEvent> =
        database.alarmEventDao().getRecentEvents(limit)

    suspend fun insertAlarmEvent(
        eventType: String,
        latitude: Double,
        longitude: Double,
        distance: Float
    ) {
        val event = AlarmEvent(
            eventType = eventType,
            latitude = latitude,
            longitude = longitude,
            distance = distance,
            timestamp = System.currentTimeMillis(),
            acknowledged = false
        )
        database.alarmEventDao().insertEvent(event)
    }

    suspend fun acknowledgeAlarmEvent(eventId: Int) {
        val event = database.alarmEventDao().getLatestEvent() ?: return
        database.alarmEventDao().updateEvent(event.copy(acknowledged = true))
    }

    suspend fun deleteOldAlarmEvents(olderThanMillis: Long) {
        database.alarmEventDao().deleteOldEvents(System.currentTimeMillis() - olderThanMillis)
    }
}
