package com.example.attendanceapp.model

data class LoginResponse(
    val expired_in: Int,
    val name: String,
    val token: String,
    val token_type: String,
    val message : String
)