package com.doodly.app

import android.app.Application
import com.doodly.app.di.AppContainer
import com.doodly.app.notification.createNotificationChannel

class DoodlyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel(this)
    }
}
