package com.example.caller_id.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.caller_id.model.DndType

@Entity(tableName = "blocked_numbers")
data class BlockedNumber(@PrimaryKey val number: String)

@Entity(tableName = "spam_numbers_sms")
data class SpamNumberSms(@PrimaryKey val number: String)

@Entity(
    tableName = "blocked_called",
    indices = [Index(value = ["number", "type"], unique = true)]
)
data class BlockedCalled(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val name: String,
    val type: String ,
    val isSpam: Boolean = false
)

@Entity(tableName = "do_not_disturb")
data class DoNotDisturbNumber(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val type: DndType,
    val endTimeMillis: Long = 0L,        // Dùng cho TIMER
    val remainingCount: Int = 0,         // Dùng cho COUNTER
    val createdAt: Long = System.currentTimeMillis()
)

