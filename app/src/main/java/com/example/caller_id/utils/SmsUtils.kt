package com.example.caller_id.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.PhoneNumberUtils.normalizeNumber
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.caller_id.R
import com.example.caller_id.model.ContactModel
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.model.SmsMessage
import com.example.caller_id.widget.getLogDebug
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object SmsUtils {
    private val patternsByRegion = mutableMapOf<String, Pattern>()

    fun init(context: Context) {
        val parser = context.resources.getXml(R.xml.short_codes)
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "shortcode") {
                val country = parser.getAttributeValue(null, "country")?.uppercase(Locale.ROOT)
                val pattern = parser.getAttributeValue(null, "pattern")
                if (country != null && pattern != null) {
                    patternsByRegion[country] = Pattern.compile(pattern)
                }
            }
            eventType = parser.next()
        }
    }

    fun getCheckAddress(address: String): String {
        val local = toNational(address) ?: address
        getLogDebug("OOO","dsssssss+$local")
        val toSend = try {
            if (isShortCode(local)) {
                local  // gửi đúng "191"
            } else {
                val proto =
                    PhoneNumberUtil.getInstance().parse(address, Locale.getDefault().country)
                PhoneNumberUtil.getInstance().format(proto, PhoneNumberUtil.PhoneNumberFormat.E164)
            }
        } catch (e: Exception) {
            getLogDebug("LOI_CHAT_ALL", "error $e")
            local
        }
        return toSend
    }

    fun isShortCode(input: String): Boolean {
        val region = Locale.getDefault().country
        val pattern = patternsByRegion[region] ?: return false
        return pattern.matcher(input).matches()
    }

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

    suspend fun getGroupedSmsInbox(context: Context): MutableList<SmsConversation> =
        withContext(Dispatchers.IO) {
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
                    val raw = it.getString(idxAddress) ?: continue
                    val normalized = getCheckAddress(raw) ?: raw
//                val local = toNational(raw) ?: raw
                    val body = it.getString(idxBody) ?: ""
                    val date = it.getLong(idxDate)
                    val read = it.getInt(idxRead)

                    if (read == 0) {
                        unreadCountMap[normalized] = unreadCountMap.getOrDefault(normalized, 0) + 1
                    }
                    Log.d(
                        "Doan_3",
                        "Processing SMS from: $normalized, body: $body, date: $date, read: $read"
                    )

                    val existing = latestMap[normalized]
//                val name = contactsMap[normalized] ?: normalized
//                val name = lookupContactName(context, address)
                    if (existing == null || date > existing.date) {
                        latestMap[normalized] = SmsConversation(
                            address = normalized,
//                        displayName =  name ,
                            latestMessage = body,
                            date = date,
                            unreadCount = unreadCountMap[normalized] ?: 0
                        )
                    }
                }
            }
            latestMap.values.sortedByDescending { it.date }.toMutableList()
        }

    fun normalizePhone(raw: String): String? {
        val pu = PhoneNumberUtil.getInstance()
        return try {
            val phoneProto = if (raw.startsWith("+")) {
                pu.parse(raw, "")  // không dùng null
            } else {
                pu.parse(raw, Locale.getDefault().country)
            }
            pu.format(phoneProto, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            null
        }
    }

    fun toNational(rawE164: String): String? {
        val pu = PhoneNumberUtil.getInstance()
        return try {
            val proto = pu.parse(rawE164, Locale.getDefault().country)
            if (pu.isValidNumber(proto)) {
                pu.format(proto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
            } else {
                // fallback: xoá +<mã nước> nếu có
                rawE164.substringAfter('+').substringAfter("${proto.countryCode}")
            }
        } catch (e: Exception) {
            null
        }
    }



    @SuppressLint("Range")
    fun lookupContactName(context: Context, phoneNumber: String): String {
        if (phoneNumber.isBlank()) return ""

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("DOAN_4", "ko quyen ")
            return ""
        }

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
        val projection = arrayOf("address", "body", "date", "type", "read", "_id")

        fun fetchFor(arg: String?) {
            if (arg.isNullOrBlank()) return
            context.contentResolver.query(uri, projection, "address = ?", arrayOf(arg), "date ASC")
                ?.use { cur ->
                    if (cur.moveToFirst()) {
                        do {
                            val isSent = cur.getInt(cur.getColumnIndexOrThrow("type")) == 2
                            smsList += SmsMessage(
                                address = cur.getString(cur.getColumnIndexOrThrow("address")),
                                body = cur.getString(cur.getColumnIndexOrThrow("body")),
                                date = cur.getLong(cur.getColumnIndexOrThrow("date")),
                                read = cur.getInt(cur.getColumnIndexOrThrow("read")) == 1,
                                isSentByMe = isSent
                            )
                        } while (cur.moveToNext())
                    }
                }
        }

        fetchFor(address)
        if (smsList.isNotEmpty()) return smsList

        val alt = toNational(address)
        fetchFor(alt)
        return smsList
    }


    fun getThreadIdForAddress(context: Context, address: String): Long? {
        val uri = Uri.parse("content://sms")
        val projection = arrayOf("thread_id")
        val selection = "address = ?"
        val cursor =
            context.contentResolver.query(uri, projection, selection, arrayOf(address), "date DESC")

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
            val idx =
                cursor.getColumnIndex(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
            while (cursor.moveToNext()) {
                cursor.getString(idx)?.let { blocked.add(it) }
            }
        }
        return blocked
    }

    fun markSmsAsRead(context: Context, from: String, body: String) {
        val uri = Uri.parse("content://sms/inbox")
        val selection = "address = ? AND body = ? AND read = 0"
        val args = arrayOf(from, body)
        val values = ContentValues().apply { put("read", 1) }
        val updated = context.contentResolver.update(uri, values, selection, args)
        Log.d("SMS", "Đã đánh dấu $updated tin nhắn là đã đọc")
    }

    suspend fun loadContacts(context: Context): List<ContactModel> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ContactModel>()
        val cr = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cursor = cr.query(
            uri, projection, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
            val idxName = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val idxNum = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(idxName) ?: ""
                val number = it.getString(idxNum) ?: ""
                list.add(ContactModel(name, number))
            }
        }
        list
    }

    suspend fun loadConversationAddresses(context: Context): List<String> =
        withContext(Dispatchers.IO) {
            val list = mutableSetOf<String>()
            val uri = Uri.parse("content://sms")
            val projection = arrayOf("address")
            val cursor = context.contentResolver.query(uri, projection, null, null, "date DESC")

            cursor?.use { c ->
                val idx = c.getColumnIndex("address")
                while (c.moveToNext()) {
                    val raw = c.getString(idx) ?: continue
                    val normalized = normalizePhone(raw) ?: raw
                    list.add(normalized)
                }
            }
            list.toList()
        }


}