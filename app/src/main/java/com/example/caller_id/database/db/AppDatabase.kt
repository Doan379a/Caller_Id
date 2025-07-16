package com.example.caller_id.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.caller_id.database.dao.BlockedNumberDao
import com.example.caller_id.database.dao.BlockedSmsDao
import com.example.caller_id.database.entity.BlockedNumber
import com.example.caller_id.database.entity.BlockedSms

@Database(
    entities = [BlockedNumber::class, BlockedSms::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun blockedSmsDao(): BlockedSmsDao
}
