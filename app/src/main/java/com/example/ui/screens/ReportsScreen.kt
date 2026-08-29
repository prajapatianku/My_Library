package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UpgradeLockBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun ReportsScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val reportType by viewModel.reportType.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val students by viewModel.students.collectAsState()
    val seats by viewModel.seats.collectAsState()
    val saasPlan by viewModel.saasSubscription.collectAsState()

    val canExport = viewModel.hasFeature("revenue_download")

    val reportCategories = listOf("Revenue & Profit", "Due Report", "Seat Occupancy", "Shift Stats")

    val totalRevenue = payments.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpense
    val totalDues = students.sumOf { it.dueAmount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header & Export Button
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Analytics & Reports",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Trusted database calculations for your library",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Export / Download button with entitlement lock
                    Button(
                        onClick = {
                            if (!canExport) {
                                viewModel.requestUpgrade("revenue_download")
                            } else {
                                // Real Premium download action
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canExport) NavyPrimary else AmberTertiary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (canExport) Icons.Default.Download else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (canExport) "Export PDF" else "Unlock Export", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Report Category Selector
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reportCategories) { cat ->
                        FilterChip(
                            selected = reportType == cat,
                            onClick = { viewModel.setReportType(cat) },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimaryContainer,
                                selectedLabelColor = NavyPrimary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Report Content Body
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (reportType) {
                "Revenue & Profit" -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Financial Summary (Aug 2026)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))

                                ReportStatRow(label = "Gross Fee Collection", amount = "₹$totalRevenue", color = SuccessGreen)
                                ReportStatRow(label = "Operational Expenses", amount = "₹$totalExpense", color = DangerRed)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ReportStatRow(label = "Net Operating Profit", amount = "₹$netProfit", color = if (netProfit >= 0) SuccessGreen else DangerRed, isBold = true)
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Expense Category Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))

                                expenses.groupBy { it.category }.forEach { (cat, list) ->
                                    val catSum = list.sumOf { it.amount }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = cat.displayName, style = MaterialTheme.typography.bodySmall)
                                        Text(text = "₹$catSum", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                "Due Report" -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Total Pending Dues: ₹$totalDues", fontWeight = FontWeight.Bold, color = DangerRed, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Students with unpaid monthly fees", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    items(students.filter { it.dueAmount > 0 }) { stu ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = stu.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Phone: ${stu.mobile} • Shift: ${stu.assignedShiftName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "₹${stu.dueAmount}", fontWeight = FontWeight.Bold, color = DangerRed, style = MaterialTheme.typography.titleMedium)
                                    Button(
                                        onClick = { viewModel.showCollectFeeDialog(stu) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                    ) {
                                        Text("Collect", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                "Seat Occupancy" -> {
                    item {
                        val total = seats.size
                        val occupied = seats.count { it.status == com.example.data.model.SeatStatus.OCCUPIED }
                        val pct = if (total > 0) (occupied * 100) / total else 0

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Seat Utilization: $pct%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp)),
                                    color = NavyPrimary,
                                    trackColor = NavyPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Occupied Desks: $occupied", style = MaterialTheme.typography.bodySmall, color = NavyPrimary, fontWeight = FontWeight.Bold)
                                    Text(text = "Available Desks: ${total - occupied}", style = MaterialTheme.typography.bodySmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                else -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Shift Capacity & Enrollment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                listOf(
                                    "Morning Shift (6 AM - 12 PM)" to "18 Students",
                                    "Afternoon Shift (12 PM - 5 PM)" to "12 Students",
                                    "Evening Shift (5 PM - 11 PM)" to "16 Students",
                                    "Full Day 24/7 Access" to "24 Students"
                                ).forEach { (shift, count) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = shift, style = MaterialTheme.typography.bodySmall)
                                        Text(text = count, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = NavyPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Upgrade Banner for Free Users at the end of Reports
            if (!canExport) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clickable { viewModel.requestUpgrade("revenue_download") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AmberTertiaryContainer.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = AmberTertiary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "Unlock Financial Exports", fontWeight = FontWeight.Bold, color = AmberTertiary, style = MaterialTheme.typography.titleSmall)
                                    Text(text = "Download PDF & CSV reports with Premium (₹99/mo)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = AmberTertiary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStatRow(label: String, amount: String, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = amount,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
