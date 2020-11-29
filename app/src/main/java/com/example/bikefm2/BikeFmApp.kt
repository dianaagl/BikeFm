package com.example.bikefm2

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BikeFmApp : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: BikeFmApp? = null

        fun applicationContext() : BikeFmApp {
            return instance as BikeFmApp
        }
    }
    override fun onCreate() {
        super.onCreate()
    }
}