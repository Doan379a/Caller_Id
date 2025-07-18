package com.example.caller_id.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_numbers")
data class BlockedNumber(@PrimaryKey val number: String)

@Entity(tableName = "spam_numbers_sms")
data class SpamNumberSms(@PrimaryKey val number: String)

@Entity(tableName = "blocked_called")
data class BlockedCalled(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val type: String ,
    val isSpam: Boolean = false
)
