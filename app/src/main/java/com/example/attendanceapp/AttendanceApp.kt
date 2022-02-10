package com.example.attendanceapp

import android.app.Application

class AttendanceApp: Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: AttendanceApp
            private set
    }
}