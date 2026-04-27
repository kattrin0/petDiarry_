package com.example.petDiary.network

import com.example.petDiary.network.models.*
import retrofit2.http.*
import retrofit2.Response

interface ApiService {

    // ========== AUTH ==========
    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/auth/guest")
    suspend fun guestLogin(): Response<AuthResponse>

    // ========== EVENTS ==========
    @GET("api/events")
    suspend fun getAllEvents(): Response<List<EventDto>>

    @GET("api/events/active")
    suspend fun getActiveEvents(): Response<List<EventDto>>

    @GET("api/events/today")
    suspend fun getTodayEvents(): Response<List<EventDto>>

    @POST("api/events")
    suspend fun addEvent(@Body event: EventDto): Response<EventDto>

    @PUT("api/events/{id}")
    suspend fun updateEvent(@Path("id") id: Long, @Body event: EventDto): Response<EventDto>

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(@Path("id") id: Long): Response<Void>

    @PATCH("api/events/{id}/toggle")
    suspend fun toggleComplete(@Path("id") id: Long): Response<EventDto>

    @DELETE("api/events/past")
    suspend fun removePastEvents(): Response<Void>

    // ========== PROFILE ==========
    @GET("api/profile")
    suspend fun getProfile(): Response<PetProfileDto>

    @POST("api/profile")
    suspend fun saveProfile(@Body profile: PetProfileDto): Response<PetProfileDto>

    @GET("api/profile/exists")
    suspend fun hasProfile(): Response<Boolean>
}