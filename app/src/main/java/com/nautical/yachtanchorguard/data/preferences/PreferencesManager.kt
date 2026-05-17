package com.nautical.yachtanchorguard.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nautical.yachtanchorguard.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class PreferencesManager(private val context: Context) {
    companion object {
        private val UNITS = stringPreferencesKey("units")
        private val ACCURACY_THRESHOLD = floatPreferencesKey("accuracy_threshold")
        private val LOSS_OF_GPS_TIMEOUT = intPreferencesKey("loss_of_gps_timeout")
        private val GPS_FIX_INTERVAL = intPreferencesKey("gps_fix_interval")
        private val MIN_TRACK_DISTANCE = floatPreferencesKey("min_track_distance")
        private val DEFAULT_DRIFT_RADIUS = floatPreferencesKey("default_drift_radius")
        private val ALARM_SOUND_URI = stringPreferencesKey("alarm_sound_uri")
        private val SMS_PHONE_NUMBER = stringPreferencesKey("sms_phone_number")
        private val SMS_ENABLED = booleanPreferencesKey("sms_enabled")
        private val SMS_KEYWORD = stringPreferencesKey("sms_keyword")
        private val TEST_MODE_ENABLED = booleanPreferencesKey("test_mode_enabled")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            units = preferences[UNITS] ?: "meters",
            accuracyThreshold = preferences[ACCURACY_THRESHOLD] ?: 15f,
            lossOfGpsTimeout = preferences[LOSS_OF_GPS_TIMEOUT] ?: 2,
            gpsFixInterval = preferences[GPS_FIX_INTERVAL] ?: 1,
            minTrackDistance = preferences[MIN_TRACK_DISTANCE] ?: 5f,
            defaultDriftRadius = preferences[DEFAULT_DRIFT_RADIUS] ?: 50f,
            alarmSoundUri = preferences[ALARM_SOUND_URI] ?: "",
            smsPhoneNumber = preferences[SMS_PHONE_NUMBER] ?: "",
            smsEnabled = preferences[SMS_ENABLED] ?: false,
            smsKeyword = preferences[SMS_KEYWORD] ?: "POSITION",
            testModeEnabled = preferences[TEST_MODE_ENABLED] ?: false
        )
    }

    suspend fun updateUnits(units: String) {
        context.dataStore.edit { preferences ->
            preferences[UNITS] = units
        }
    }

    suspend fun updateAccuracyThreshold(threshold: Float) {
        context.dataStore.edit { preferences ->
            preferences[ACCURACY_THRESHOLD] = threshold
        }
    }

    suspend fun updateLossOfGpsTimeout(timeout: Int) {
        context.dataStore.edit { preferences ->
            preferences[LOSS_OF_GPS_TIMEOUT] = timeout
        }
    }

    suspend fun updateGpsFixInterval(interval: Int) {
        context.dataStore.edit { preferences ->
            preferences[GPS_FIX_INTERVAL] = interval
        }
    }

    suspend fun updateMinTrackDistance(distance: Float) {
        context.dataStore.edit { preferences ->
            preferences[MIN_TRACK_DISTANCE] = distance
        }
    }

    suspend fun updateDefaultDriftRadius(radius: Float) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_DRIFT_RADIUS] = radius
        }
    }

    suspend fun updateAlarmSoundUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[ALARM_SOUND_URI] = uri
        }
    }

    suspend fun updateSmsPhoneNumber(phoneNumber: String) {
        context.dataStore.edit { preferences ->
            preferences[SMS_PHONE_NUMBER] = phoneNumber
        }
    }

    suspend fun updateSmsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SMS_ENABLED] = enabled
        }
    }

    suspend fun updateSmsKeyword(keyword: String) {
        context.dataStore.edit { preferences ->
            preferences[SMS_KEYWORD] = keyword
        }
    }

    suspend fun updateTestModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TEST_MODE_ENABLED] = enabled
        }
    }
}
