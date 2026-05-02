package com.example.petDiary.data.network

import com.example.petDiary.data.models.AuthRequest
import com.example.petDiary.data.models.AuthResponse
import com.example.petDiary.data.models.Event
import com.example.petDiary.data.models.PetProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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
    suspend fun getAllEvents(): Response<List<Event>>

    @GET("api/events/active")
    suspend fun getActiveEvents(): Response<List<Event>>

    @GET("api/events/today")
    suspend fun getTodayEvents(): Response<List<Event>>

    @POST("api/events")
    suspend fun addEvent(@Body event: Event): Response<Event>

    @PUT("api/events/{id}")
    suspend fun updateEvent(@Path("id") id: Long, @Body event: Event): Response<Event>

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(@Path("id") id: Long): Response<Void>

    @PATCH("api/events/{id}/toggle")
    suspend fun toggleComplete(@Path("id") id: Long): Response<Event>

    @DELETE("api/events/past")
    suspend fun removePastEvents(): Response<Void>

    // ========== PROFILE ==========
    @GET("api/profile")
    suspend fun getProfile(): Response<PetProfile>

    @POST("api/profile")
    suspend fun saveProfile(@Body profile: PetProfile): Response<PetProfile>

    @GET("api/profile/exists")
    suspend fun hasProfile(): Response<Boolean>
}