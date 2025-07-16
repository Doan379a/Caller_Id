package com.example.caller_id.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_numbers")
data class BlockedNumber(@PrimaryKey val number: String)
@Entity(tableName = "blocked_sms")
data class BlockedSms(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val address: String,
    val body: String,
    val date: Long,
    val read: Boolean = false,
    val isSentByMe: Boolean = false,
    val status: Int = 0  // tương ứng với SmsSendStatus
)

@Entity(tableName = "blocked_called")
data class BlockedCalled(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val type: String ,
    val isSpam: Boolean = false
)
