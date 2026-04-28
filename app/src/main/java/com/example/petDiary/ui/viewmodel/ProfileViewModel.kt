package com.example.petDiary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petDiary.data.PhotoRepository
import com.example.petDiary.network.RetrofitClient
import com.example.petDiary.network.TokenManager
import com.example.petDiary.network.models.PetProfileDto
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.apiService
    private val photoRepository = PhotoRepository(application)

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
                    var profile = response.body() ?: PetProfileDto()

                    // Если фото сохранено как локальный путь, проверяем существует ли файл
                    profile.photoPath?.let { photoPath ->
                        if (!photoPath.startsWith("https://")) {
                            val localPath = photoRepository.getPhotoPath(photoPath)
                            if (localPath == null) {
                                profile = profile.copy(photoPath = null)
                            }
                        }
                    }

                    _petProfile.value = profile
                    _hasSavedData.value = true
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

                // Если есть новое фото - загружаем на Яндекс.Диск
                if (photoLocalPath != null) {
                    // Определяем, нужно ли загружать в облако
                    val tokenManager = TokenManager(getApplication())
                    val isAuthorized = tokenManager.getToken() != null

                    val photoUrl = photoRepository.savePhoto(photoLocalPath, isAuthorized)
                    finalProfile = profile.copy(photoPath = photoUrl)
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