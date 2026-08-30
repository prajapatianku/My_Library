package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun DashboardScreen(
    viewModel: LibraryViewModel,
    onNavigateToStudents: () -> Unit,
    onNavigateToSeats: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val owner by viewModel.ownerProfile.collectAsState()
    val library by viewModel.library.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val activeBranchId by viewModel.activeBranchId.collectAsState()
    val saasPlan by viewModel.saasSubscription.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val students by viewModel.students.collectAsState()
    val requests by viewModel.registrationRequests.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val activeBranch = branches.find { it.id == activeBranchId } ?: branches.firstOrNull()
    val daysRemaining = remember(saasPlan) {
        viewModel.getSubscriptionDaysRemaining()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        if (daysRemaining in 0..7) {
            item {
                Surface(
                    color = DangerRed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.requestUpgrade("general") }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your subscription expires in $daysRemaining days! Tap here to renew now.",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Top App Header
        item {
            Surface(
                color = PureWhite,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(0.dp, Color.Transparent),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(OrangePrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Welcome, ${owner.fullName.split(" ").firstOrNull() ?: "Owner"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WarmTextDark
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.showBranchManagerDialog(true) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activeBranch?.name ?: library.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WarmTextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                if (branches.size > 1 || !viewModel.hasFeature("multi_branch")) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Switch branch",
                                        modifier = Modifier.size(16.dp),
                                        tint = OrangePrimary
                                    )
                                }
                            }
                        }

                        // SaaS Plan Badge
                        PlanBadge(
                            planType = saasPlan.planType,
                            onClick = { viewModel.requestUpgrade("view_plans") }
                        )
                    }
                }
            }
        }

        // Hero KPI Financial Banner (Vibrant Sunset Orange & White Accents)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF5200),
                                    Color(0xFFFF7A00),
                                    Color(0xFFFFA043)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Today's Collection",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₹${metrics.todayCollection}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "This Month", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "₹${metrics.monthlyCollection}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onNavigateToExpenses() }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Expenses ↗", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "₹${metrics.totalExpenses}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE4E6))
                            }
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onNavigateToStudents() }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Pending Dues ↗", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "₹${metrics.pendingDues}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFEF08A))
                            }
                        }
                    }
                }
            }
        }

        // Live Operational KPI Cards (Grid)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Library Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmTextDark
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiMetricCard(
                        title = "Active Students",
                        value = "${metrics.activeStudentsCount}",
                        subtitle = "Total enrolled",
                        icon = Icons.Default.People,
                        iconBgColor = OrangePrimaryContainer,
                        iconTintColor = OrangePrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStudents
                    )

                    KpiMetricCard(
                        title = "Present Today",
                        value = "${metrics.presentTodayCount}",
                        subtitle = "${metrics.absentTodayCount} Absent",
                        icon = Icons.Default.CheckCircle,
                        iconBgColor = Color(0xFFDCFCE7),
                        iconTintColor = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAttendance
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiMetricCard(
                        title = "Seats Occupancy",
                        value = "${metrics.occupiedSeatsCount}/${metrics.totalSeatsCount}",
                        subtitle = "${metrics.availableSeatsCount} seats available",
                        icon = Icons.Default.Chair,
                        iconBgColor = WarmPeachSecondaryContainer,
                        iconTintColor = OrangePrimaryDark,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSeats
                    )

                    KpiMetricCard(
                        title = "Fee Dues",
                        value = "₹${metrics.pendingDues}",
                        subtitle = "${metrics.expiringCount} students pending",
                        icon = Icons.Default.AttachMoney,
                        iconBgColor = Color(0xFFFEE2E2),
                        iconTintColor = DangerRed,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStudents
                    )
                }
            }
        }

        // Quick Actions Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmTextDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Quick Actions: Students, Seats, Public QR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Students",
                        icon = Icons.Default.People,
                        color = OrangePrimary,
                        onClick = onNavigateToStudents,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Seats",
                        icon = Icons.Default.Chair,
                        color = Color(0xFFF59E0B),
                        onClick = onNavigateToSeats,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Public QR",
                        icon = Icons.Default.QrCode2,
                        color = Color(0xFF0284C7),
                        onClick = { viewModel.showQrDialog(true) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Action Required Banner (Pending Requests / Expiring Dues / WhatsApp Fee Alerts)
        if (metrics.pendingRequestsCount > 0 || metrics.pendingDues > 0) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Action Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmTextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // WhatsApp Fee Due Notification Banner
                    if (metrics.pendingDues > 0) {
                        val isWhatsAppUnlocked = viewModel.hasFeature("whatsapp_fee_reminders")
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.5.dp, Color(0xFF25D366).copy(alpha = 0.6f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE8F8EE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Chat,
                                                contentDescription = "WhatsApp",
                                                tint = Color(0xFF128C7E),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "WhatsApp Fee Due Alerts",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WarmTextDark
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (isWhatsAppUnlocked) Color(0xFFE8F8EE) else Color(0xFFFFF7ED)
                                                ) {
                                                    Text(
                                                        text = if (isWhatsAppUnlocked) "2nd/3rd Plan" else "Plan 2 & 3",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isWhatsAppUnlocked) Color(0xFF128C7E) else OrangePrimaryDark,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${students.count { it.dueAmount > 0 }} students have fee dues today (₹${metrics.pendingDues} pending)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = WarmTextMuted
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.showWhatsAppReminderDialog(true) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE5DECE))
                                    ) {
                                        Icon(imageVector = Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(14.dp), tint = WarmTextDark)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View Dues", fontSize = 12.sp, color = WarmTextDark)
                                    }

                                    Button(
                                        onClick = {
                                            if (!isWhatsAppUnlocked) {
                                                viewModel.requestUpgrade("whatsapp_fee_reminders")
                                            } else {
                                                viewModel.sendOwnerWhatsAppAlert(context)
                                            }
                                        },
                                        modifier = Modifier.weight(1.3f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF128C7E))
                                    ) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isWhatsAppUnlocked) "Notify Owner" else "Unlock Alerts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (metrics.pendingRequestsCount > 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = OrangePrimaryContainer),
                            border = BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = OrangePrimary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${metrics.pendingRequestsCount} Student Registration Requests",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = WarmTextDark
                                        )
                                        Text(
                                            text = "Students registered via public QR waiting for approval",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WarmTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Registration Requests Cards preview
                    requests.filter { it.status == "pending" }.forEach { req ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, Color(0xFFF3ECE4)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = req.studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = WarmTextDark)
                                    Text(text = "${req.course} • ${req.requestedShift}", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                                    Text(text = "Mobile: ${req.mobile}", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalButton(
                                        onClick = { viewModel.rejectRequest(req.id) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFFEE2E2), contentColor = DangerRed)
                                    ) {
                                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.approveRequest(req.id) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                    ) {
                                        Text("Approve", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Students Quick List preview
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Students",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmTextDark
                    )
                    Text(
                        text = "View All (${students.size}) ↗",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToStudents() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        items(students.take(4)) { student ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { viewModel.selectStudentForDetail(student) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, Color(0xFFF3ECE4)),
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(OrangePrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.fullName.take(2).uppercase(),
                                fontWeight = FontWeight.Black,
                                color = OrangePrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = student.fullName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = WarmTextDark
                            )
                            Text(
                                text = "${student.studentCode} • ${student.assignedShiftName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmTextMuted
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (student.assignedSeatNumber.isNotBlank()) {
                            Surface(
                                color = WarmPeachSecondaryContainer,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, OrangePrimary.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "Seat ${student.assignedSeatNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangePrimaryDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (student.dueAmount > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Due: ₹${student.dueAmount}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                        }
                    }
                }
            }
        }
    }
}

