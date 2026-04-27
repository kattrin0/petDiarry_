package com.example.petDiary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petDiary.network.RetrofitClient
import com.example.petDiary.network.models.EventDto
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.apiService

    private val _events = MutableLiveData<List<EventDto>>()
    val events: LiveData<List<EventDto>> = _events

    private val _todayDate = MutableLiveData<String>()
    val todayDate: LiveData<String> = _todayDate

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadTodayDate()
        refreshEvents()
    }

    private fun loadTodayDate() {
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        val today = dateFormat.format(Date())
        _todayDate.value = "Сегодня: $today"
    }

    fun refreshEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getActiveEvents()
                if (response.isSuccessful) {
                    _events.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Ошибка загрузки: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addEvent(event: EventDto) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.addEvent(event)
                if (response.isSuccessful) {
                    refreshEvents()
                    _error.value = "Событие добавлено!"
                } else {
                    _error.value = response.errorBody()?.string() ?: "Ошибка добавления"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEvent(event: EventDto) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.updateEvent(event.id!!, event)
                if (response.isSuccessful) {
                    refreshEvents()
                } else {
                    _error.value = response.errorBody()?.string() ?: "Ошибка обновления"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteEvent(event: EventDto) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.deleteEvent(event.id!!)
                if (response.isSuccessful) {
                    refreshEvents()
                    _error.value = "Событие удалено!"
                } else {
                    _error.value = response.errorBody()?.string() ?: "Ошибка удаления"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleEventComplete(event: EventDto) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.toggleComplete(event.id!!)
                if (response.isSuccessful) {
                    refreshEvents()
                } else {
                    _error.value = response.errorBody()?.string() ?: "Ошибка"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}