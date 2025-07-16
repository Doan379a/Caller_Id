package com.example.caller_id.service

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.example.caller_id.sharePreferent.SharePrefUtils
import com.example.caller_id.widget.isInternationalNumber
import com.example.caller_id.widget.normalizeToE164

class BlockCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: return
        val context = applicationContext

        val blockTopSpammer = SharePrefUtils.getSetting(context, "CBSPAMMER")
        val blockPrivate = SharePrefUtils.getSetting(context, "CBHIDDENNUMBER")
        val blockInternational = SharePrefUtils.getSetting(context, "CBINTERNATIONALCALLS")
        val blockUnknownContacts = SharePrefUtils.getSetting(context, "CBNOTINCONTACTS")

        val isPrivateOrUnknown = number.isEmpty() || number == "Unknown"

        val numberInternational = normalizeToE164(context, number)
        val isInternational = isInternationalNumber(context, numberInternational)
        Log.d("numbercallDetails", numberInternational)
        val isNotInContacts = !isNumberInContacts(context, number)
        val isTopSpammer = isTopSpammer(number)

        val shouldBlock = (blockTopSpammer && isTopSpammer) ||
                (blockPrivate && isPrivateOrUnknown) ||
                (blockInternational && isInternational) ||
                (blockUnknownContacts && isNotInContacts)

        val response = if (shouldBlock) {
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(true)
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
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        val cursor = cr.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
        cursor?.use {
            return it.moveToFirst()
        }
        return false
    }
}
