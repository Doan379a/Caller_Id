package com.example.caller_id

import android.app.Application
import com.example.caller_id.utils.SmsUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application(){
    override fun onCreate() {
        super.onCreate()
        SmsUtils.init(applicationContext)
    }
}
