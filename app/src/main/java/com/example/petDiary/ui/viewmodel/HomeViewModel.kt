package com.example.petDiary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petDiary.data.network.RetrofitClient
import com.example.petDiary.data.network.TokenManager
import com.example.petDiary.data.models.Event
import com.example.petDiary.data.models.PetProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)
    private val prefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _petProfile = MutableLiveData<PetProfile>()
    val petProfile: LiveData<PetProfile> = _petProfile

    private val _todayEvents = MutableLiveData<List<Event>>()
    val todayEvents: LiveData<List<Event>> = _todayEvents

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private fun loadProfileFromLocal(): PetProfile? {
        val json = prefs.getString("guest_profile", null)
        if (json == null) return null
        return gson.fromJson(json, PetProfile::class.java)
    }

    private fun loadEventsFromLocal(): List<Event> {
        val json = prefs.getString("guest_events", null)
        if (json == null) return emptyList()
        val type = object : TypeToken<List<Event>>() {}.type
        return gson.fromJson(json, type)
    }

    fun loadPetProfile() {
        if (tokenManager.isGuestMode()) {
            val localProfile = loadProfileFromLocal()
            _petProfile.value = localProfile ?: PetProfile()
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = api.getProfile()
                    if (response.isSuccessful) {
                        _petProfile.value = response.body() ?: PetProfile()
                    } else {
                        _petProfile.value = PetProfile()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _petProfile.value = PetProfile()
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun loadTodayEvents() {
        if (tokenManager.isGuestMode()) {
            // В гостевом режиме берем из локального хранилища
            val events = loadEventsFromLocal()
            _todayEvents.value = events.sortedBy { it.timeHour * 60 + it.timeMinute }
        } else {
            viewModelScope.launch {
                try {
                    val response = api.getTodayEvents()
                    if (response.isSuccessful) {
                        val events = response.body() ?: emptyList()
                        _todayEvents.value = events.sortedBy { it.timeHour * 60 + it.timeMinute }
                    } else {
                        _todayEvents.value = emptyList()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _todayEvents.value = emptyList()
                }
            }
        }
    }

    fun refreshData() {
        loadPetProfile()
        loadTodayEvents()
    }
}