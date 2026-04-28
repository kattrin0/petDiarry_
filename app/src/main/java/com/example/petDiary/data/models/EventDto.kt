package com.example.petDiary.data.models

data class EventDto(
    val id: Long? = null,
    val title: String,
    val description: String? = "",
    val date: String,
    val time: String? = "",
    val dateMillis: Long,
    val timeHour: Int = 0,
    val timeMinute: Int = 0,
    val completed: Boolean = false
)