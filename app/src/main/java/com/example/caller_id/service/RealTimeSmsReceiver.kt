package com.example.caller_id.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.caller_id.database.dao.BlockedNumberDao
import com.example.caller_id.ui.main.MainActivity
import com.example.caller_id.widget.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RealTimeSmsReceiver : BroadcastReceiver() {
    @Inject
    lateinit var numDao: BlockedNumberDao

    companion object {
        const val ACTION_REFRESH = "com.example.caller_id.ACTION_REFRESH_SMS"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val sender = messages[0].originatingAddress ?: return
        val body = messages.joinToString("") { it.messageBody }
        val timestamp = messages[0].timestampMillis

        CoroutineScope(Dispatchers.IO).launch {
//            if (numDao.isBlocked(sender)) {
//                // Tin từ số bị block ➝ lưu vào Room DB
//                smsDao.insert(BlockedSms(address = sender, body = body, date = timestamp))
//            } else {
                // Tin bình thường ➝ chèn vào hệ thống SMS inbox
                val values = ContentValues().apply {
                    put("address", sender); put("body", body)
                    put("read", 0); put("date", timestamp); put("type", 1)
                }
                context.contentResolver.insert(Uri.parse("content://sms/inbox"), values)
//            }
            context.sendBroadcast(Intent(ACTION_REFRESH).setPackage(context.packageName))
        }
    }
}
