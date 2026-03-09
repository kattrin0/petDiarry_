// /petDiary/app/src/main/java/com/example/petDiary/data/repository/PhotoRepository.kt
package com.example.petDiary.data.repository

import android.content.Context
import android.util.Log
import com.example.petDiary.data.service.YandexDiskService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PhotoRepository(private val context: Context) {
    private val TAG = "PhotoRepository"
    private val yandexDiskService = YandexDiskService(context)

    companion object {
        private const val PHOTO_DIR = "pet_photos"
    }

    /**
     * Сохранение фото: локально + загрузка на Яндекс.Диск для авторизованных
     * Возвращает URL для Firebase (Яндекс.Диск) или локальный путь
     */
    suspend fun savePhoto(localPath: String, isAuthorized: Boolean): String? =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "savePhoto: $localPath, isAuthorized: $isAuthorized")

                val sourceFile = File(localPath)
                if (!sourceFile.exists()) {
                    Log.e(TAG, "Файл не существует: $localPath")
                    return@withContext null
                }

                // Всегда сохраняем локальную копию
                val localFileName = saveLocalCopy(localPath)
                Log.d(TAG, "Локальная копия: $localFileName")

                // Для авторизованных пользователей загружаем в облако
                if (isAuthorized) {
                    Log.d(TAG, "Загрузка на Яндекс.Диск...")
                    val cloudUrl = yandexDiskService.uploadPhoto(localPath)

                    if (cloudUrl != null) {
                        Log.d(TAG, "✓ Загружено на Яндекс.Диск: $cloudUrl")
                        // Возвращаем URL для сохранения в Firebase
                        return@withContext cloudUrl
                    } else {
                        Log.e(TAG, "✗ Ошибка загрузки на Яндекс.Диск")
                    }
                }

                // Для неавторизованных или при ошибке - возвращаем локальный путь
                return@withContext localFileName
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка в savePhoto", e)
                null
            }
        }

    private fun saveLocalCopy(sourcePath: String): String {
        val photoDir = File(context.filesDir, PHOTO_DIR)
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }
        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        val destination = File(photoDir, fileName)
        File(sourcePath).copyTo(destination, overwrite = true)
        return destination.absolutePath
    }

    /**
     * Получение пути к фото (URL или локальный файл)
     */
    suspend fun getPhotoPath(photoIdentifier: String): String? =
        withContext(Dispatchers.IO) {
            try {
                // Если это URL с Яндекс.Диска - возвращаем как есть для Glide
                if (photoIdentifier.startsWith("https://")) {
                    Log.d(TAG, "Это URL с Яндекс.Диска")
                    return@withContext photoIdentifier
                }

                // Если это локальный файл
                val localFile = File(photoIdentifier)
                if (localFile.exists()) {
                    return@withContext localFile.absolutePath
                }

                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка получения фото", e)
                null
            }
        }
}