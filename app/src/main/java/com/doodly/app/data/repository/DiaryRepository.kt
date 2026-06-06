package com.doodly.app.data.repository

import com.doodly.app.data.local.DiaryDao
import com.doodly.app.data.local.DiaryEntry
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val diaryDao: DiaryDao) {
    val allEntries: Flow<List<DiaryEntry>> = diaryDao.getAll()

    fun getMonth(start: Long, end: Long): Flow<List<DiaryEntry>> =
        diaryDao.getMonth(start, end)

    suspend fun findById(id: Int): DiaryEntry? = diaryDao.findById(id)

    suspend fun findByDate(date: Long): DiaryEntry? = diaryDao.findByDate(date)

    suspend fun insert(entry: DiaryEntry): Int = diaryDao.insert(entry).toInt()

    suspend fun update(entry: DiaryEntry) = diaryDao.update(entry)

    suspend fun delete(entry: DiaryEntry) = diaryDao.delete(entry)
}
