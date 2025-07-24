package com.example.caller_id.service

import com.example.caller_id.database.entity.DoNotDisturbNumber

interface DndUpdateListener {
    fun onDndUpdated(data: DoNotDisturbNumber)
}
