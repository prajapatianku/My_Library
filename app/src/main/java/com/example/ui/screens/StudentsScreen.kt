package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Student
import com.example.data.model.StudentStatus
import com.example.data.model.Seat
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val students by viewModel.filteredStudents.collectAsState()
    val allStudents by viewModel.students.collectAsState()
    val searchQuery by viewModel.studentSearchQuery.collectAsState()
    val statusFilter by viewModel.studentStatusFilter.collectAsState()
    val shiftFilter by viewModel.studentShiftFilter.collectAsState()
    val selectedStudent by viewModel.selectedStudentForDetail.collectAsState()
    val showAddDialog by viewModel.showAddStudentDialog.collectAsState()

    val shiftOptions = listOf("All", "Morning", "Afternoon", "Evening", "Full Day")
    val statusOptions = listOf("All", "Active", "Has Due", "Expired")

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddStudentDialog(true) },
                icon = { Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Student", fontWeight = FontWeight.Bold) },
                containerColor = OrangePrimary,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SlateBackground)
        ) {
            // Top Search Bar
            Surface(
                color = PureWhite,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setStudentSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name, code, phone, or seat...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OrangePrimary) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setStudentSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color(0xFFE5DECE),
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(statusOptions) { status ->
                            FilterChip(
                                selected = statusFilter == status,
                                onClick = { viewModel.setStudentStatusFilter(status) },
                                label = { Text(status, fontWeight = if (statusFilter == status) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangePrimaryContainer,
                                    selectedLabelColor = OrangePrimaryDark,
                                    containerColor = PureWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = statusFilter == status,
                                    borderColor = if (statusFilter == status) OrangePrimary else Color(0xFFE5DECE)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Shift Filter Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(shiftOptions) { shift ->
                            FilterChip(
                                selected = shiftFilter == shift,
                                onClick = { viewModel.setStudentShiftFilter(shift) },
                                label = { Text(shift, fontWeight = if (shiftFilter == shift) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarmPeachSecondaryContainer,
                                    selectedLabelColor = OrangePrimaryDark,
                                    containerColor = PureWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = shiftFilter == shift,
                                    borderColor = if (shiftFilter == shift) OrangePrimaryDark else Color(0xFFE5DECE)
                                )
                            )
                        }
                    }
                }
            }

            // Student Count Summary & WhatsApp Alerts Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${students.size} of ${allStudents.size} Students",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmTextMuted,
                    fontWeight = FontWeight.SemiBold
                )

                val dueCount = remember(allStudents) { allStudents.count { it.dueAmount > 0 } }
                if (dueCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE8F8EE),
                        border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { viewModel.showWhatsAppReminderDialog(true) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color(0xFF128C7E),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$dueCount Dues (WhatsApp)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF128C7E)
                            )
                        }
                    }
                }
            }

            // Student List
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = OrangePrimary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No students found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarmTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search or filters, or add a new student.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmTextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(students) { student ->
                        StudentCard(
                            student = student,
                            onClick = { viewModel.selectStudentForDetail(student) },
                            onCollectFee = { viewModel.showCollectFeeDialog(student) },
                            onWhatsAppReminder = { viewModel.sendStudentWhatsAppReminder(context, student) },
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.mobile}"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCard(
    student: Student,
    onClick: () -> Unit,
    onCollectFee: () -> Unit,
    onWhatsAppReminder: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(OrangePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.fullName.take(2).uppercase(),
                            fontWeight = FontWeight.Black,
                            color = OrangePrimary,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = student.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarmTextDark
                        )
                        Text(
                            text = "${student.studentCode} • ${student.course}",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmTextMuted
                        )
                    }
                }

                StatusBadge(status = if (student.dueAmount > 0) "Has Due" else "Active")
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF3ECE4))
            Spacer(modifier = Modifier.height(10.dp))

            // Details row: Shift, Seat, Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Shift", fontSize = 10.sp, color = WarmTextMuted)
                    Text(text = student.assignedShiftName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                }

                Column {
                    Text(text = "Seat", fontSize = 10.sp, color = WarmTextMuted)
                    Text(
                        text = if (student.assignedSeatNumber.isNotBlank()) "Seat ${student.assignedSeatNumber}" else "Floating",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (student.assignedSeatNumber.isNotBlank()) OrangePrimaryDark else WarmTextDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Fee / Due", fontSize = 10.sp, color = WarmTextMuted)
                    Row {
                        Text(text = "₹${student.monthlyFee}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                        if (student.dueAmount > 0) {
                            Text(text = " (Due ₹${student.dueAmount})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = DangerRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DECE))
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp), tint = WarmTextDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 12.sp, color = WarmTextDark, fontWeight = FontWeight.Bold)
                }

                if (student.dueAmount > 0) {
                    Button(
                        onClick = onWhatsAppReminder,
                        modifier = Modifier.weight(1.3f),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF128C7E))
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onCollectFee,
                    modifier = Modifier.weight(if (student.dueAmount > 0) 1.2f else 1.3f),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (student.dueAmount > 0) OrangePrimary else Color(0xFF16A34A)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (student.dueAmount > 0) "Collect Due" else "Collect Fee", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AddStudentDialog(
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val shifts by viewModel.shifts.collectAsState()
    val seats by viewModel.seats.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("UPSC / Govt Exam") }
    var selectedShift by remember { mutableStateOf(shifts.firstOrNull()?.name ?: "Full Day") }
    var selectedSeat by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var studentAddress by remember { mutableStateOf("") }
    
    val vacantSeats = remember(seats) {
        seats.filter { it.status == com.example.data.model.SeatStatus.AVAILABLE }
    }
    var showSeatSelectionDialog by remember { mutableStateOf(false) }
    
    // Auto-fill initial rate based on selected shift
    val defaultShiftPrice = remember(selectedShift, shifts) {
        shifts.find { it.name == selectedShift }?.defaultPrice ?: 1000
    }
    var monthlyFee by remember(defaultShiftPrice) { mutableStateOf(defaultShiftPrice.toString()) }

    if (showSeatSelectionDialog) {
        SeatSelectionDialog(
            availableSeats = vacantSeats,
            onSeatSelected = { selectedSeat = it },
            onDismiss = { showSeatSelectionDialog = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Add New Student", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarmTextDark)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 10) {
                            mobile = digits
                        }
                    },
                    label = { Text("Mobile Number * (10 Digits)") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = true,
                    isError = mobile.isNotEmpty() && mobile.length != 10,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Exam / Course (e.g. UPSC, SSC)") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Shift Selection Row
                Text(
                    text = "Select Shift *",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = WarmTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(shifts) { sh ->
                        FilterChip(
                            selected = selectedShift == sh.name,
                            onClick = { selectedShift = sh.name },
                            label = { Text(sh.name, fontSize = 11.sp, fontWeight = if (selectedShift == sh.name) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimaryContainer,
                                selectedLabelColor = OrangePrimaryDark,
                                containerColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedShift == sh.name,
                                borderColor = if (selectedShift == sh.name) OrangePrimary else Color(0xFFE5DECE)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showSeatSelectionDialog = true }
                    ) {
                        OutlinedTextField(
                            value = selectedSeat,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Seat *") },
                            placeholder = { Text("Tap to select") },
                            textStyle = AppInputTextStyle,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = WarmTextDark,
                                disabledBorderColor = Color(0xFFE5DECE),
                                disabledLabelColor = WarmTextMuted,
                                disabledContainerColor = PureWhite
                            ),
                            singleLine = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = monthlyFee,
                        onValueChange = { monthlyFee = it },
                        label = { Text("Shift Price (₹)") },
                        textStyle = AppInputTextStyle,
                        colors = appOutlinedTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gender Selection
                Text(
                    text = "Gender *",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = WarmTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Male", "Female", "Other").forEach { gen ->
                        val isSel = gender == gen
                        FilterChip(
                            selected = isSel,
                            onClick = { gender = gen },
                            label = { Text(gen, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimaryContainer,
                                selectedLabelColor = OrangePrimaryDark,
                                containerColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSel,
                                borderColor = if (isSel) OrangePrimary else Color(0xFFE5DECE)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Address Input
                OutlinedTextField(
                    value = studentAddress,
                    onValueChange = { studentAddress = it },
                    label = { Text("Address") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (fullName.isNotBlank() && mobile.isNotBlank()) {
                            viewModel.createStudent(
                                fullName = fullName,
                                mobile = mobile,
                                whatsapp = mobile,
                                email = email,
                                course = course,
                                assignedSeat = selectedSeat.trim(),
                                assignedShift = selectedShift,
                                monthlyFee = monthlyFee.toIntOrNull() ?: 1000,
                                initialDue = monthlyFee.toIntOrNull() ?: 1000,
                                gender = gender,
                                address = studentAddress
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    enabled = fullName.isNotBlank() && mobile.length == 10
                ) {
                    Text("Enroll Student")
                }
            }
        }
    }
}

@Composable
fun StudentDetailDialog(
    student: Student,
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val payments by viewModel.payments.collectAsState()
    val studentPayments = payments.filter { it.studentId == student.id || it.studentName == student.fullName }
    val isWhatsAppUnlocked = viewModel.hasFeature("whatsapp_fee_reminders")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
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
                    Text(text = "Student Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Digital ID Card Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "MY LIBRARY STUDENT PASS", color = AmberTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text(text = student.studentCode, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp, 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = student.fullName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = student.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = student.course, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                                Text(text = "Shift: ${student.assignedShiftName} | Seat: ${student.assignedSeatNumber.ifBlank { "Floating" }}", fontSize = 11.sp, color = AmberTertiary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Profile Info: Gender & Address
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF8F3)),
                    border = BorderStroke(1.dp, Color(0xFFE5DECE))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Gender", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                            Text(text = student.gender, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Contact", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                            Text(text = student.mobile, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                        }
                        if (student.address.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Address", style = MaterialTheme.typography.bodySmall, color = WarmTextMuted)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = student.address, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarmTextDark)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // WhatsApp Fee Due Alert & Action
                if (student.dueAmount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F8EE)),
                        border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "WhatsApp",
                                        tint = Color(0xFF128C7E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Fee Due Reminder (${student.feeDueDate})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF128C7E)
                                    )
                                }
                                Text(
                                    text = "Due: ₹${student.dueAmount}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = DangerRed
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (!isWhatsAppUnlocked) {
                                        onDismiss()
                                        viewModel.requestUpgrade("whatsapp_fee_reminders")
                                    } else {
                                        viewModel.sendStudentWhatsAppReminder(context, student)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF128C7E))
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isWhatsAppUnlocked) "Send Fee Due Alert on WhatsApp" else "Unlock WhatsApp Alerts (2nd/3rd Plan)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.showCollectFeeDialog(student)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Collect Fee")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.deleteStudent(student.id)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment History
                Text(text = "Payment History", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                if (studentPayments.isEmpty()) {
                    Text(text = "No payment records found.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(studentPayments) { p ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { viewModel.showActiveReceipt(p) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = p.receiptNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        Text(text = "${p.paymentDate} • via ${p.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(text = "₹${p.amount}", fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeatSelectionDialog(
    availableSeats: List<Seat>,
    onSeatSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Vacant Seat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmTextDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (availableSeats.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No vacant seats available in this branch!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DangerRed,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(availableSeats) { seat ->
                            Surface(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSeatSelected(seat.seatNumber)
                                        onDismiss()
                                    },
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = seat.seatNumber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
