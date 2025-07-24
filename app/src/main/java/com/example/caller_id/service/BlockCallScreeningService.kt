package com.example.caller_id.service

import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.room.Room
import com.example.caller_id.database.db.AppDatabase
import com.example.caller_id.database.repository.BlockRepository
import com.example.caller_id.model.DndType
import com.example.caller_id.sharePreferent.SharePrefUtils
import com.example.caller_id.widget.isInternationalNumber
import com.example.caller_id.widget.normalizeToE164
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BlockCallScreeningService : CallScreeningService()  {
    @Inject
    lateinit var repo: BlockRepository

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: return
        val context = applicationContext

        val numberInternational = normalizeToE164(context, number)
        val blockTopSpammer = SharePrefUtils.getSetting(context, "CBSPAMMER")
        val blockPrivate = SharePrefUtils.getSetting(context, "CBHIDDENNUMBER")
        val blockInternational = SharePrefUtils.getSetting(context, "CBINTERNATIONALCALLS")
        val blockUnknownContacts = SharePrefUtils.getSetting(context, "CBNOTINCONTACTS")
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sms_db"
        ).build()
        val dao = db.blockedCalledDao()
        val dao2 = db.dndCalledDao()

        if (!isDoNotDisturbEnabled(context)) {
            val blockedList = runBlocking { dao.getAllBlockCalled().firstOrNull() ?: emptyList() }

            val isBlocked = blockedList.any {
                it.type == "number" && normalizeToE164(context, it.number) == numberInternational
            }

            val isBlockedCountry = blockedList.any {
                it.type == "country" && numberInternational.startsWith(it.number)
            }

            val isBlockedStart = blockedList.any {
                it.type == "numberstart" && number.startsWith(it.number)
            }

            val isBlockedEnd = blockedList.any {
                it.type == "numberend" && number.startsWith(it.number)
            }

            val isPrivateOrUnknown = number.isEmpty() || number == "Unknown"
            val isInternational = isInternationalNumber(context, numberInternational)
            val isNotInContacts = !isNumberInContacts(context, number)
            val isTopSpammer = isTopSpammer(number)

            val shouldBlock = isBlocked || isBlockedCountry || isBlockedStart || isBlockedEnd ||
                    (blockTopSpammer && isTopSpammer) ||
                    (blockPrivate && isPrivateOrUnknown) ||
                    (blockInternational && isInternational) ||
                    (blockUnknownContacts && isNotInContacts)

            if (shouldBlock) {
                // Nếu bị block thì trả về luôn, không xét DND nữa
                val response = CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(true)
                    .setSkipNotification(true)
                    .build()

                respondToCall(callDetails, response)
                return
            }

            // Nếu không bị block thì mới tiếp tục kiểm tra DND
            val dndList = runBlocking { dao2.getAll().firstOrNull() ?: emptyList() }

            val dndMatch = dndList.find {
                normalizeToE164(context, it.number) == numberInternational
            }

            val isDndMatchedAndValid = dndMatch?.let {
                when (it.type) {
                    DndType.MANUALLY -> true
                    DndType.TIMER -> it.endTimeMillis > System.currentTimeMillis()
                    DndType.COUNTER -> it.remainingCount > 0
                    else -> false
                }
            } ?: false

            if (isDndMatchedAndValid) {
                val response = CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(false)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build()

                respondToCall(callDetails, response)

                CoroutineScope(Dispatchers.IO).launch {
                    if (dndMatch?.type == DndType.COUNTER) {
                        DndListenerManager.listener?.onDndUpdated(dndMatch)
                    }
                }


                return
            }

            // Nếu không bị block và không thuộc DND thì cho phép cuộc gọi
            respondToCall(callDetails, CallResponse.Builder().build())
        } else {
            val blockedNumberList = runBlocking {
                dao.getAllBlockCalled().firstOrNull()
                    ?.filter { it.type == "number" }
                    ?: emptyList()
            }

            val isBlocked = blockedNumberList.any {
                try {
                    normalizeToE164(context, it.number) == numberInternational
                } catch (e: Exception) {
                    false
                }
            }

            val response = if (isBlocked) {
                CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(true)
                    .setSkipNotification(true)
                    .build()
            } else {
                CallResponse.Builder().build()
            }

            respondToCall(callDetails, response)
        }
    }

    private fun isTopSpammer(number: String): Boolean {
        val topSpammerList = listOf(
            "0364789779", "+84123456789", "+84345678901"
        )
        return topSpammerList.contains(number)
    }

    private fun isNumberInContacts(context: Context, number: String): Boolean {
        val cr = context.contentResolver
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        val cursor = cr.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
        cursor?.use {
            return it.moveToFirst()
        }
        return false
    }

    fun isDoNotDisturbEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (!notificationManager.isNotificationPolicyAccessGranted) {
                return false
            }

            return notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
        }
        return false
    }

}
