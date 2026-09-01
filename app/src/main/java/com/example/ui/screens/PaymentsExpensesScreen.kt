package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun PaymentsExpensesScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val payments by viewModel.payments.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val students by viewModel.students.collectAsState()
    val showCollectFeeDialog by viewModel.showCollectFeeDialog.collectAsState()
    val showAddExpenseDialog by viewModel.showAddExpenseDialog.collectAsState()
    val activeReceipt by viewModel.activeReceipt.collectAsState()
    val selectedTab by viewModel.financeSubTab.collectAsState()

    var timeFilter by remember { mutableStateOf("Overall") } // "Daily", "Monthly", "Yearly", "Overall"

    val filteredPayments = remember(payments, timeFilter) {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val currentMonthStr = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
        val currentYearStr = java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        
        when (timeFilter) {
            "Daily" -> payments.filter { it.paymentDate.startsWith(todayStr) }
            "Monthly" -> payments.filter { it.paymentDate.startsWith(currentMonthStr) }
            "Yearly" -> payments.filter { it.paymentDate.startsWith(currentYearStr) }
            else -> payments
        }
    }

    val filteredExpenses = remember(expenses, timeFilter) {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val currentMonthStr = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
        val currentYearStr = java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        
        when (timeFilter) {
            "Daily" -> expenses.filter { it.expenseDate.startsWith(todayStr) }
            "Monthly" -> expenses.filter { it.expenseDate.startsWith(currentMonthStr) }
            "Yearly" -> expenses.filter { it.expenseDate.startsWith(currentYearStr) }
            else -> expenses
        }
    }

    val totalIncome = filteredPayments.sumOf { it.amount }
    val totalExpense = filteredExpenses.sumOf { it.amount }
    val netProfit = totalIncome - totalExpense

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        if (!viewModel.hasFeature("revenue_download")) {
                            viewModel.requestUpgrade("revenue_download")
                        } else {
                            viewModel.exportFinancialReport(context)
                        }
                    } else {
                        viewModel.showAddExpenseDialog(true)
                    }
                },
                icon = { Icon(imageVector = if (selectedTab == 0) Icons.Default.Download else Icons.Default.Add, contentDescription = null) },
                text = { Text(if (selectedTab == 0) "Export Report" else "Add Expense", fontWeight = FontWeight.Bold) },
                containerColor = if (selectedTab == 0) NavyPrimary else OrangePrimary,
                contentColor = PureWhite,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SlateBackground)
        ) {
            // Top Balance Summary Bar
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
                        Column {
                            Text(text = "Total Collections", fontSize = 11.sp, color = WarmTextMuted, fontWeight = FontWeight.Medium)
                            Text(text = "₹$totalIncome", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF15803D))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Expenses", fontSize = 11.sp, color = WarmTextMuted, fontWeight = FontWeight.Medium)
                            Text(text = "₹$totalExpense", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = DangerRed)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Net Cash Flow", fontSize = 11.sp, color = WarmTextMuted, fontWeight = FontWeight.Medium)
                            Text(text = "₹$netProfit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = if (netProfit >= 0) Color(0xFF15803D) else DangerRed)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Time Filter Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Daily", "Monthly", "Yearly", "Overall").forEach { filter ->
                            FilterChip(
                                selected = timeFilter == filter,
                                onClick = { timeFilter = filter },
                                label = { Text(filter, fontSize = 11.sp, fontWeight = if (timeFilter == filter) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangePrimaryContainer,
                                    selectedLabelColor = OrangePrimaryDark,
                                    containerColor = PureWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = timeFilter == filter,
                                    borderColor = if (timeFilter == filter) OrangePrimary else Color(0xFFE5DECE)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Selector: Fee Ledger vs Expenses
                    Surface(
                        color = Color(0xFFFBF8F4),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFECE4D8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setFinanceSubTab(0) },
                                color = if (selectedTab == 0) OrangePrimary else Color.Transparent,
                                shape = RoundedCornerShape(9.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Fee Ledger (${filteredPayments.size})",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (selectedTab == 0) PureWhite else WarmTextMuted
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setFinanceSubTab(1) },
                                color = if (selectedTab == 1) OrangePrimary else Color.Transparent,
                                shape = RoundedCornerShape(9.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Expenses (${filteredExpenses.size})",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (selectedTab == 1) PureWhite else WarmTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Tab Content
            if (selectedTab == 0) {
                // Fee Payments List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPayments) { payment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.showActiveReceipt(payment) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE4)),
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
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFDCFCE7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = payment.studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = WarmTextDark)
                                        Text(
                                            text = "${payment.receiptNumber} • ${payment.paymentMethod} • ${payment.paymentDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WarmTextMuted
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "+₹${payment.amount}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF15803D), style = MaterialTheme.typography.titleMedium)
                                    Text(text = "View Receipt", fontSize = 11.sp, color = OrangePrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                // Expenses List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredExpenses) { expense ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE4)),
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
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFEE2E2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = expense.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = WarmTextDark)
                                        Text(
                                            text = "${expense.category.displayName} • ${expense.expenseDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WarmTextMuted
                                        )
                                    }
                                }

                                Text(text = "-₹${expense.amount}", fontWeight = FontWeight.ExtraBold, color = DangerRed, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectFeeDialog(
    student: Student,
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(if (student.dueAmount > 0) student.dueAmount.toString() else student.monthlyFee.toString()) }
    var discount by remember { mutableStateOf("0") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var notes by remember { mutableStateOf("Monthly Fee Access") }
    var sendWhatsAppReceipt by remember { mutableStateOf(true) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            modifier = Modifier.fillMaxWidth(0.95f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE4))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Collect Student Fee", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmTextDark)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Student: ${student.fullName} (${student.studentCode})", style = MaterialTheme.typography.bodyMedium, color = OrangePrimary, fontWeight = FontWeight.Bold)
                if (student.dueAmount > 0) {
                    Text(text = "Pending Dues: ₹${student.dueAmount}", style = MaterialTheme.typography.bodySmall, color = DangerRed, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount Collected (₹) *") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(PaymentMethod.UPI, PaymentMethod.CASH, PaymentMethod.CARD, PaymentMethod.BANK_TRANSFER).forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method.name, fontSize = 11.sp, fontWeight = if (selectedMethod == method) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimaryContainer,
                                selectedLabelColor = OrangePrimaryDark,
                                containerColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedMethod == method,
                                borderColor = if (selectedMethod == method) OrangePrimary else Color(0xFFE5DECE)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Month Description") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // WhatsApp Receipt Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { sendWhatsAppReceipt = !sendWhatsAppReceipt }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = sendWhatsAppReceipt,
                        onCheckedChange = { sendWhatsAppReceipt = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF128C7E))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color(0xFF128C7E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Send Receipt via WhatsApp to student",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmTextDark
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val collectedAmount = amount.toIntOrNull() ?: 0
                        if (collectedAmount > 0) {
                            viewModel.collectFee(
                                studentId = student.id,
                                amount = collectedAmount,
                                discount = discount.toIntOrNull() ?: 0,
                                paymentMethod = selectedMethod,
                                notes = notes,
                                context = context,
                                sendWhatsAppReceipt = sendWhatsAppReceipt
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    enabled = (amount.toIntOrNull() ?: 0) > 0
                ) {
                    Text("Confirm Collection & Issue Receipt", fontWeight = FontWeight.Bold, color = PureWhite)
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.ELECTRICITY) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            modifier = Modifier.fillMaxWidth(0.95f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE4))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Log Library Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmTextDark)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. AC Electricity Bill) *") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹) *") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(ExpenseCategory.RENT, ExpenseCategory.ELECTRICITY, ExpenseCategory.INTERNET, ExpenseCategory.SALARY).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName.take(8), fontSize = 10.sp, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimaryContainer,
                                selectedLabelColor = OrangePrimaryDark,
                                containerColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == cat,
                                borderColor = if (selectedCategory == cat) OrangePrimary else Color(0xFFE5DECE)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val expAmount = amount.toIntOrNull() ?: 0
                        if (title.isNotBlank() && expAmount > 0) {
                            viewModel.addExpense(
                                category = selectedCategory,
                                title = title,
                                amount = expAmount,
                                paymentMethod = selectedMethod,
                                description = description
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    enabled = title.isNotBlank() && (amount.toIntOrNull() ?: 0) > 0
                ) {
                    Text("Save Expense Record", fontWeight = FontWeight.Bold, color = PureWhite)
                }
            }
        }
    }
}
