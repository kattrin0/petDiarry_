package com.example.petDiary.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableLiveData<Int>()
    val themeMode: LiveData<Int> = _themeMode

    init {
        loadTheme()
    }

    private fun loadTheme() {
        val mode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        _themeMode.value = mode
    }

    fun getThemeMode(): Int {
        return prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun saveThemeMode(mode: Int) {
        prefs.edit().putInt("theme_mode", mode).apply()
        _themeMode.value = mode
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun getThemeModes(): Array<Int> {
        return arrayOf(
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
    }
}