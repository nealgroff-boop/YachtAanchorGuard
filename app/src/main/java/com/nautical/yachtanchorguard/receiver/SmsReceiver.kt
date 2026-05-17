package com.nautical.yachtanchorguard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsMessage
import com.nautical.yachtanchorguard.data.db.AppDatabase
import com.nautical.yachtanchorguard.data.preferences.PreferencesManager
import com.nautical.yachtanchorguard.data.repository.AnchorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val repository = AnchorRepository(database)
                val preferencesManager = PreferencesManager(context)
                val settings = preferencesManager.appSettingsFlow.first()

                if (!settings.smsEnabled) {
                    return@launch
                }

                val messages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    intent.getParcelableArrayExtra(Telephony.Sms.Intents.SMS_MESSAGES_KEY, SmsMessage::class.java)
                        ?.filterIsInstance<SmsMessage>() ?: emptyList()
                } else {
                    @Suppress("DEPRECATION")
                    intent.getSerializableExtra(Telephony.Sms.Intents.SMS_MESSAGES_KEY)?.let { extra ->
                        (extra as? Array<*>)?.filterIsInstance<SmsMessage>() ?: emptyList()
                    } ?: emptyList()
                }

                for (message in messages) {
                    val senderNumber = message.originatingAddress ?: continue
                    val messageBody = message.messageBody

                    // Check if message contains the keyword
                    if (messageBody.contains(settings.smsKeyword, ignoreCase = true)) {
                        val lastFix = repository.getLatestGpsFix()
                        val anchor = repository.getAnchor()

                        if (lastFix != null && anchor != null) {
                            val distance = com.nautical.yachtanchorguard.util.GpsUtils.calculateDistance(
                                lastFix.latitude,
                                lastFix.longitude,
                                anchor.latitude,
                                anchor.longitude
                            )

                            val responseMessage = buildString {
                                append("Position: ")
                                append(String.format("%.6f, %.6f", lastFix.latitude, lastFix.longitude))
                                append(". Distance from anchor: ")
                                append(String.format("%.1f m", distance))
                            }

                            sendSms(context, senderNumber, responseMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            smsManager?.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
