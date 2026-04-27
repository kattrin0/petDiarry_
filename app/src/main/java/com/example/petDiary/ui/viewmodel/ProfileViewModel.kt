package com.example.petDiary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petDiary.network.RetrofitClient
import com.example.petDiary.network.models.PetProfileDto
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.apiService

    private val _petProfile = MutableLiveData<PetProfileDto>()
    val petProfile: LiveData<PetProfileDto> = _petProfile

    private val _hasSavedData = MutableLiveData<Boolean>()
    val hasSavedData: LiveData<Boolean> = _hasSavedData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadPetProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getProfile()
                if (response.isSuccessful) {
                    val profile = response.body()
                    _petProfile.value = profile ?: PetProfileDto()
                    _hasSavedData.value = profile != null
                } else {
                    _petProfile.value = PetProfileDto()
                    _hasSavedData.value = false
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun savePetProfile(profile: PetProfileDto, photoLocalPath: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var finalProfile = profile

                if (photoLocalPath != null) {
                    // TODO: Загрузка фото на сервер
                    // finalProfile = profile.copy(photoPath = uploadedUrl)
                }

                val response = api.saveProfile(finalProfile)
                if (response.isSuccessful) {
                    _petProfile.value = response.body()
                    _hasSavedData.value = true
                    _error.value = "Профиль сохранён!"
                } else {
                    _error.value = response.errorBody()?.string() ?: "Ошибка сохранения"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshProfile() {
        loadPetProfile()
    }

    fun clearError() {
        _error.value = null
    }
}