package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SaaSPlanType
import com.example.ui.components.PlanBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun SettingsScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val owner by viewModel.ownerProfile.collectAsState()
    val library by viewModel.library.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val activeBranchId by viewModel.activeBranchId.collectAsState()
    val saasPlan by viewModel.saasSubscription.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val showBranchDialog by viewModel.showBranchManagerDialog.collectAsState()
    val showQrDialog by viewModel.showQrDialog.collectAsState()

    var showAuditLogsDialog by remember { mutableStateOf(false) }
    var showLibraryProfileDialog by remember { mutableStateOf(false) }
    var showShiftManagerDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Owner Profile Card
        item {
            Surface(
                color = PureWhite,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(OrangePrimaryContainer)
                                    .border(1.dp, OrangePrimary.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = owner.fullName.take(2).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    color = OrangePrimaryDark,
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = owner.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WarmTextDark
                                )
                                Text(
                                    text = owner.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarmTextMuted
                                )
                                Text(
                                    text = owner.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = WarmTextMuted
                                )
                            }
                        }

                        PlanBadge(
                            planType = saasPlan.planType,
                            onClick = { viewModel.requestUpgrade("settings") }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Section: SaaS Subscription & Plan Upgrade
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { viewModel.requestUpgrade("settings") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OrangePrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Library Subscription",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            text = "Current: ${saasPlan.planType.displayName} (Status: Active)",
                            style = MaterialTheme.typography.bodySmall,
                            color = PureWhite.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to view plans, pricing & upgrade features",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmPeachSecondaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = PureWhite)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Section: Management Options
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Library Operations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmTextDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsItemCard(
                    title = "Library Details & Timings",
                    subtitle = "${library.name} • ${library.openingTime} - ${library.closingTime}",
                    icon = Icons.Default.Storefront,
                    onClick = { showLibraryProfileDialog = true }
                )

                SettingsItemCard(
                    title = "Manage Library Shifts",
                    subtitle = "Configure shifts, operating timings & fee rates",
                    icon = Icons.Default.AccessTime,
                    onClick = { showShiftManagerDialog = true }
                )

                SettingsItemCard(
                    title = "WhatsApp Fee Due Reminders",
                    subtitle = if (viewModel.hasFeature("whatsapp_fee_reminders")) "Active • Automated alerts to owner & students" else "Notify owner on due dates (Plan 2 & 3)",
                    icon = Icons.Default.Chat,
                    isLocked = !viewModel.hasFeature("whatsapp_fee_reminders"),
                    onClick = {
                        if (!viewModel.hasFeature("whatsapp_fee_reminders")) {
                            viewModel.requestUpgrade("whatsapp_fee_reminders")
                        } else {
                            viewModel.showWhatsAppReminderDialog(true)
                        }
                    }
                )

                SettingsItemCard(
                    title = "Multi-Branch Management",
                    subtitle = "${branches.size} branch registered (Requires Business Plan)",
                    icon = Icons.Default.AccountTree,
                    isLocked = !viewModel.hasFeature("multi_branch"),
                    onClick = { viewModel.showBranchManagerDialog(true) }
                )

                SettingsItemCard(
                    title = "Student Registration QR Code",
                    subtitle = "Allow walk-in students to register by scanning QR",
                    icon = Icons.Default.QrCode2,
                    onClick = { viewModel.showQrDialog(true) }
                )

                SettingsItemCard(
                    title = "Audit & Security Logs",
                    subtitle = "Track financial & seat changes (${auditLogs.size} logs)",
                    icon = Icons.Default.History,
                    onClick = { showAuditLogsDialog = true }
                )

                SettingsItemCard(
                    title = "Support & Helpdesk",
                    subtitle = if (viewModel.hasFeature("email_support")) "Priority Support Active" else "Available on Premium/Business",
                    icon = Icons.Default.HelpOutline,
                    isLocked = !viewModel.hasFeature("email_support"),
                    onClick = {
                        if (!viewModel.hasFeature("email_support")) {
                            viewModel.requestUpgrade("email_support")
                        }
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Section: Account & Session Management (Logout Feature)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Account & Session",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmTextDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { showLogoutConfirmDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEE2E2)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Log Out",
                                    tint = DangerRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Log Out",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                                Text(
                                    text = "Sign out from ${owner.fullName}'s admin session",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarmTextMuted
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = DangerRed.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Confirm Logout", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of ${library.name}? You can sign back in anytime using your registered email or phone number.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmTextDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Log Out", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = WarmTextDark)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = PureWhite
        )
    }

    // Audit Logs Dialog
    if (showAuditLogsDialog) {
        AuditLogsDialog(
            auditLogs = auditLogs,
            onDismiss = { showAuditLogsDialog = false }
        )
    }

    // Library Profile Dialog
    if (showLibraryProfileDialog) {
        LibraryProfileDialog(
            library = library,
            onDismiss = { showLibraryProfileDialog = false }
        )
    }

    // Shift Manager Dialog
    if (showShiftManagerDialog) {
        ShiftManagerDialog(
            viewModel = viewModel,
            onDismiss = { showShiftManagerDialog = false }
        )
    }
}

