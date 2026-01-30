package com.azzan.app

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles the HTTP POST request to FCM Legacy API.
 */
object AzaanBroadcaster {

    private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
    private const val SERVER_KEY = "YOUR_FCM_SERVER_KEY_HERE"

    private val client = OkHttpClient()

    suspend fun sendAzaanBroadcast(): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("to", "/topics/masjid_azaan")
                put("priority", "high")
                put("notification", JSONObject().apply {
                    put("title", "Azaan Alert")
                    put("body", "The Azaan is being broadcasted from Al-Huda Islamic Center.")
                    put("sound", "default")
                })
                put("data", JSONObject().apply {
                    put("type", "azaan_start")
                    put("masjid_id", "alhuda_01")
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(FCM_URL)
                .addHeader("Authorization", "key=$SERVER_KEY")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}