package com.example.attendanceapp.model

data class BasicResponse<T> (
    val data : T?,
    val success : Boolean
)