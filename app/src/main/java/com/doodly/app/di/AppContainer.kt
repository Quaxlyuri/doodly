package com.doodly.app.di

import android.content.Context
import com.doodly.app.data.local.AppDatabase
import com.doodly.app.data.remote.AiImageService
import com.doodly.app.data.repository.AiRepository
import com.doodly.app.data.repository.BackupRepository
import com.doodly.app.data.repository.DiaryRepository
import com.doodly.app.data.repository.NoOpBackupRepository
import com.doodly.app.data.repository.SettingsRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getDatabase(appContext)

    val diaryRepository = DiaryRepository(database.diaryDao())
    val settingsRepository = SettingsRepository(appContext)
    val backupRepository: BackupRepository = NoOpBackupRepository()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    private val imageService = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AiImageService::class.java)

    val aiRepository = AiRepository(appContext, imageService)
}