@Composable
fun SettingsItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isLocked) AmberTertiaryContainer else NavyPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else icon,
                        contentDescription = null,
                        tint = if (isLocked) AmberTertiary else NavyPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun RegistrationQrDialog(
    library: com.example.data.model.Library,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Registration QR Code", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // QR Graphic Placeholder / Container
                Surface(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Registration QR",
                            modifier = Modifier.size(130.dp),
                            tint = NavyPrimary
                        )
                        Text(text = "Scan to Register", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = library.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "https://my-library.app/register/${library.registrationToken}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Print QR Poster")
                }
            }
        }
    }
}

@Composable
fun BranchManagerDialog(
    branches: List<com.example.data.model.Branch>,
    activeBranchId: String,
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    var newBranchName by remember { mutableStateOf("") }
    var newBranchAddress by remember { mutableStateOf("") }
    var newBranchPhone by remember { mutableStateOf("") }
    var newBranchCity by remember { mutableStateOf("Greater Noida") }
    var newBranchState by remember { mutableStateOf("Uttar Pradesh") }
    var newBranchPincode by remember { mutableStateOf("201310") }
    var newBranchTotalSeats by remember { mutableStateOf("60") }
    var newBranchOpeningTime by remember { mutableStateOf("06:00 AM") }
    var newBranchClosingTime by remember { mutableStateOf("11:00 PM") }
    var newBranchUpiId by remember { mutableStateOf("saraswati.lib@okhdfcbank") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Branch Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmTextDark)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Active Branches", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Spacer(modifier = Modifier.height(6.dp))

                branches.forEach { br ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                viewModel.switchBranch(br.id)
                                onDismiss()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (br.id == activeBranchId) NavyPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = br.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = WarmTextDark)
                                Text(text = "${br.code} • ${br.address}", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                                Text(text = "Seats: ${br.totalSeats} | UPI: ${br.upiId}", fontSize = 10.sp, color = OrangePrimaryDark)
                            }
                            if (br.id == activeBranchId) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Active", tint = NavyPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Create New Branch (Premium Plan)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newBranchName,
                    onValueChange = { newBranchName = it },
                    label = { Text("Library / Branch Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newBranchAddress,
                    onValueChange = { newBranchAddress = it },
                    label = { Text("Address *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newBranchCity,
                        onValueChange = { newBranchCity = it },
                        label = { Text("City") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newBranchPincode,
                        onValueChange = { newBranchPincode = it },
                        label = { Text("Pincode") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newBranchPhone,
                        onValueChange = { newBranchPhone = it },
                        label = { Text("Phone / Mobile") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newBranchTotalSeats,
                        onValueChange = { newBranchTotalSeats = it },
                        label = { Text("Seats Capacity") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newBranchOpeningTime,
                        onValueChange = { newBranchOpeningTime = it },
                        label = { Text("Opens At") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newBranchClosingTime,
                        onValueChange = { newBranchClosingTime = it },
                        label = { Text("Closes At") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newBranchUpiId,
                    onValueChange = { newBranchUpiId = it },
                    label = { Text("UPI payment ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (newBranchName.isNotBlank()) {
                            viewModel.createBranch(
                                name = newBranchName,
                                code = "BR-0" + (branches.size + 1),
                                address = newBranchAddress,
                                phone = newBranchPhone.ifBlank { "+91 9876543210" },
                                city = newBranchCity,
                                state = newBranchState,
                                pincode = newBranchPincode,
                                totalSeats = newBranchTotalSeats.toIntOrNull() ?: 60,
                                openingTime = newBranchOpeningTime,
                                closingTime = newBranchClosingTime,
                                upiId = newBranchUpiId
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    enabled = newBranchName.isNotBlank()
                ) {
                    Text("Create Branch")
                }
            }
        }
    }
}

@Composable
fun AuditLogsDialog(
    auditLogs: List<com.example.data.model.AuditLog>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Audit & Security Logs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(auditLogs) { log ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = log.action, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = NavyPrimary)
                                    Text(text = log.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = log.details, style = MaterialTheme.typography.bodySmall)
                                Text(text = "By: ${log.user}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryProfileDialog(
    library: com.example.data.model.Library,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Library Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = library.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OrangePrimaryDark)
                Text(text = "${library.address}${if (library.location.isNotBlank()) " • " + library.location else ""}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Total Seats Capacity: ${library.totalSeats} Seats", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Text(text = "Operating Hours: ${library.openingTime} to ${library.closingTime}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Contact Phone: ${library.phone}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Official Email: ${library.email}", style = MaterialTheme.typography.bodySmall)
                Text(text = "UPI Payment ID: ${library.upiId}", style = MaterialTheme.typography.bodySmall, color = SuccessGreen, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
fun ShiftManagerDialog(
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val shifts by viewModel.shifts.collectAsState()
    
    var showAddForm by remember { mutableStateOf(false) }
    
    // Add shift form state
    var newName by remember { mutableStateOf("") }
    var newStart by remember { mutableStateOf("") }
    var newEnd by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }

    // Edit shift state
    var editingShiftId by remember { mutableStateOf<String?>(null) }
    var editStart by remember { mutableStateOf("") }
    var editEnd by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.85f)
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
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = OrangePrimaryDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shift Configuration",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmTextDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (showAddForm) {
                        // Add Shift Form Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SlateBackground.copy(alpha = 0.5f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Add Custom Shift",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = WarmTextDark
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    OutlinedTextField(
                                        value = newName,
                                        onValueChange = { newName = it },
                                        label = { Text("Shift Name (e.g. Night Shift)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = WarmTextDark,
                                            unfocusedTextColor = WarmTextDark,
                                            focusedContainerColor = PureWhite,
                                            unfocusedContainerColor = PureWhite
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = newStart,
                                            onValueChange = { newStart = it },
                                            label = { Text("Start (e.g. 09:00 PM)") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = WarmTextDark,
                                                unfocusedTextColor = WarmTextDark,
                                                focusedContainerColor = PureWhite,
                                                unfocusedContainerColor = PureWhite
                                            )
                                        )
                                        OutlinedTextField(
                                            value = newEnd,
                                            onValueChange = { newEnd = it },
                                            label = { Text("End (e.g. 06:00 AM)") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = WarmTextDark,
                                                unfocusedTextColor = WarmTextDark,
                                                focusedContainerColor = PureWhite,
                                                unfocusedContainerColor = PureWhite
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = newPrice,
                                        onValueChange = { newPrice = it },
                                        label = { Text("Monthly Fee (₹)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = WarmTextDark,
                                            unfocusedTextColor = WarmTextDark,
                                            focusedContainerColor = PureWhite,
                                            unfocusedContainerColor = PureWhite
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showAddForm = false },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancel", color = WarmTextDark)
                                        }
                                        Button(
                                            onClick = {
                                                val fee = newPrice.toIntOrNull() ?: 0
                                                if (newName.isNotBlank() && newStart.isNotBlank() && newEnd.isNotBlank() && fee > 0) {
                                                    viewModel.createShift(newName, newStart, newEnd, fee)
                                                    showAddForm = false
                                                    newName = ""
                                                    newStart = ""
                                                    newEnd = ""
                                                    newPrice = ""
                                                }
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                        ) {
                                            Text("Add Shift")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Button(
                                onClick = { showAddForm = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Custom Shift")
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Existing Timings & Fees",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WarmTextDark,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(shifts) { shift ->
                        val isEditing = editingShiftId == shift.id
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE4))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                if (isEditing) {
                                    Text(
                                        text = "Editing timings for ${shift.name}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrangePrimaryDark
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = editStart,
                                            onValueChange = { editStart = it },
                                            label = { Text("Start Time") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = WarmTextDark,
                                                unfocusedTextColor = WarmTextDark,
                                                focusedContainerColor = PureWhite,
                                                unfocusedContainerColor = PureWhite
                                            )
                                        )
                                        OutlinedTextField(
                                            value = editEnd,
                                            onValueChange = { editEnd = it },
                                            label = { Text("End Time") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = WarmTextDark,
                                                unfocusedTextColor = WarmTextDark,
                                                focusedContainerColor = PureWhite,
                                                unfocusedContainerColor = PureWhite
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = editPrice,
                                        onValueChange = { editPrice = it },
                                        label = { Text("Default Fee (₹)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = WarmTextDark,
                                            unfocusedTextColor = WarmTextDark,
                                            focusedContainerColor = PureWhite,
                                            unfocusedContainerColor = PureWhite
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { editingShiftId = null },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancel", color = WarmTextDark)
                                        }
                                        Button(
                                            onClick = {
                                                val priceVal = editPrice.toIntOrNull() ?: shift.defaultPrice
                                                viewModel.updateShift(
                                                    shift.copy(
                                                        startTime = editStart,
                                                        endTime = editEnd,
                                                        defaultPrice = priceVal
                                                    )
                                                )
                                                editingShiftId = null
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                        ) {
                                            Text("Save Changes")
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = shift.name,
                                                fontWeight = FontWeight.Bold,
                                                color = WarmTextDark,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = WarmTextMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${shift.startTime} - ${shift.endTime}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = WarmTextMuted
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "₹${shift.defaultPrice}/mo",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = OrangePrimaryDark,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    editingShiftId = shift.id
                                                    editStart = shift.startTime
                                                    editEnd = shift.endTime
                                                    editPrice = shift.defaultPrice.toString()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                            ) {
                                                Text("Edit", fontSize = 11.sp, color = WarmTextDark)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
