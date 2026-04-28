package com.example.petDiary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petDiary.network.RetrofitClient
import com.example.petDiary.network.models.EventDto
import com.example.petDiary.network.models.PetProfileDto
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.apiService

    private val _petProfile = MutableLiveData<PetProfileDto>()
    val petProfile: LiveData<PetProfileDto> = _petProfile

    private val _todayEvents = MutableLiveData<List<EventDto>>()
    val todayEvents: LiveData<List<EventDto>> = _todayEvents

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadPetProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getProfile()
                if (response.isSuccessful) {
                    _petProfile.value = response.body() ?: PetProfileDto()
                } else {
                    _petProfile.value = PetProfileDto()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _petProfile.value = PetProfileDto()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTodayEvents() {
        viewModelScope.launch {
            try {
                val response = api.getTodayEvents()
                if (response.isSuccessful) {
                    val events = response.body() ?: emptyList()
                    // Сортируем по времени
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

    fun refreshData() {
        loadPetProfile()
        loadTodayEvents()
    }
}