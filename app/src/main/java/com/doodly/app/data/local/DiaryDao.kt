package com.doodly.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntry): Long

    @Update
    suspend fun update(entry: DiaryEntry)

    @Delete
    suspend fun delete(entry: DiaryEntry)

    @Query("SELECT * FROM diary ORDER BY date DESC")
    fun getAll(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary WHERE id = :id")
    suspend fun findById(id: Int): DiaryEntry?

    @Query("SELECT * FROM diary WHERE date = :date LIMIT 1")
    suspend fun findByDate(date: Long): DiaryEntry?

    @Query("SELECT * FROM diary WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun getMonth(start: Long, end: Long): Flow<List<DiaryEntry>>
}
