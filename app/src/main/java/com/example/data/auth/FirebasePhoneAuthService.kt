package com.example.data.auth

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

object FirebasePhoneAuthService {
    private const val TAG = "FirebasePhoneAuth"
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    var latestAutoSmsCode: String? = null
    var lastSentPhoneNumber: String? = null

    fun sendSmsOtp(
        activity: Activity?,
        phoneNumber: String,
        onCodeSent: (String) -> Unit = {},
        onAutoVerified: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val cleanNumber = phoneNumber.trim().filter { it.isDigit() || it == '+' }
        val formatted = if (cleanNumber.startsWith("+")) {
            cleanNumber
        } else if (cleanNumber.length == 10) {
            "+91$cleanNumber"
        } else {
            "+$cleanNumber"
        }

        lastSentPhoneNumber = formatted

        if (activity == null) {
            Log.w(TAG, "Cannot initiate PhoneAuth: Activity is null")
            onError("Activity context not available.")
            return
        }

        try {
            val auth = FirebaseAuth.getInstance()
            val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formatted)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        Log.i(TAG, "? Auto-verification completed for $formatted: ${credential.smsCode}")
                        val code = credential.smsCode
                        if (!code.isNullOrBlank()) {
                            latestAutoSmsCode = code
                            onAutoVerified(code)
                        }
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Log.e(TAG, "? SMS OTP verification failed for $formatted: ${e.message}", e)
                        onError(e.localizedMessage ?: "Failed to send SMS")
                    }

                    override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                        Log.i(TAG, "?? SMS OTP code dispatched to $formatted. VerificationId: $id")
                        verificationId = id
                        resendToken = token
                        onCodeSent(id)
                    }
                })

            resendToken?.let { optionsBuilder.setForceResendingToken(it) }

            PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
            Log.i(TAG, "PhoneAuthProvider.verifyPhoneNumber started for $formatted")
        } catch (e: Exception) {
            Log.e(TAG, "Exception during sendSmsOtp: ${e.message}", e)
            onError(e.localizedMessage ?: "Failed to send SMS OTP")
        }
    }

    fun verifySmsOtp(enteredOtp: String, onResult: (Boolean, String?) -> Unit) {
        val trimmed = enteredOtp.trim()
        if (latestAutoSmsCode != null && latestAutoSmsCode == trimmed) {
            onResult(true, null)
            return
        }

        val vId = verificationId
        if (vId.isNullOrBlank()) {
            onResult(false, "No active SMS session.")
            return
        }

        try {
            val credential = PhoneAuthProvider.getCredential(vId, trimmed)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "? Firebase phone credential verified successfully!")
                        onResult(true, null)
                    } else {
                        val errMsg = task.exception?.localizedMessage ?: "Invalid SMS code."
                        Log.w(TAG, "Verification error: $errMsg")
                        onResult(false, errMsg)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "verifySmsOtp exception: ${e.message}", e)
            onResult(false, e.localizedMessage)
        }
    }
}
