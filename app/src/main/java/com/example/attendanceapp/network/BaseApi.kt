package com.example.attendanceapp.network

import com.example.attendanceapp.model.AttendanceResponse
import com.example.attendanceapp.model.BasicResponse
import com.example.attendanceapp.model.LoginResponse
import com.example.attendanceapp.model.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface BaseApi {

    //Login
    @POST("auth/login")
    suspend fun login(@Body body : HashMap<String, String>): Response<BasicResponse<List<LoginResponse>>>

    //User
    @GET("user")
    suspend fun getUser(): Response<BasicResponse<List<UserResponse>>>

    //Attendance
    @Multipart
    @Headers("Accept: application/x-www-form-urlencoded")
    @POST("attendance")
    suspend fun postAttendance(
        @Part("time") time: RequestBody,
        @Part("status") status: RequestBody,
        @Part("jobdesk") jobdesk: RequestBody,
        @Part img1 : MultipartBody.Part
    ): Response<BasicResponse<List<AttendanceResponse>>>
}