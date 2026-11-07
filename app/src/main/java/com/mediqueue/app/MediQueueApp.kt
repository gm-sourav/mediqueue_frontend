package com.mediqueue.app

import android.app.Application
import com.mediqueue.app.data.ApiClient

class MediQueueApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}