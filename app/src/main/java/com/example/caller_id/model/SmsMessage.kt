package com.example.caller_id.model

enum class SmsSendStatus {
    SENDING, SENT, FAILED
}

data class SmsMessage(
    val address: String,
    val body: String,
    val date: Long,
    val read: Boolean,
    val isSentByMe: Boolean = false,
    var status: SmsSendStatus = SmsSendStatus.SENT // default SENT nếu là tin cũ
)

data class SmsConversation(
    val address: String,
    val displayName: String="",
    val latestMessage: String,
    val date: Long,
    val unreadCount: Int
)
