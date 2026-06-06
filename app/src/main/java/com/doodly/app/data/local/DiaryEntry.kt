package com.doodly.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diary",
    indices = [Index(value = ["date"], unique = true)]
)
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "mood") val mood: String,
    @ColumnInfo(name = "image_path") val imagePath: String?,
    @ColumnInfo(name = "tags") val tags: List<String> = emptyList(),
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
