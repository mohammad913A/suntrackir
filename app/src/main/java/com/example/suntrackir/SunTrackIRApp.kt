package com.example.suntrackir

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class SunTrackIRApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}