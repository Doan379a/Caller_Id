package com.example.caller_id.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.caller_id.R
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.model.SmsMessage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SmsUtils {

    fun formatSmsTimestamp(context: Context, timestamp: Long): String {
        val now = Calendar.getInstance()
        val msgTime = Calendar.getInstance()
        msgTime.timeInMillis = timestamp

        return if (
            now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)
        ) {
            // Nếu cùng ngày → hiển thị giờ và AM/PM
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeFormat.format(Date(timestamp))
        } else {
            // Nếu khác ngày → hiển thị ngày và tên tháng (sử dụng getString nếu muốn dịch)
            val day = msgTime.get(Calendar.DAY_OF_MONTH)
            val month = msgTime.get(Calendar.MONTH) // từ 0 đến 11

            // Lấy tên tháng từ string resources nếu muốn hỗ trợ đa ngôn ngữ
            val monthName = when (month) {
                0 -> context.getString(R.string.january)
                1 -> context.getString(R.string.february)
                2 -> context.getString(R.string.march)
                3 -> context.getString(R.string.april)
                4 -> context.getString(R.string.may)
                5 -> context.getString(R.string.june)
                6 -> context.getString(R.string.july)
                7 -> context.getString(R.string.august)
                8 -> context.getString(R.string.september)
                9 -> context.getString(R.string.october)
                10 -> context.getString(R.string.november)
                11 -> context.getString(R.string.december)
                else -> ""
            }

            "$day $monthName"
        }
    }

    fun getGroupedSmsInbox(context: Context): MutableList<SmsConversation> {
        val unreadCountMap = mutableMapOf<String, Int>()
        val latestMap = mutableMapOf<String, SmsConversation>()

        val uri = Uri.parse("content://sms")
        val projection = arrayOf("address", "body", "date", "read")

        val cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "date DESC"
        )

        cursor?.use {
            val idxAddress = it.getColumnIndex("address")
            val idxBody = it.getColumnIndex("body")
            val idxDate = it.getColumnIndex("date")
            val idxRead = it.getColumnIndex("read")

            while (it.moveToNext()) {
                val address = it.getString(idxAddress) ?: continue
                val body = it.getString(idxBody) ?: ""
                val date = it.getLong(idxDate)
                val read = it.getInt(idxRead)

                // Đếm chưa đọc
                if (read == 0) {
                    unreadCountMap[address] = unreadCountMap.getOrDefault(address, 0) + 1
                }

                // Lưu tin mới nhất theo người gửi
                val existing = latestMap[address]
//                val name = lookupContactName(context, address)
                if (existing == null || date > existing.date) {
                    latestMap[address] = SmsConversation(
                        address = address,
//                        displayName = if (name.isNotEmpty()) name else address,
                        latestMessage = body,
                        date = date,
                        unreadCount = unreadCountMap[address] ?: 0
                    )
                }
            }
        }

        return latestMap.values.sortedByDescending { it.date }.toMutableList()
    }


    @SuppressLint("Range")
    fun lookupContactName(context: Context, phoneNumber: String): String {
        if (phoneNumber.isBlank()) return ""

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) return ""

        val lookupUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        context.contentResolver.query(lookupUri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (index >= 0) return cursor.getString(index)
            }
        }

        return ""
    }

    fun loadSmsByAddress(context: Context, address: String): List<SmsMessage> {
        val smsList = mutableListOf<SmsMessage>()
        val uri = Uri.parse("content://sms/")
        val cursor = context.contentResolver.query(
            uri,
            arrayOf("address", "body", "date", "type", "read"),
            "address = ?",
            arrayOf(address),
            "date ASC"
        )

        cursor?.use {
            val indexAddress = it.getColumnIndex("address")
            val indexBody = it.getColumnIndex("body")
            val indexDate = it.getColumnIndex("date")
            val indexRead = it.getColumnIndex("read")
            val indexType = it.getColumnIndex("type")

            while (it.moveToNext()) {
                val isSent = it.getInt(indexType) == 2 // 1: received, 2: sent
                smsList.add(
                    SmsMessage(
                        address = it.getString(indexAddress),
                        body = it.getString(indexBody),
                        date = it.getLong(indexDate),
                        read = it.getInt(indexRead) == 1,
                        isSentByMe = isSent
                    )
                )
            }
        }
        return smsList
    }

    fun getThreadIdForAddress(context: Context, address: String): Long? {
        val uri = Uri.parse("content://sms")
        val projection = arrayOf("thread_id")
        val selection = "address = ?"
        val cursor = context.contentResolver.query(uri, projection, selection, arrayOf(address), "date DESC")

        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex("thread_id")
                if (idx != -1) return it.getLong(idx)
            }
        }
        return null
    }

    fun getAllBlockedNumbers(context: Context): List<String> {
        val blocked = mutableListOf<String>()
        if (!BlockedNumberContract.canCurrentUserBlockNumbers(context)) return blocked

        val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
        val projection = arrayOf(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
            while (cursor.moveToNext()) {
                cursor.getString(idx)?.let { blocked.add(it) }
            }
        }
        return blocked
    }

}