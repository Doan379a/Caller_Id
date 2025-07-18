package com.example.caller_id.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.caller_id.database.dao.BlockedCalledDao
import com.example.caller_id.database.dao.BlockedNumberDao
import com.example.caller_id.database.dao.SpamNumberSmsDao
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.database.entity.BlockedNumber
import com.example.caller_id.database.entity.SpamNumberSms

@Database(
    entities = [BlockedNumber::class, SpamNumberSms::class,BlockedCalled::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun spamNumberSmsDao(): SpamNumberSmsDao
    abstract fun blockedCalledDao(): BlockedCalledDao
}
