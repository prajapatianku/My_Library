package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.StudentPayment
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun ReceiptDialog(
    payment: StudentPayment,
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val library by viewModel.library.collectAsState()
    val context = LocalContext.current
    val students by viewModel.students.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f)
                .clip(RoundedCornerShape(24.dp)),
            color = PureWhite,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payment Receipt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmTextDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Printable Receipt Container
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFEBE3D7), RoundedCornerShape(16.dp))
                        .background(Color(0xFFFDFBF7))
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Library Brand Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = library.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = OrangePrimaryDark,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${library.address}, ${library.city} - ${library.pincode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmTextMuted,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Phone: ${library.phone} | UPI: ${library.upiId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFEBE3D7))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Receipt Info Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "RECEIPT NO", fontSize = 10.sp, color = WarmTextMuted, fontWeight = FontWeight.Bold)
                            Text(text = payment.receiptNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = WarmTextDark)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "DATE", fontSize = 10.sp, color = WarmTextMuted, fontWeight = FontWeight.Bold)
                            Text(text = payment.paymentDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = WarmTextDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Student Details Box
                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1EAE0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            ReceiptRow(label = "Student Name", value = payment.studentName)
                            ReceiptRow(label = "Student ID", value = payment.studentCode.ifBlank { "STU-001" })
                            ReceiptRow(label = "Shift", value = payment.shiftName)
                            if (payment.seatNumber.isNotBlank()) {
                                ReceiptRow(label = "Assigned Seat", value = "Seat ${payment.seatNumber}")
                            }
                            ReceiptRow(label = "Payment Mode", value = payment.paymentMethod.name)
                            ReceiptRow(label = "Transaction ID", value = payment.transactionId)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount Breakdown
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF16A34A).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Amount Paid", style = MaterialTheme.typography.labelMedium, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                Text(text = payment.notes.ifBlank { "Monthly Study Fee" }, style = MaterialTheme.typography.bodySmall, color = WarmTextDark)
                            }
                            Text(
                                text = "₹${payment.amount}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Footer Verification Note
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Digitally Verified & Generated by Library Space",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCCFC0))
                    ) {
                        Text("Close", color = WarmTextDark)
                    }
                    Button(
                        onClick = {
                            val student = students.find { it.id == payment.studentId || it.fullName == payment.studentName }
                            val targetPhone = student?.whatsapp?.ifBlank { student.mobile } ?: student?.mobile ?: ""
                            val msg = """
                                🏛️ *${library.name}*
                                📄 *FEE PAYMENT RECEIPT*
                                --------------------------------------
                                Receipt No: ${payment.receiptNumber}
                                Date: ${payment.paymentDate}
                                
                                👤 Student: *${payment.studentName}*
                                🆔 Student ID: ${payment.studentCode}
                                🪑 Assigned Seat: ${if (payment.seatNumber.isNotBlank()) "Seat " + payment.seatNumber else "Floating Area"}
                                🕒 Shift Name: ${payment.shiftName}
                                --------------------------------------
                                💰 Paid Amount: *₹${payment.amount}*
                                💳 Paid via: ${payment.paymentMethod.name}
                                🔗 Transaction ID: ${payment.transactionId}
                                📝 Description: ${payment.notes.ifBlank { "Monthly Study Fee" }}
                                
                                ✅ Status: *Paid & Verified*
                                Thank you for your payment!
                                --------------------------------------
                                💡 _Powered by My Library App_
                            """.trimIndent()
                            viewModel.launchWhatsApp(context, targetPhone, msg)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = PureWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Receipt", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
    }
}
