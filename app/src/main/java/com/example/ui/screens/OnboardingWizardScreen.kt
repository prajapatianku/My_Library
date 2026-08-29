package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun OnboardingWizardScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }
    val totalSteps = 4

    var libraryName by remember { mutableStateOf("My Library & Study Space") }
    var phone by remember { mutableStateOf("+91 9876543210") }
    var address by remember { mutableStateOf("12/4 Anand Nagar, Near Metro Station") }
    var city by remember { mutableStateOf("New Delhi") }
    var state by remember { mutableStateOf("Delhi") }
    var pincode by remember { mutableStateOf("110092") }
    var openingTime by remember { mutableStateOf("06:00 AM") }
    var closingTime by remember { mutableStateOf("11:00 PM") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Header Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Library Setup Wizard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Text(
                    text = "Step $step of $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { step / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NavyPrimary,
                trackColor = NavyPrimaryContainer
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Content
            when (step) {
                1 -> {
                    Text(
                        text = "Let's name your study center",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "This name will be displayed on student receipts, ID cards, and registration forms.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = libraryName,
                        onValueChange = { libraryName = it },
                        label = { Text("Library / Study Center Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Phone / WhatsApp *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                2 -> {
                    Text(
                        text = "Where is your library located?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Address will appear on student admission slips and receipts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                     LaunchedEffect(pincode) {
                        val cleanPin = pincode.trim().filter { it.isDigit() }
                        if (cleanPin.length == 6) {
                            viewModel.lookupPincode(cleanPin) { detectedCity, detectedState ->
                                city = detectedCity
                                state = detectedState
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Street Address / Building *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pincode,
                            onValueChange = { pincode = it },
                            label = { Text("Pincode (6 digits)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City / District") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                3 -> {
                    Text(
                        text = "Operating Hours & Shifts",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Configure your library daily timings. Students can be assigned to specific shifts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = openingTime,
                            onValueChange = { openingTime = it },
                            label = { Text("Opening Time") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = closingTime,
                            onValueChange = { closingTime = it },
                            label = { Text("Closing Time") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavyPrimaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Default Shifts Created:", fontWeight = FontWeight.Bold, color = NavyPrimary, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "• Morning Shift (06:00 AM - 12:00 PM)", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Afternoon Shift (12:00 PM - 05:00 PM)", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Evening Shift (05:00 PM - 11:00 PM)", style = MaterialTheme.typography.bodySmall)
                            Text(text = "• Full Day 24/7 Access", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                4 -> {
                    Text(
                        text = "Ready to Launch Your Library!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your library workspace is pre-populated with initial seat desks, student passes, and financial ledger.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = libraryName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = NavyPrimary)
                            Text(text = "$address, $city - $pincode", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Timings: $openingTime to $closingTime", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "✓ 60 Interactive Desks & Floor Plan configured", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                            Text(text = "✓ Digital Fee Receipts & WhatsApp messaging ready", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                            Text(text = "✓ Live Attendance register ready", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                        }
                    }
                }
            }
        }

        // Bottom Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = { step-- },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Previous")
                }
            }

            Button(
                onClick = {
                    if (step < totalSteps) {
                        step++
                    } else {
                        viewModel.completeOnboarding(
                            name = libraryName,
                            phone = phone,
                            address = address,
                            city = city,
                            state = state,
                            pincode = pincode,
                            openingTime = openingTime,
                            closingTime = closingTime
                        )
                    }
                },
                modifier = Modifier.weight(if (step > 1) 1.5f else 1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text(if (step == totalSteps) "Get Started" else "Next Step")
            }
        }
    }
}
