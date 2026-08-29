package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SaaSPlanType
import com.example.data.model.Student
import com.example.ui.components.PlanBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel

private val WhatsAppGreen = Color(0xFF25D366)
private val WhatsAppDarkGreen = Color(0xFF128C7E)
private val WhatsAppBgLight = Color(0xFFE8F8EE)

@Composable
fun WhatsAppReminderDialog(
    viewModel: LibraryViewModel,
    preselectedStudent: Student? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val owner by viewModel.ownerProfile.collectAsState()
    val library by viewModel.library.collectAsState()
    val students by viewModel.students.collectAsState()
    val saasPlan by viewModel.saasSubscription.collectAsState()

    val isFeatureUnlocked = viewModel.hasFeature("whatsapp_fee_reminders")
    val dueStudents = remember(students) { students.filter { it.dueAmount > 0 } }
    val totalPendingDues = remember(dueStudents) { dueStudents.sumOf { it.dueAmount } }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Due Students & Alerts, 1 = Message Preview & Settings
    var activeStudentForPreview by remember {
        mutableStateOf(preselectedStudent ?: dueStudents.firstOrNull() ?: students.firstOrNull())
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(WhatsAppBgLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "WhatsApp",
                                tint = WhatsAppDarkGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "WhatsApp Fee Alerts",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmTextDark
                            )
                            Text(
                                text = "Automated Fee Due Notifications",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmTextMuted
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Plan Status Banner (2nd & 3rd Plan)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFeatureUnlocked) WhatsAppBgLight else Color(0xFFFFF7ED)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isFeatureUnlocked) WhatsAppGreen.copy(alpha = 0.4f) else OrangePrimary.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFeatureUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isFeatureUnlocked) WhatsAppDarkGreen else OrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isFeatureUnlocked) {
                                        "🟢 Active on ${saasPlan.planType.displayName}"
                                    } else {
                                        "🔒 Unlocked on 2nd (Premium) & 3rd (Business) Plans"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFeatureUnlocked) WhatsAppDarkGreen else OrangePrimaryDark
                                )
                                Text(
                                    text = if (isFeatureUnlocked) {
                                        "Owner WhatsApp: ${owner.whatsapp.ifBlank { owner.phone }}"
                                    } else {
                                        "Upgrade from Free Plan to enable instant owner & student alerts"
                                    },
                                    fontSize = 11.sp,
                                    color = WarmTextDark
                                )
                            }
                        }

                        if (!isFeatureUnlocked) {
                            Button(
                                onClick = {
                                    onDismiss()
                                    viewModel.requestUpgrade("whatsapp_fee_reminders")
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Text("Upgrade", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Subtabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF7F5F0),
                    contentColor = OrangePrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Due Alerts (${dueStudents.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Message Preview",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // TAB 0: Owner Alert & Due Student Reminders
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Top Section: Notify Owner on WhatsApp
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.5.dp, WhatsAppGreen.copy(alpha = 0.5f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.NotificationsActive,
                                                    contentDescription = null,
                                                    tint = WhatsAppDarkGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Owner Daily Due Digest",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = WarmTextDark
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${dueStudents.size} students have fee dues today (Total: ₹$totalPendingDues)",
                                                fontSize = 11.sp,
                                                color = WarmTextMuted
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            if (!isFeatureUnlocked) {
                                                onDismiss()
                                                viewModel.requestUpgrade("whatsapp_fee_reminders")
                                            } else {
                                                viewModel.sendOwnerWhatsAppAlert(context)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isFeatureUnlocked) "Notify Owner on WhatsApp Now" else "Unlock Owner WhatsApp Alerts (2nd/3rd Plan)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Due Students Header
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pending Student Fee Dues",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WarmTextDark
                                )
                                Text(
                                    text = "Total Pending: ₹$totalPendingDues",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                            }
                        }

                        if (dueStudents.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = WhatsAppDarkGreen,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "All fees are up to date!",
                                            fontWeight = FontWeight.Bold,
                                            color = WarmTextDark
                                        )
                                        Text(
                                            text = "No pending student dues at this time.",
                                            fontSize = 12.sp,
                                            color = WarmTextMuted
                                        )
                                    }
                                }
                            }
                        } else {
                            items(dueStudents) { student ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                                    border = BorderStroke(1.dp, Color(0xFFF3ECE4)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = student.fullName,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = WarmTextDark
                                                )
                                                Text(
                                                    text = "${student.studentCode} • ${student.assignedShiftName} • Seat ${student.assignedSeatNumber.ifBlank { "N/A" }}",
                                                    fontSize = 11.sp,
                                                    color = WarmTextMuted
                                                )
                                                Text(
                                                    text = "Due Date: ${student.feeDueDate} • Monthly: ₹${student.monthlyFee}",
                                                    fontSize = 11.sp,
                                                    color = WarmTextDark,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "₹${student.dueAmount}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = DangerRed
                                                )
                                                Text(
                                                    text = "Pending",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DangerRed
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    activeStudentForPreview = student
                                                    selectedTab = 1
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp),
                                                border = BorderStroke(1.dp, Color(0xFFE5DECE))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = WarmTextDark
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Preview", fontSize = 11.sp, color = WarmTextDark)
                                            }

                                            Button(
                                                onClick = {
                                                    if (!isFeatureUnlocked) {
                                                        onDismiss()
                                                        viewModel.requestUpgrade("whatsapp_fee_reminders")
                                                    } else {
                                                        viewModel.sendStudentWhatsAppReminder(context, student)
                                                    }
                                                },
                                                modifier = Modifier.weight(1.4f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Chat,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("WhatsApp Student", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: Live Message Preview
                    val targetStudent = activeStudentForPreview ?: dueStudents.firstOrNull() ?: students.first()
                    val studentReminderText = viewModel.generateStudentWhatsAppReminderText(targetStudent, library)
                    val ownerDigestText = viewModel.generateOwnerWhatsAppDueAlertText(dueStudents, library, owner)

                    var previewType by remember { mutableStateOf(0) } // 0 = Student Message, 1 = Owner Alert

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = previewType == 0,
                                onClick = { previewType = 0 },
                                label = { Text("Student Reminder Preview", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = previewType == 1,
                                onClick = { previewType = 1 },
                                label = { Text("Owner Digest Preview", fontSize = 11.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECE5DD)),
                            border = BorderStroke(1.dp, Color(0xFFD1D7DB))
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            ) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = PureWhite,
                                        shadowElevation = 1.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = if (previewType == 0) studentReminderText else ownerDigestText,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 12.sp,
                                                    lineHeight = 18.sp
                                                ),
                                                color = WarmTextDark
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (!isFeatureUnlocked) {
                                    onDismiss()
                                    viewModel.requestUpgrade("whatsapp_fee_reminders")
                                } else {
                                    if (previewType == 0) {
                                        viewModel.sendStudentWhatsAppReminder(context, targetStudent)
                                    } else {
                                        viewModel.sendOwnerWhatsAppAlert(context)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (previewType == 0) "Send to ${targetStudent.fullName} on WhatsApp" else "Send Due Digest to Owner WhatsApp",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
