package com.example.data.supabase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object SupabaseConfig {
    val SUPABASE_URL = com.example.BuildConfig.SUPABASE_URL
    val SUPABASE_ANON_KEY = com.example.BuildConfig.SUPABASE_SECRET_KEY
    val SUPABASE_PUBLISHABLE_KEY = com.example.BuildConfig.SUPABASE_PUBLISHABLE_KEY
    val SUPABASE_SECRET_KEY = com.example.BuildConfig.SUPABASE_SECRET_KEY
    val SUPABASE_JWKS_URL = com.example.BuildConfig.SUPABASE_JWKS_URL
}

class SupabaseApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun upsertAccount(accountId: String, accountJson: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/library_accounts"
            val payload = JSONObject().apply {
                put("id", accountId)
                put("data", accountJson)
                put("updated_at", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            }.toString()
            val body = payload.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                true
            } else {
                // Fallback PUT request targeting this specific ID
                val putUrl = "${SupabaseConfig.SUPABASE_URL}/rest/v1/library_accounts?id=eq.$accountId"
                val putRequest = Request.Builder()
                    .url(putUrl)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .put(body)
                    .build()
                val putResponse = client.newCall(putRequest).execute()
                putResponse.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiClient", "Error upserting account: ${e.message}")
            false
        }
    }

    suspend fun fetchAccount(accountId: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/library_accounts?id=eq.$accountId&select=data"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string()
                if (!bodyStr.isNullOrBlank()) {
                    val jsonArray = JSONArray(bodyStr)
                    if (jsonArray.length() > 0) {
                        jsonArray.getJSONObject(0).optString("data")
                    } else null
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiClient", "Error fetching account: ${e.message}")
            null
        }
    }

    suspend fun getTable(tableName: String, queryParams: String = ""): String? = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/$tableName?$queryParams"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                Log.w("SupabaseApiClient", "GET $tableName returned code ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiClient", "Error fetching $tableName: ${e.message}")
            null
        }
    }

    suspend fun insertRecord(tableName: String, jsonBody: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/$tableName"
            val body = jsonBody.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseApiClient", "Error inserting into $tableName: ${e.message}")
            false
        }
    }

    suspend fun deleteAccount(accountId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/library_accounts?id=eq.$accountId"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .delete()
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseApiClient", "Error deleting account $accountId: ${e.message}")
            false
        }
    }

    suspend fun updateRecord(tableName: String, filterColumn: String, filterValue: String, jsonBody: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/$tableName?$filterColumn=eq.$filterValue"
            val body = jsonBody.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .patch(body)
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseApiClient", "Error updating $tableName: ${e.message}")
            false
        }
    }

    suspend fun deleteRecord(tableName: String, filterColumn: String, filterValue: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.SUPABASE_URL}/rest/v1/$tableName?$filterColumn=eq.$filterValue"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .delete()
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseApiClient", "Error deleting from $tableName: ${e.message}")
            false
        }
    }
}
