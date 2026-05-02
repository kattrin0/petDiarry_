package com.example.petDiary.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class YandexDiskService(private val context: Context) {

    private val TAG = "YandexDiskService"

    companion object {
        private val OAUTH_TOKEN = "y0__xDR2YD2Axi11T4g9vWB1RYwpfzn6QfhNewQKFNv49Q_7oKf2YNNhIysdQ"
        private const val APP_FOLDER = "petdiary_photos"
        private const val BASE_URL = "https://cloud-api.yandex.net/v1/disk"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()


    // Загрузка фото на Яндекс.Диск
    suspend fun uploadPhoto(localPath: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val file = File(localPath)
                if (!file.exists()) {
                    Log.e(TAG, "Файл не найден: $localPath")
                    return@withContext null
                }

                createFolder()

                val fileName = "${System.currentTimeMillis()}.jpg"
                val remotePath = "$APP_FOLDER/$fileName"

                val uploadLink = getUploadLink(remotePath)
                if (uploadLink == null) {
                    Log.e(TAG, "Не удалось получить ссылку для загрузки")
                    return@withContext null
                }

                val uploadSuccess = uploadFile(uploadLink, file)
                if (!uploadSuccess) {
                    Log.e(TAG, "Ошибка загрузки файла")
                    return@withContext null
                }

                val publicUrl = publishFile(remotePath)
                if (publicUrl != null) {
                    return@withContext publicUrl
                } else {
                    Log.e(TAG, "Не удалось получить публичную ссылку")
                    return@withContext null
                }

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка загрузки", e)
                null
            }
        }

     //Создание папки на Яндекс.Диске
    private suspend fun createFolder() {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/resources?path=$APP_FOLDER")
                .put(RequestBody.Companion.create(null, ""))
                .addHeader("Authorization", "OAuth $OAUTH_TOKEN")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful && response.code != 409) {
                Log.e(TAG, "Ошибка создания папки: ${response.code}")
            }
            response.close()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка создания папки", e)
        }
    }


     //Получение ссылки для загрузки файла
    private suspend fun getUploadLink(remotePath: String): String? {
        return try {
            val request = Request.Builder()
                .url("$BASE_URL/resources/upload?path=$remotePath&overwrite=true")
                .get()
                .addHeader("Authorization", "OAuth $OAUTH_TOKEN")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Ошибка получения ссылки: ${response.code}")
                    return@use null
                }

                val json = JSONObject(response.body?.string() ?: return@use null)
                json.getString("href")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения ссылки", e)
            null
        }
    }


     // Загрузка файла по полученной ссылке
    private suspend fun uploadFile(uploadLink: String, file: File): Boolean {
        return try {
            val request = Request.Builder()
                .url(uploadLink)
                .put(file.asRequestBody("image/jpeg".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Ошибка загрузки файла: ${response.code}")
                    false
                } else {
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки файла", e)
            false
        }
    }


     // Публикация файла и получение публичной ссылки

    private suspend fun publishFile(remotePath: String): String? {
        return try {
            // Публикуем файл
            val publishRequest = Request.Builder()
                .url("$BASE_URL/resources/publish?path=$remotePath")
                .put(RequestBody.Companion.create(null, ""))
                .addHeader("Authorization", "OAuth $OAUTH_TOKEN")
                .build()

            client.newCall(publishRequest).execute().close()

            // Ждем немного для обработки
            Thread.sleep(500)

            val infoRequest = Request.Builder()
                .url("$BASE_URL/resources?path=$remotePath")
                .get()
                .addHeader("Authorization", "OAuth $OAUTH_TOKEN")
                .build()

            client.newCall(infoRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Ошибка получения информации: ${response.code}")
                    return@use null
                }

                val json = JSONObject(response.body?.string() ?: return@use null)
                val publicUrl = json.optString("public_url")
                if (publicUrl.isNullOrEmpty()) {
                    Log.e(TAG, "Публичная ссылка не найдена")
                    null
                } else {
                    publicUrl
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка публикации", e)
            null
        }
    }

}