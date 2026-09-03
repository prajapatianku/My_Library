package com.example.data.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSenderService {
    private const val GMAIL_SENDER = "ratneshankit123@gmail.com"
    private const val GMAIL_APP_PASSWORD = "vytlpndyompmkcxx"

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

        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
                put("mail.smtp.ssl.protocols", "TLSv1.2")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(GMAIL_SENDER, GMAIL_APP_PASSWORD)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(GMAIL_SENDER, "Vidyara Library Support"))
                addRecipient(Message.RecipientType.TO, InternetAddress(email))
                subject = "Vidyara - Your Verification OTP is $otpCode"

                val html = """
                    <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;">
                        <h2 style="color: #0f172a; margin-bottom: 6px;">Vidyara Library</h2>
                        <p style="color: #64748b; font-size: 13px; margin-top: 0;">Smart Library Automation Platform</p>
                        <hr style="border: none; border-top: 1px solid #f1f5f9; margin: 16px 0;" />
                        <p style="color: #334155; font-size: 14px;">Hello <b>$ownerName</b>,</p>
                        <p style="color: #334155; font-size: 14px;">You requested to reset your password or verify your account. Please use the following 6-digit One-Time Password (OTP) in the Vidyara app:</p>
                        <div style="background-color: #f8fafc; border: 2px dashed #ea580c; padding: 18px; text-align: center; border-radius: 10px; margin: 20px 0;">
                            <span style="font-size: 32px; font-weight: 900; letter-spacing: 8px; color: #ea580c;">$otpCode</span>
                        </div>
                        <p style="color: #64748b; font-size: 12px;">? This code is strictly confidential and expires in <b>10 minutes</b>. Please do not share it with anyone.</p>
                        <hr style="border: none; border-top: 1px solid #f1f5f9; margin: 16px 0;" />
                        <p style="color: #94a3b8; font-size: 11px;">If you didn't request this code, you can safely ignore this email.</p>
                    </div>
                """.trimIndent()

                setContent(html, "text/html; charset=utf-8")
            }

            Transport.send(message)
            Log.i("EmailSenderService", "? OTP $otpCode sent via Gmail SMTP to $email successfully!")
            onComplete(true, "OTP email sent successfully to $email via Gmail.")
        } catch (e: Exception) {
            Log.e("EmailSenderService", "? Gmail SMTP error sending to $email: ${e.message}", e)
            onComplete(false, "Failed to send email: ${e.localizedMessage}")
        }
    }
}
