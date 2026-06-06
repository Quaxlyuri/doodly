package com.doodly.app.data.repository

import com.doodly.app.data.local.DiaryEntry

interface BackupRepository {
    suspend fun backup(entry: DiaryEntry): Result<Unit>
}

class NoOpBackupRepository : BackupRepository {
    override suspend fun backup(entry: DiaryEntry): Result<Unit> = Result.success(Unit)
}
