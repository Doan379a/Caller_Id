package com.example.caller_id.sharePreferent

import android.content.Context
import android.content.SharedPreferences


class PreferenceManager(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "Caller Id"
    }

    fun getSharedPrefs(): SharedPreferences = sharedPref



}


