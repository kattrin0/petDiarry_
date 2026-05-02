package com.example.petDiary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petDiary.data.network.RetrofitClient
import com.example.petDiary.data.network.TokenManager
import com.example.petDiary.data.models.AuthRequest
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isAuthenticated = MutableLiveData<Boolean>()
    val isAuthenticated: LiveData<Boolean> = _isAuthenticated

    private val _isGuest = MutableLiveData<Boolean>()
    val isGuest: LiveData<Boolean> = _isGuest

    private val _onSignOut = MutableLiveData<Boolean>()
    val onSignOut: LiveData<Boolean> = _onSignOut

    init {
        val token = tokenManager.getToken()
        if (token != null && !tokenManager.isGuestMode()) {
            _isAuthenticated.value = true
        } else if (tokenManager.isGuestMode()) {
            _isGuest.value = true
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
                    tokenManager.setGuestMode(false)
                    _isAuthenticated.value = true
                    _isGuest.value = false
                    _error.value = "Регистрация успешна!"
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = when {
                        errorBody?.contains("already exists") == true -> "Пользователь с таким email уже существует"
                        else -> "Ошибка регистрации"
                    }
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
                    tokenManager.setGuestMode(false)
                    _isAuthenticated.value = true
                    _isGuest.value = false
                    _error.value = "Вход выполнен!"
                } else {
                    _error.value = "Неверный email или пароль"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInAsGuest() {
        val guestId = "guest_${System.currentTimeMillis()}"

        tokenManager.saveToken(guestId)
        tokenManager.saveUserId(-1L)
        tokenManager.setGuestMode(true)

        _isGuest.value = true
        _isAuthenticated.value = false
        _error.value = "Гостевой режим"
    }

    fun signOut() {
        tokenManager.clear()
        _isAuthenticated.value = false
        _isGuest.value = false
        _onSignOut.value = true
        _error.value = "Вы вышли из аккаунта"
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSignOut() {
        _onSignOut.value = false
    }
}