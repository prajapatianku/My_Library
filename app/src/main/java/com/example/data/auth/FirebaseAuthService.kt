package com.example.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthService {
    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        try {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(email.trim())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("FirebaseAuthService", "Password reset email sent to $email via Firebase")
                        onResult(true, "Firebase password reset email dispatched.")
                    } else {
                        val errMsg = task.exception?.localizedMessage ?: "Firebase error"
                        Log.w("FirebaseAuthService", "Firebase reset error: $errMsg")
                        onResult(false, errMsg)
                    }
                }
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "Firebase Auth unavailable: ${e.message}")
            onResult(false, e.message)
        }
    }
}
