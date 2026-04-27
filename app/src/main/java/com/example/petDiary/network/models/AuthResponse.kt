package com.example.petDiary.network.models

data class AuthResponse(
    val token: String,
    val userId: Long,
    val email: String,
    val guest: Boolean
)