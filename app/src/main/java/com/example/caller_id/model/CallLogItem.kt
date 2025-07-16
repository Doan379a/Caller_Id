package com.example.caller_id.model

data class CallLogItem(
    val name: String?,
    val number: String,
    val date: String,
    val type: String,
    val rawDate: Long,
    val count: Int = 1,

)
