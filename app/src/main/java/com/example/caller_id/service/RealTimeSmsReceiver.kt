package com.example.caller_id.service

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat

class RealTimeSmsReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_REFRESH = "com.example.caller_id.ACTION_REFRESH_SMS"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            // Tin nhắn mới đến!
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    android.Manifest.permission.READ_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("DOAN_1", "Không có quyền READ_SMS")
                return
            }

            // Trích xuất tin nhắn từ Intent
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.originatingAddress
                val messageBody = sms.messageBody
                Log.d("DOAN_1", "Tin nhắn mới từ $sender: $messageBody")

                // Chèn tin nhắn vào cơ sở dữ liệu SMS
                val values = ContentValues().apply {
                    put("address", sms.originatingAddress)
                    put("body", sms.messageBody)
                    put("read", 0) // Đánh dấu tin nhắn là chưa đọc
                    put("date", sms.timestampMillis)
                    put("type", 1) // 1 = inbox
                }
                context.contentResolver.insert(Uri.parse("content://sms/inbox"), values)
                val refresh = Intent(ACTION_REFRESH).apply {
                    setPackage(context.packageName)
                }
                context.sendBroadcast(refresh)

            }
        }
    }
}
