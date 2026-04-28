package com.example.petDiary.data.models

data class AuthRequest(
    val email: String,
    val password: String,
    val guest: Boolean = false
)