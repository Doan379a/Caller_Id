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
import com.example.caller_id.sharePreferent.SharePrefUtils
import com.example.caller_id.widget.isInternationalNumber
import com.example.caller_id.widget.normalizeToE164
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BlockCallScreeningService : CallScreeningService() {
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

        if (isDoNotDisturbEnabled(context)){

        }else{

        }
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sms_db"
        ).build()

        val dao = db.blockedCalledDao()
        /// number
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

        ///Country code
        val blockedCountryList = runBlocking {
            dao.getAllBlockCalled().firstOrNull()
                ?.filter { it.type == "country" }
                ?: emptyList()
        }
        val isBlockedCountry = blockedCountryList.any { blocked ->
            numberInternational.startsWith(blocked.number)
        }

        //// start end
        val blockedStartList = runBlocking {
            dao.getAllBlockCalled().firstOrNull()
                ?.filter { it.type == "numberstart" }
                ?: emptyList()
        }
        val isBlockedStart = blockedStartList.any { blocked ->
            number.startsWith(blocked.number)
        }

        val blockedEndList = runBlocking {
            dao.getAllBlockCalled().firstOrNull()
                ?.filter { it.type == "numberend" }
                ?: emptyList()
        }
        val isBlockedEnd = blockedEndList.any { blocked ->
            number.startsWith(blocked.number)
        }

        val isPrivateOrUnknown = number.isEmpty() || number == "Unknown"

        val isInternational = isInternationalNumber(context, numberInternational)
        Log.d("numbercallDetails", numberInternational)
        val isNotInContacts = !isNumberInContacts(context, number)
        val isTopSpammer = isTopSpammer(number)


        val shouldBlock = isBlocked || isBlockedCountry || isBlockedStart || isBlockedEnd ||
                (blockTopSpammer && isTopSpammer) ||
                (blockPrivate && isPrivateOrUnknown) ||
                (blockInternational && isInternational) ||
                (blockUnknownContacts && isNotInContacts)

        val response = if (shouldBlock) {
            CallResponse.Builder()
                .setDisallowCall(true) //không hiện ui
                .setRejectCall(true) // từ chối
                .setSkipCallLog(false) // ghi nhật ký
                .setSkipNotification(true)  // không thông báo
                .build()
        } else {
            CallResponse.Builder().build()
        }

        respondToCall(callDetails, response)
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
