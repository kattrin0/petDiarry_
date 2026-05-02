package com.example.petDiary.data.models

data class PetProfile(
    val name: String = "",
    val breed: String = "",
    val gender: String = "",
    val birthDate: String = "",
    val weight: Double = 0.0,
    val color: String = "",
    val chipNumber: String = "",
    val sterilized: Boolean = false,
    val notes: String = "",
    val photoPath: String? = null
)