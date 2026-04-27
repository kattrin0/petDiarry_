package com.example.petDiary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petDiary.network.RetrofitClient
import com.example.petDiary.network.TokenManager
import com.example.petDiary.network.models.AuthRequest
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.apiService
    private val tokenManager = com.example.petDiary.network.TokenManager(application)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isAuthenticated = MutableLiveData<Boolean>()
    val isAuthenticated: LiveData<Boolean> = _isAuthenticated

    private val _isGuest = MutableLiveData<Boolean>()
    val isGuest: LiveData<Boolean> = _isGuest

    init {
        // Проверяем, есть ли сохранённый токен
        val token = tokenManager.getToken()
        if (token != null) {
            _isAuthenticated.value = true
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = AuthRequest(email, password, false)
                val response = api.register(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()!!
                    tokenManager.saveToken(authResponse.token)
                    tokenManager.saveUserId(authResponse.userId)
                    _isAuthenticated.value = true
                    _isGuest.value = false
                    _error.value = "Регистрация успешна!"
                } else {
                    _error.value = response.errorBody()?.string() ?: "Ошибка регистрации"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = AuthRequest(email, password, false)
                val response = api.login(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()!!
                    tokenManager.saveToken(authResponse.token)
                    tokenManager.saveUserId(authResponse.userId)
                    _isAuthenticated.value = true
                    _isGuest.value = false
                    _error.value = "Вход выполнен!"
                } else {
                    _error.value = response.errorBody()?.string() ?: "Ошибка входа"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.guestLogin()

                if (response.isSuccessful) {
                    val authResponse = response.body()!!
                    tokenManager.saveToken(authResponse.token)
                    tokenManager.saveUserId(authResponse.userId)
                    _isGuest.value = true
                    _isAuthenticated.value = false
                    _error.value = "Гостевой режим"
                } else {
                    _error.value = "Ошибка входа как гость"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        tokenManager.clear()
        _isAuthenticated.value = false
        _isGuest.value = false
        _error.value = "Вы вышли из аккаунта"
    }

    fun clearError() {
        _error.value = null
    }
}