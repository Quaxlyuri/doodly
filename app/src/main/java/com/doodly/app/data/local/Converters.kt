package com.doodly.app.data.local

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromTags(tags: List<String>): String = JSONArray(tags).toString()

    @TypeConverter
    fun toTags(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        val array = JSONArray(value)
        return List(array.length()) { index -> array.optString(index) }
    }
}
