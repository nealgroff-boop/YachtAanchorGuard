package com.nautical.yachtanchorguard.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nautical.yachtanchorguard.data.model.AnchorData
import com.nautical.yachtanchorguard.data.model.GpsFix
import com.nautical.yachtanchorguard.data.model.AlarmEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface AnchorDao {
    @Query("SELECT * FROM anchor_data WHERE id = 1")
    fun getAnchor(): Flow<AnchorData?>

    @Query("SELECT * FROM anchor_data WHERE id = 1")
    suspend fun getAnchorOnce(): AnchorData?

    @Insert
    suspend fun insertAnchor(anchor: AnchorData)

    @Update
    suspend fun updateAnchor(anchor: AnchorData)

    @Query("DELETE FROM anchor_data")
    suspend fun deleteAnchor()
}

@Dao
interface GpsFixDao {
    @Query("SELECT * FROM gps_fixes ORDER BY timestamp DESC LIMIT 1")
    fun getLatestFixFlow(): Flow<GpsFix?>

    @Query("SELECT * FROM gps_fixes ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestFix(): GpsFix?

    @Query("SELECT * FROM gps_fixes WHERE timestamp > :since ORDER BY timestamp ASC")
    suspend fun getFixesSince(since: Long): List<GpsFix>

    @Query("SELECT * FROM gps_fixes ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentFixes(limit: Int): List<GpsFix>

    @Insert
    suspend fun insertFix(fix: GpsFix)

    @Query("DELETE FROM gps_fixes WHERE timestamp < :before")
    suspend fun deleteOldFixes(before: Long)

    @Query("DELETE FROM gps_fixes")
    suspend fun deleteAllFixes()
}

@Dao
interface AlarmEventDao {
    @Query("SELECT * FROM alarm_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEvent(): AlarmEvent?

    @Query("SELECT * FROM alarm_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEvents(limit: Int): List<AlarmEvent>

    @Insert
    suspend fun insertEvent(event: AlarmEvent)

    @Update
    suspend fun updateEvent(event: AlarmEvent)

    @Query("DELETE FROM alarm_events WHERE timestamp < :before")
    suspend fun deleteOldEvents(before: Long)
}
