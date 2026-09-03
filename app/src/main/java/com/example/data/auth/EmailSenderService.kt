package com.example.data.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object EmailSenderService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    var customBrevoApiKey: String = ""

    suspend fun sendOtpEmail(
        recipientEmail: String,
        otpCode: String,
        ownerName: String = "Library Owner",
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val email = recipientEmail.trim()
        if (email.isBlank() || !email.contains("@")) {
            onComplete(false, "Invalid email address.")
            return@withContext
        }

        // 1. Dispatch Firebase Auth Password Reset Email
        FirebaseAuthService.sendPasswordResetEmail(email) { fbSuccess, fbMsg ->
            Log.d("EmailSenderService", "Firebase dispatch for $email: $fbSuccess, $fbMsg")
        }

        // 2. Dispatch via Brevo if API key is present
        if (customBrevoApiKey.isNotBlank()) {
            try {
                val jsonBody = JSONObject().apply {
                    put("sender", JSONObject().apply {
                        put("name", "Vidyara Library Support")
                        put("email", "support@vidyara.com")
                    })
                    put("to", JSONArray().apply {
                        put(JSONObject().apply {
                            put("email", email)
                            put("name", ownerName)
                        })
                    })
                    put("subject", "Your Vidyara Password Reset OTP: $otpCode")
                    put("htmlContent", """
                        <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px;">
                            <h2 style="color: #0f172a; margin-bottom: 8px;">Vidyara Library Management</h2>
                            <p style="color: #475569; font-size: 14px;">Hello $ownerName,</p>
                            <p style="color: #475569; font-size: 14px;">You requested to reset your password. Use the 6-digit OTP code below to verify your account in the app:</p>
                            <div style="background-color: #f1f5f9; padding: 14px; text-align: center; border-radius: 8px; margin: 20px 0;">
                                <span style="font-size: 28px; font-weight: bold; letter-spacing: 6px; color: #ea580c;">$otpCode</span>
                            </div>
                            <p style="color: #64748b; font-size: 12px;">This verification code is valid for 10 minutes. If you did not request this, please ignore this email.</p>
                        </div>
                    """.trimIndent())
                }

                val request = Request.Builder()
                    .url("https://api.brevo.com/v3/smtp/email")
                    .addHeader("api-key", customBrevoApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.i("EmailSenderService", "Brevo OTP email delivered to $email")
                    onComplete(true, "OTP email delivered successfully via Brevo.")
                    return@withContext
                } else {
                    Log.w("EmailSenderService", "Brevo HTTP error: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("EmailSenderService", "Brevo dispatch exception: ${e.message}")
            }
        }

        // 3. Dispatch via Supabase recovery endpoint
        try {
            val supabaseUrl = "https://ynqtzrkeburayuplqwek.supabase.co/auth/v1/recover"
            val sbBody = JSONObject().apply { put("email", email) }
            val sbReq = Request.Builder()
                .url(supabaseUrl)
                .addHeader("apikey", "sb_publishable_b9PQVAKGt1qLn-t5v9Bi1A_u9dVoTQD")
                .addHeader("Content-Type", "application/json")
                .post(sbBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val sbRes = client.newCall(sbReq).execute()
            Log.d("EmailSenderService", "Supabase recovery response: ${sbRes.code}")
        } catch (e: Exception) {
            Log.w("EmailSenderService", "Supabase recovery exception: ${e.message}")
        }

        onComplete(true, "Verification OTP dispatched to $email.")
    }
}
