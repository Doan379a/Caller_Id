package com.example.caller_id.database.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.caller_id.database.dao.BlockedCalledDao
import com.example.caller_id.database.dao.BlockedNumberDao
import com.example.caller_id.database.dao.DNDCalledDao
import com.example.caller_id.database.dao.SpamNumberSmsDao
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.database.entity.BlockedNumber
import com.example.caller_id.database.entity.DndTypeConverter
import com.example.caller_id.database.entity.DoNotDisturbNumber
import com.example.caller_id.database.entity.SpamNumberSms

@Database(
    entities = [BlockedNumber::class, SpamNumberSms::class,BlockedCalled::class , DoNotDisturbNumber::class],
    version = 1
)

@TypeConverters(DndTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun spamNumberSmsDao(): SpamNumberSmsDao
    abstract fun blockedCalledDao(): BlockedCalledDao
    abstract fun dndCalledDao(): DNDCalledDao
}
