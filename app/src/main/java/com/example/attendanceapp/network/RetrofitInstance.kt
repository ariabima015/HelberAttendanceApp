package com.example.attendanceapp.network


import android.app.Application
import android.content.Context
import com.example.attendanceapp.AttendanceApp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder

import com.google.gson.Gson





class RetrofitInstance {

    val BASE_URL = "https://helber.id/api/"
    val PREF_KEY = "com.example.attendanceapp.PREFERENCE_KEY"

    fun getToken() : String {
        val app: Application = AttendanceApp.instance
        val mPrefs = app.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)

        return mPrefs.getString("token", "").toString()
    }

    fun createApi(): BaseApi {
        val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(getToken()))
            .addInterceptor(logger)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttp)
            .build()

        return retrofit.create(BaseApi::class.java)
    }
}
