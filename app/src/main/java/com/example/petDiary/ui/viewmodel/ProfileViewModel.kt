package com.example.petDiary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petDiary.data.PhotoRepository
import com.example.petDiary.data.network.RetrofitClient
import com.example.petDiary.data.network.TokenManager
import com.example.petDiary.data.models.PetProfile
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.apiService
    private val photoRepository = PhotoRepository(application)
    private val tokenManager = TokenManager(application)
    private val prefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _petProfile = MutableLiveData<PetProfile>()
    val petProfile: LiveData<PetProfile> = _petProfile

    private val _hasSavedData = MutableLiveData<Boolean>()
    val hasSavedData: LiveData<Boolean> = _hasSavedData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private fun saveProfileToLocal(profile: PetProfile) {
        val json = gson.toJson(profile)
        prefs.edit().putString("guest_profile", json).apply()
    }

    private fun loadProfileFromLocal(): PetProfile? {
        val json = prefs.getString("guest_profile", null)
        if (json == null) return null
        return gson.fromJson(json, PetProfile::class.java)
    }

    fun loadPetProfile() {
        if (tokenManager.isGuestMode()) {
            val localProfile = loadProfileFromLocal()
            if (localProfile != null) {
                _petProfile.value = localProfile
                _hasSavedData.value = true
            } else {
                _petProfile.value = PetProfile()
                _hasSavedData.value = false
            }
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = api.getProfile()
                    if (response.isSuccessful) {
                        var profile = response.body() ?: PetProfile()

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
                        _petProfile.value = PetProfile()
                        _hasSavedData.value = false
                    }
                } catch (e: Exception) {
                    _error.value = "Ошибка загрузки: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun savePetProfile(profile: PetProfile, photoLocalPath: String? = null) {
        if (tokenManager.isGuestMode()) {
            var finalProfile = profile

            if (photoLocalPath != null) {
                finalProfile = profile.copy(photoPath = photoLocalPath)
            }
            _petProfile.value = finalProfile
            saveProfileToLocal(finalProfile)
            _hasSavedData.value = true
            _error.value = "Профиль сохранён!"
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    var finalProfile = profile

                    if (photoLocalPath != null) {
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
    }

    fun refreshProfile() {
        loadPetProfile()
    }

    fun clearError() {
        _error.value = null
    }
}