package com.example.caller_id.service

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import com.example.caller_id.ui.main.MainActivity
import com.example.caller_id.widget.showToast


class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            // Không cần làm gì, stub để Android nhận diện
            context.showToast("SmsReceiver")
            (context as MainActivity).loadUnreadSmsCount()
        }
    }
}
