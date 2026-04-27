package com.example.petDiary.network.models

data class AuthRequest(
    val email: String,
    val password: String,
    val guest: Boolean = false
)