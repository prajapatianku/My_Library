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
    val SUPABASE_ANON_KEY = com.example.BuildConfig.SUPABASE_PUBLISHABLE_KEY.ifBlank { com.example.BuildConfig.SUPABASE_SECRET_KEY }
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

    suspend fun fetchAccount(queryKey: String): String? = withContext(Dispatchers.IO) {
        val trimmed = queryKey.trim()
        val phoneKey = trimmed.replace("+", "").replace(" ", "").replace("-", "")
        val emailKey = trimmed.lowercase()

        try {
            // First search directly by account ID
            val url1 = "${SupabaseConfig.SUPABASE_URL}/rest/v1/library_accounts?id=eq.$trimmed&select=data"
            val req1 = Request.Builder()
                .url(url1)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .get()
                .build()
            val resp1 = client.newCall(req1).execute()
            if (resp1.isSuccessful) {
                val bodyStr = resp1.body?.string()
                if (!bodyStr.isNullOrBlank()) {
                    val arr = JSONArray(bodyStr)
                    if (arr.length() > 0) return@withContext arr.getJSONObject(0).optString("data")
                }
            }

            // Fallback: Fetch library_accounts and match phone or email inside JSON
            val urlAll = "${SupabaseConfig.SUPABASE_URL}/rest/v1/library_accounts?select=data"
            val reqAll = Request.Builder()
                .url(urlAll)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                .get()
                .build()
            val respAll = client.newCall(reqAll).execute()
            if (respAll.isSuccessful) {
                val bodyStr = respAll.body?.string()
                if (!bodyStr.isNullOrBlank()) {
                    val arr = JSONArray(bodyStr)
                    for (i in 0 until arr.length()) {
                        val dStr = arr.getJSONObject(i).optString("data")
                        if (dStr.isNotBlank()) {
                            try {
                                val dObj = JSONObject(dStr)
                                val owner = dObj.optJSONObject("ownerProfile")
                                if (owner != null) {
                                    val p = owner.optString("phone").replace("+", "").replace(" ", "").replace("-", "")
                                    val e = owner.optString("email").trim().lowercase()
                                    if ((phoneKey.isNotBlank() && p == phoneKey) || (emailKey.isNotBlank() && e == emailKey)) {
                                        return@withContext dStr
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
            null
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
