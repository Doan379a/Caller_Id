package com.example.caller_id.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    // Stub, để hệ thống công nhận app hỗ trợ quick reply
}
