package com.example.petDiary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petDiary.data.network.RetrofitClient
import com.example.petDiary.data.network.TokenManager
import com.example.petDiary.data.models.Event
import com.example.petDiary.notification.NotificationService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)
    private val prefs = application.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    private val gson = Gson()
    private val notificationService = NotificationService(application)

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

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

    private fun saveEventsToLocal(events: List<Event>) {
        val json = gson.toJson(events)
        prefs.edit().putString("guest_events", json).apply()
    }

    private fun loadEventsFromLocal(): List<Event> {
        val json = prefs.getString("guest_events", null)
        if (json == null) return emptyList()
        val type = object : TypeToken<List<Event>>() {}.type
        return gson.fromJson(json, type)
    }

    fun refreshEvents() {
        if (tokenManager.isGuestMode()) {
            val loaded = loadEventsFromLocal()
            _events.value = loaded
            notificationService.syncNotificationsForEvents(loaded)
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = api.getActiveEvents()
                    if (response.isSuccessful) {
                        val list = response.body() ?: emptyList()
                        _events.value = list
                        notificationService.syncNotificationsForEvents(list)
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
    }

    fun addEvent(event: Event) {
        if (tokenManager.isGuestMode()) {
            val currentEvents = _events.value?.toMutableList() ?: mutableListOf()
            val newEvent = event.copy(id = System.currentTimeMillis())
            currentEvents.add(newEvent)
            _events.value = currentEvents
            saveEventsToLocal(currentEvents)

            // Планируем уведомление
            notificationService.scheduleNotification(newEvent)

            _error.value = "Событие добавлено!"
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = api.addEvent(event)
                    if (response.isSuccessful) {
                        val savedEvent = response.body()!!
                        refreshEvents()

                        // Планируем уведомление
                        notificationService.scheduleNotification(savedEvent)

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
    }

    fun updateEvent(event: Event) {
        if (tokenManager.isGuestMode()) {
            val currentEvents = _events.value?.toMutableList() ?: mutableListOf()
            val index = currentEvents.indexOfFirst { it.id == event.id }
            if (index != -1) {
                currentEvents[index] = event
                _events.value = currentEvents
                saveEventsToLocal(currentEvents)
                if (!event.completed) notificationService.scheduleNotification(event)
                else notificationService.cancelNotification(event)
            }
        } else {
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
    }

    fun deleteEvent(event: Event) {
        if (tokenManager.isGuestMode()) {
            // Отменяем уведомление
            notificationService.cancelNotification(event)

            val currentEvents = _events.value?.toMutableList() ?: mutableListOf()
            currentEvents.removeAll { it.id == event.id }
            _events.value = currentEvents
            saveEventsToLocal(currentEvents)
            _error.value = "Событие удалено!"
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = api.deleteEvent(event.id!!)
                    if (response.isSuccessful) {
                        // Отменяем уведомление
                        notificationService.cancelNotification(event)

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
    }

    fun toggleEventComplete(event: Event) {
        val updatedEvent = event.copy(completed = !event.completed)

        if (tokenManager.isGuestMode()) {
            val currentEvents = _events.value?.toMutableList() ?: mutableListOf()
            val index = currentEvents.indexOfFirst { it.id == event.id }
            if (index != -1) {
                currentEvents[index] = updatedEvent
                _events.value = currentEvents
                saveEventsToLocal(currentEvents)
            }
            if (updatedEvent.completed) notificationService.cancelNotification(updatedEvent)
            else notificationService.scheduleNotification(updatedEvent)
        } else {
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
    }

    fun clearError() {
        _error.value = null
    }
}