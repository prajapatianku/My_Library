package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val students by viewModel.students.collectAsState()
    val attendanceRecords by viewModel.attendance.collectAsState()
    val shifts by viewModel.shifts.collectAsState()
    val selectedShift by viewModel.attendanceShiftFilter.collectAsState()
    val selectedDateIso by viewModel.selectedAttendanceDate.collectAsState()

    val isoDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }
    val shortDateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val selectedDateObj: Date = remember(selectedDateIso) {
        try {
            isoDateFormat.parse(selectedDateIso) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    val formattedDateText = remember(selectedDateObj) {
        displayDateFormat.format(selectedDateObj)
    }

    val daysAgo = remember(selectedDateIso) {
        viewModel.getAttendanceDaysAgo(selectedDateIso)
    }

    val isEditable = remember(selectedDateIso) {
        viewModel.isAttendanceDateEditable(selectedDateIso)
    }

    val selectedDateRecords = attendanceRecords.filter { it.attendanceDate == selectedDateIso }
    val presentCount = selectedDateRecords.count { it.status == AttendanceStatus.PRESENT }
    val absentCount = selectedDateRecords.count { it.status == AttendanceStatus.ABSENT }
    val totalStudents = students.size
    val attendancePct = if (totalStudents > 0) (presentCount * 100) / totalStudents else 0

    val shiftFilterOptions = listOf("All") + shifts.map { it.name }

    val filteredStudents = if (selectedShift == "All") {
        students
    } else {
        students.filter { it.assignedShiftName.contains(selectedShift, ignoreCase = true) }
    }

    // Quick date generator for helper chips
    val cal = Calendar.getInstance()
    val todayIso = isoDateFormat.format(cal.time)
    cal.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayIso = isoDateFormat.format(cal.time)
    cal.add(Calendar.DAY_OF_YEAR, -1)
    val dayBeforeIso = isoDateFormat.format(cal.time)

    // Function to launch native Android DatePicker
    val openNativeDatePicker = {
        val pickerCal = Calendar.getInstance()
        pickerCal.time = selectedDateObj
        val dpd = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, dayOfMonth)
                viewModel.setSelectedAttendanceDate(isoDateFormat.format(newCal.time))
            },
            pickerCal.get(Calendar.YEAR),
            pickerCal.get(Calendar.MONTH),
            pickerCal.get(Calendar.DAY_OF_MONTH)
        )
        // Prevent picking future dates
        dpd.datePicker.maxDate = System.currentTimeMillis()
        dpd.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        // Top Header & Date Navigation Card
        Surface(
            color = PureWhite,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Screen Title & Bulk Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Attendance Register",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarmTextDark
                        )
                        Text(
                            text = if (isEditable) "Mark and review student presence" else "Historical attendance archive (Read-Only)",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmTextMuted
                        )
                    }

                    if (isEditable) {
                        Button(
                            onClick = { viewModel.markAllPresent(selectedDateIso) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All Present", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            color = Color(0xFFF5F2EC),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = WarmTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Locked",
                                    fontSize = 11.sp,
                                    color = WarmTextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Date Navigator Box
                Surface(
                    color = Color(0xFFFBF9F6),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEFE8DE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { viewModel.goToPreviousAttendanceDay() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Day",
                                tint = OrangePrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { openNativeDatePicker() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Pick Date",
                                tint = OrangePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formattedDateText,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmTextDark
                                )
                                val statusLabel = when (daysAgo) {
                                    0 -> "Today • Editable"
                                    1 -> "Yesterday • Editable"
                                    2 -> "2 Days Ago • Editable"
                                    else -> if (daysAgo > 2) "$daysAgo Days Ago • Locked" else "Future • Locked"
                                }
                                Text(
                                    text = statusLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isEditable) Color(0xFF16A34A) else DangerRed
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.goToNextAttendanceDay() },
                            enabled = daysAgo > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Day",
                                tint = if (daysAgo > 0) OrangePrimary else Color(0xFFD1C7BA)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Date Jump Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedDateIso == todayIso,
                            onClick = { viewModel.setSelectedAttendanceDate(todayIso) },
                            label = { Text("Today", fontWeight = if (selectedDateIso == todayIso) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimaryContainer,
                                selectedLabelColor = OrangePrimaryDark,
                                containerColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedDateIso == todayIso,
                                borderColor = if (selectedDateIso == todayIso) OrangePrimary else Color(0xFFE5DECE)
                            ),
                            leadingIcon = if (selectedDateIso == todayIso) {
                                { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = OrangePrimaryDark) }
                            } else null
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedDateIso == yesterdayIso,
                            onClick = { viewModel.setSelectedAttendanceDate(yesterdayIso) },
                            label = { Text("Yesterday", fontWeight = if (selectedDateIso == yesterdayIso) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimaryContainer,
                                selectedLabelColor = OrangePrimaryDark,
                                containerColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedDateIso == yesterdayIso,
                                borderColor = if (selectedDateIso == yesterdayIso) OrangePrimary else Color(0xFFE5DECE)
                            ),
                            leadingIcon = if (selectedDateIso == yesterdayIso) {
                                { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = OrangePrimaryDark) }
                            } else null
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedDateIso == dayBeforeIso,
                            onClick = { viewModel.setSelectedAttendanceDate(dayBeforeIso) },
                            label = { Text("Day Before (2 Days Ago)", fontWeight = if (selectedDateIso == dayBeforeIso) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimaryContainer,
                                selectedLabelColor = OrangePrimaryDark,
                                containerColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedDateIso == dayBeforeIso,
                                borderColor = if (selectedDateIso == dayBeforeIso) OrangePrimary else Color(0xFFE5DECE)
                            ),
                            leadingIcon = if (selectedDateIso == dayBeforeIso) {
                                { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = OrangePrimaryDark) }
                            } else null
                        )
                    }
                    item {
                        OutlinedButton(
                            onClick = { openNativeDatePicker() },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DECE))
                        ) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pick Date", fontSize = 12.sp, color = WarmTextDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Editability Notice Banner if Locked
                if (!isEditable) {
                    Surface(
                        color = Color(0xFFFFF4EC),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Attendance for this past date is locked. You can edit records for today and the past 2 days (yesterday & day before).",
                                fontSize = 11.sp,
                                color = WarmTextDark,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Stats summary pills (Only Present, Absent, Total Active)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$presentCount", fontWeight = FontWeight.ExtraBold, color = Color(0xFF15803D), fontSize = 16.sp)
                            Text(text = "Present ($attendancePct%)", fontSize = 10.sp, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Surface(
                        color = DangerRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$absentCount", fontWeight = FontWeight.ExtraBold, color = DangerRed, fontSize = 16.sp)
                            Text(text = "Absent", fontSize = 10.sp, color = DangerRed, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Surface(
                        color = OrangePrimaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalStudents", fontWeight = FontWeight.ExtraBold, color = OrangePrimaryDark, fontSize = 16.sp)
                            Text(text = "Total Active", fontSize = 10.sp, color = OrangePrimaryDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Shift Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(shiftFilterOptions) { shift ->
                        FilterChip(
                            selected = selectedShift == shift,
                            onClick = { viewModel.setAttendanceShiftFilter(shift) },
                            label = { Text(shift, fontWeight = if (selectedShift == shift) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimaryContainer,
                                selectedLabelColor = OrangePrimaryDark,
                                containerColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedShift == shift,
                                borderColor = if (selectedShift == shift) OrangePrimary else Color(0xFFE5DECE)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Student Attendance List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredStudents) { student ->
                val currentRecord = selectedDateRecords.find { it.studentId == student.id }
                val currentStatus = currentRecord?.status ?: AttendanceStatus.ABSENT
                val isMarked = currentRecord != null

                AttendanceItemCard(
                    student = student,
                    currentStatus = currentStatus,
                    isMarked = isMarked,
                    isEditable = isEditable,
                    onStatusChange = { newStatus ->
                        viewModel.markAttendance(student.id, newStatus, selectedDateIso)
                    }
                )
            }
        }
    }
}

@Composable
fun AttendanceItemCard(
    student: com.example.data.model.Student,
    currentStatus: AttendanceStatus,
    isMarked: Boolean,
    isEditable: Boolean,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(OrangePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.fullName.take(2).uppercase(),
                            fontWeight = FontWeight.Black,
                            color = OrangePrimary,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = student.fullName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarmTextDark
                        )
                        Text(
                            text = "${student.assignedShiftName} • Seat ${student.assignedSeatNumber.ifBlank { "Floating" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmTextMuted
                        )
                    }
                }

                if (isMarked) {
                    Surface(
                        color = if (currentStatus == AttendanceStatus.PRESENT) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (currentStatus == AttendanceStatus.PRESENT) Color(0xFF15803D).copy(alpha = 0.3f) else DangerRed.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = if (currentStatus == AttendanceStatus.PRESENT) "Present" else "Absent",
                            color = if (currentStatus == AttendanceStatus.PRESENT) Color(0xFF15803D) else DangerRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Surface(
                        color = Color(0xFFF5F2EC),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Unmarked",
                            color = WarmTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attendance Action Row: ONLY Present & Absent
            if (isEditable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AttendanceToggleButton(
                        label = "Present",
                        icon = Icons.Default.CheckCircle,
                        isSelected = isMarked && currentStatus == AttendanceStatus.PRESENT,
                        activeColor = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onStatusChange(AttendanceStatus.PRESENT) }
                    )
                    AttendanceToggleButton(
                        label = "Absent",
                        icon = Icons.Default.Cancel,
                        isSelected = isMarked && currentStatus == AttendanceStatus.ABSENT,
                        activeColor = DangerRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onStatusChange(AttendanceStatus.ABSENT) }
                    )
                }
            } else {
                // Read-only indicator when historical record is locked
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Record",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Record Locked (Past 2-day edit window elapsed)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceToggleButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
