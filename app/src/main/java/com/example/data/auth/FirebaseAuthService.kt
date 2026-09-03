package com.example.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthService {
    
    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            onResult(false, "Invalid email address")
            return
        }
        try {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(cleanEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("FirebaseAuthService", "Password reset email sent to $cleanEmail via Firebase")
                        onResult(true, "Firebase password reset email dispatched to $cleanEmail.")
                    } else {
                        val errMsg = task.exception?.localizedMessage ?: "Firebase error"
                        Log.w("FirebaseAuthService", "Firebase reset error: $errMsg")
                        // If user record doesn't exist in Firebase yet, auto-provision the user in Firebase and resend reset email
                        if (errMsg.contains("no user", ignoreCase = true) || errMsg.contains("user-not-found", ignoreCase = true) || errMsg.contains("record", ignoreCase = true)) {
                            val tempPass = "Vidyara@" + (100000..999999).random()
                            auth.createUserWithEmailAndPassword(cleanEmail, tempPass)
                                .addOnCompleteListener { createRes ->
                                    if (createRes.isSuccessful) {
                                        auth.sendPasswordResetEmail(cleanEmail)
                                            .addOnCompleteListener { resendTask ->
                                                if (resendTask.isSuccessful) {
                                                    Log.i("FirebaseAuthService", "Auto-provisioned and sent reset email to $cleanEmail")
                                                    onResult(true, "Firebase password reset email dispatched to $cleanEmail.")
                                                } else {
                                                    onResult(false, resendTask.exception?.localizedMessage ?: errMsg)
                                                }
                                            }
                                    } else {
                                        onResult(false, errMsg)
                                    }
                                }
                        } else {
                            onResult(false, errMsg)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "Firebase Auth unavailable: ${e.message}")
            onResult(false, e.message)
        }
    }

    fun registerOrUpdateFirebaseUser(email: String, password: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || password.length < 6) return
        try {
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(cleanEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("FirebaseAuthService", "Firebase user registered for $cleanEmail")
                    } else {
                        Log.d("FirebaseAuthService", "User may already exist in Firebase: ${task.exception?.localizedMessage}")
                    }
                }
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "registerOrUpdateFirebaseUser error: ${e.message}")
        }
    }
}
