package com.example.petDiary.data.models

data class AuthResponse(
    val token: String,
    val userId: Long,
    val email: String,
    val guest: Boolean
)