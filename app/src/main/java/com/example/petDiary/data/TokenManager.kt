package com.example.petDiary.data

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
    }

    // Сохранить токен
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    // Получить токен
    fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    // Сохранить ID пользователя
    fun saveUserId(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    // Получить ID пользователя
    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    // Очистить все данные (при выходе)
    fun clear() {
        prefs.edit().clear().apply()
    }

    // Проверить, авторизован ли пользователь
    fun isLoggedIn(): Boolean {
        return getToken() != null && getUserId() != -1L
    }
}