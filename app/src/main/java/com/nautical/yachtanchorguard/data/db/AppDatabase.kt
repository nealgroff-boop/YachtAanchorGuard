package com.nautical.yachtanchorguard.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nautical.yachtanchorguard.data.model.AnchorData
import com.nautical.yachtanchorguard.data.model.GpsFix
import com.nautical.yachtanchorguard.data.model.AlarmEvent

@Database(
    entities = [AnchorData::class, GpsFix::class, AlarmEvent::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun anchorDao(): AnchorDao
    abstract fun gpsFixDao(): GpsFixDao
    abstract fun alarmEventDao(): AlarmEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yacht_anchor_guard_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
