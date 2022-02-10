package com.example.attendanceapp.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val basicRequest = request.newBuilder()
            .addHeader("X-Authorization", "$token")
            .build()
        return chain.proceed(basicRequest)
    }
}