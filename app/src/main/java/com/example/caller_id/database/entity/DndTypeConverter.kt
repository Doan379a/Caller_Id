package com.example.caller_id.database.entity

import androidx.room.TypeConverter
import com.example.caller_id.model.DndType

class DndTypeConverter {
    @TypeConverter
    fun fromType(type: DndType): String = type.name

    @TypeConverter
    fun toType(value: String): DndType = DndType.valueOf(value)
}