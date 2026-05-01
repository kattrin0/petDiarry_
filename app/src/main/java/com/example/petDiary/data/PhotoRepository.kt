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
    private val tokenManager = TokenManager(context)

    companion object {
        private const val PHOTO_DIR = "pet_photos"
    }

    suspend fun savePhoto(localPath: String, saveToCloud: Boolean = true): String? =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "savePhoto: $localPath, saveToCloud: $saveToCloud")

                val sourceFile = File(localPath)
                if (!sourceFile.exists()) {
                    Log.e(TAG, "Файл не существует: $localPath")
                    return@withContext null
                }

                val localFileName = saveLocalCopy(localPath)
                Log.d(TAG, "Локальная копия: $localFileName")
                
                if (saveToCloud && !tokenManager.isGuestMode()) {
                    Log.d(TAG, "Загрузка на Яндекс.Диск...")
                    val cloudUrl = yandexDiskService.uploadPhoto(localPath)

                    if (cloudUrl != null) {
                        Log.d(TAG, "Загружено на Яндекс.Диск: $cloudUrl")
                        return@withContext cloudUrl
                    } else {
                        Log.e(TAG, " Ошибка загрузки на Яндекс.Диск")
                    }
                }

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

    suspend fun getPhotoPath(photoIdentifier: String): String? =
        withContext(Dispatchers.IO) {
            try {
                if (photoIdentifier.startsWith("https://")) {
                    Log.d(TAG, "URL с Яндекс.Диска")
                    return@withContext photoIdentifier
                }

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