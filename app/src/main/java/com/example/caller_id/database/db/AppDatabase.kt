package com.example.caller_id.database.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.caller_id.database.dao.BlockedCalledDao
import com.example.caller_id.database.dao.BlockedNumberDao
import com.example.caller_id.database.dao.BlockedSmsDao
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.database.entity.BlockedNumber
import com.example.caller_id.database.entity.BlockedSms

@Database(
    entities = [BlockedNumber::class, BlockedSms::class,BlockedCalled::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun blockedSmsDao(): BlockedSmsDao
    abstract fun blockedCalledDao(): BlockedCalledDao
}
