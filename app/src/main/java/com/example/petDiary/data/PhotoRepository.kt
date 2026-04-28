package com.example.petDiary.data

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
     * Сохранение фото: локально + загрузка на Яндекс.Диск
     * Возвращает URL для сохранения в БД (через ProfileViewModel -> API)
     */
    suspend fun savePhoto(localPath: String, saveToCloud: Boolean = true): String? =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "savePhoto: $localPath, saveToCloud: $saveToCloud")

                val sourceFile = File(localPath)
                if (!sourceFile.exists()) {
                    Log.e(TAG, "Файл не существует: $localPath")
                    return@withContext null
                }

                // Всегда сохраняем локальную копию
                val localFileName = saveLocalCopy(localPath)
                Log.d(TAG, "Локальная копия: $localFileName")

                // Для авторизованных пользователей загружаем в облако
                if (saveToCloud) {
                    Log.d(TAG, "Загрузка на Яндекс.Диск...")
                    val cloudUrl = yandexDiskService.uploadPhoto(localPath)

                    if (cloudUrl != null) {
                        Log.d(TAG, "✓ Загружено на Яндекс.Диск: $cloudUrl")
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
                // Если это URL с Яндекс.Диска
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