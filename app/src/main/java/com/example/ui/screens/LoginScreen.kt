package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Shift
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel
import java.util.UUID

enum class AuthMode {
    LOGIN,
    REGISTER
}

enum class LoginMethod {
    PASSWORD,
    OTP
}

enum class RegSection {
    PERSONAL_DETAILS,
    LIBRARY_DETAILS,
    SHIFT_DETAILS
}

data class EditableShiftItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startTime: String,
    val endTime: String,
    val price: Int,
    var isEnabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier,
    onOpenSuperAdmin: () -> Unit = {}
) {
    val owner by viewModel.ownerProfile.collectAsState()
    val library by viewModel.library.collectAsState()
    val lastGeneratedOtp by viewModel.lastGeneratedOtp.collectAsState()

    var currentAuthMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var loginMethod by remember { mutableStateOf(LoginMethod.PASSWORD) }

    // --- Login Fields ---
    var loginPhoneOrEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isLoginPasswordVisible by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpMessageBanner by remember { mutableStateOf<String?>(null) }

    // --- Registration Stepper & Fields ---
    var regSection by remember { mutableStateOf(RegSection.PERSONAL_DETAILS) }

    // Section 1: Personal Details
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var ownerWhatsapp by remember { mutableStateOf("") }
    var isSameWhatsapp by remember { mutableStateOf(true) }
    var ownerEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }
    var isRegConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Section 2: Library Details
    var libraryName by remember { mutableStateOf("") }
    var libraryContactNumber by remember { mutableStateOf("") }
    var isSameLibraryContact by remember { mutableStateOf(true) }
    var libraryEmail by remember { mutableStateOf("") }
    var isSameLibraryEmail by remember { mutableStateOf(true) }
    var libraryAddress by remember { mutableStateOf("") }
    var libraryLocation by remember { mutableStateOf("") }
    var libraryPincode by remember { mutableStateOf("") }
    var libraryCity by remember { mutableStateOf("") }
    var libraryState by remember { mutableStateOf("") }
    var seatsCapacity by remember { mutableStateOf(60) }
    var customSeatInput by remember { mutableStateOf("60") }

    // Section 3: Library Shifts
    val defaultShifts = remember {
        mutableStateListOf(
            EditableShiftItem(name = "Morning Shift", startTime = "06:00 AM", endTime = "12:00 PM", price = 600, isEnabled = true),
            EditableShiftItem(name = "Evening Shift", startTime = "05:00 PM", endTime = "11:00 PM", price = 650, isEnabled = true),
            EditableShiftItem(name = "Full Day (24x7)", startTime = "06:00 AM", endTime = "11:00 PM", price = 1200, isEnabled = true)
        )
    }

    var regShowAddForm by remember { mutableStateOf(false) }
    var regNewName by remember { mutableStateOf("") }
    var regNewStart by remember { mutableStateOf("") }
    var regNewEnd by remember { mutableStateOf("") }
    var regNewPrice by remember { mutableStateOf("") }

    var regEditingShiftId by remember { mutableStateOf<String?>(null) }
    var regEditStart by remember { mutableStateOf("") }
    var regEditEnd by remember { mutableStateOf("") }
    var regEditPrice by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Brand Header
            Box(
                modifier = Modifier
                    .height(130.dp)
                    .width(180.dp)
                    .clipToBounds(),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    painter = painterResource(id = com.example.R.drawable.logo_vidyara),
                    contentDescription = "Vidyara Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Auth Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_main_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE4))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Auth Mode Switcher (Login vs Register)
                    Surface(
                        color = Color(0xFFFBF8F4),
                        shape = RoundedCornerShape(14.dp),
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
                                    .clickable {
                                        currentAuthMode = AuthMode.LOGIN
                                        validationError = null
                                    },
                                color = if (currentAuthMode == AuthMode.LOGIN) OrangePrimary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Login,
                                            contentDescription = null,
                                            tint = if (currentAuthMode == AuthMode.LOGIN) PureWhite else WarmTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Existing User (Login)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (currentAuthMode == AuthMode.LOGIN) PureWhite else WarmTextMuted
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        currentAuthMode = AuthMode.REGISTER
                                        validationError = null
                                    },
                                color = if (currentAuthMode == AuthMode.REGISTER) OrangePrimary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = if (currentAuthMode == AuthMode.REGISTER) PureWhite else WarmTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "New User (Register)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (currentAuthMode == AuthMode.REGISTER) PureWhite else WarmTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (validationError != null) {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = validationError ?: "",
                                    color = DangerRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // -------------------------------------------------------------
                    // 1. LOGIN MODE (Password or OTP)
                    // -------------------------------------------------------------
                    if (currentAuthMode == AuthMode.LOGIN) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Welcome Back!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = WarmTextDark
                            )
                            Text(
                                text = "Sign in to manage your library seats & students",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmTextMuted
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Phone / Email Field
                            OutlinedTextField(
                                value = loginPhoneOrEmail,
                                onValueChange = { loginPhoneOrEmail = it },
                                label = { Text("Phone Number or Email *") },
                                placeholder = { Text("+91 9876543210") },
                                textStyle = AppInputTextStyle,
                                colors = appOutlinedTextFieldColors(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = OrangePrimary
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_phone_input"),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Login Method Selector: Password vs OTP
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = loginMethod == LoginMethod.PASSWORD,
                                    onClick = {
                                        loginMethod = LoginMethod.PASSWORD
                                        validationError = null
                                    },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Login with Password", fontSize = 12.sp)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OrangePrimaryContainer,
                                        selectedLabelColor = OrangePrimaryDark,
                                        containerColor = PureWhite
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = loginMethod == LoginMethod.PASSWORD,
                                        borderColor = if (loginMethod == LoginMethod.PASSWORD) OrangePrimary else Color(0xFFE5DECE)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                FilterChip(
                                    selected = loginMethod == LoginMethod.OTP,
                                    onClick = {
                                        loginMethod = LoginMethod.OTP
                                        validationError = null
                                    },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Login with OTP", fontSize = 12.sp)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OrangePrimaryContainer,
                                        selectedLabelColor = OrangePrimaryDark,
                                        containerColor = PureWhite
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = loginMethod == LoginMethod.OTP,
                                        borderColor = if (loginMethod == LoginMethod.OTP) OrangePrimary else Color(0xFFE5DECE)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Method 1: Password Login
                            if (loginMethod == LoginMethod.PASSWORD) {
                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it },
                                    label = { Text("Password *") },
                                    textStyle = AppInputTextStyle,
                                    colors = appOutlinedTextFieldColors(),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = OrangePrimary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { isLoginPasswordVisible = !isLoginPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isLoginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password"
                                            )
                                        }
                                    },
                                    visualTransformation = if (isLoginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_input"),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (loginPhoneOrEmail.isNotBlank()) {
                                                viewModel.loginWithPassword(loginPhoneOrEmail, loginPassword)
                                            } else {
                                                validationError = "Please enter your Phone Number or Email."
                                            }
                                        }
                                    )
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        if (loginPhoneOrEmail.isBlank()) {
                                            validationError = "Please enter your Phone Number or Email."
                                        } else {
                                            viewModel.loginWithPassword(loginPhoneOrEmail, loginPassword)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("login_submit_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OrangePrimary,
                                        contentColor = PureWhite
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.Login, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sign In with Password",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                // Method 2: OTP Login
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    if (!isOtpSent) {
                                        Button(
                                            onClick = {
                                                if (loginPhoneOrEmail.isBlank()) {
                                                    validationError = "Please enter your phone number first."
                                                } else {
                                                    val generatedCode = viewModel.sendOtp(loginPhoneOrEmail)
                                                    isOtpSent = true
                                                    otpMessageBanner = "Verification code sent to $loginPhoneOrEmail: $generatedCode"
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("send_otp_button"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = OrangePrimary,
                                                contentColor = PureWhite
                                            )
                                        ) {
                                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Get Verification OTP", fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        // OTP Banner notification
                                        Surface(
                                            color = Color(0xFFECFDF5),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "SMS OTP Dispatched",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF065F46)
                                                    )
                                                    Text(
                                                        text = otpMessageBanner ?: "OTP sent to $loginPhoneOrEmail",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF047857)
                                                    )
                                                }

                                                if (lastGeneratedOtp != null) {
                                                    Button(
                                                        onClick = {
                                                            enteredOtp = lastGeneratedOtp ?: ""
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("Auto-fill", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        OutlinedTextField(
                                            value = enteredOtp,
                                            onValueChange = { if (it.length <= 6) enteredOtp = it },
                                            label = { Text("Enter 4-Digit OTP *") },
                                            placeholder = { Text("e.g. 4829") },
                                            textStyle = AppInputTextStyle,
                                            colors = appOutlinedTextFieldColors(),
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Pin,
                                                    contentDescription = null,
                                                    tint = OrangePrimary
                                                )
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("otp_input_field"),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onDone = {
                                                    if (enteredOtp.isNotBlank()) {
                                                        val ok = viewModel.verifyOtpAndLogin(loginPhoneOrEmail, enteredOtp)
                                                        if (!ok) validationError = "Invalid OTP. Please check the code."
                                                    }
                                                }
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Didn't receive OTP?",
                                                fontSize = 12.sp,
                                                color = WarmTextMuted
                                            )
                                            TextButton(
                                                onClick = {
                                                    val generatedCode = viewModel.sendOtp(loginPhoneOrEmail)
                                                    otpMessageBanner = "New OTP sent to $loginPhoneOrEmail: $generatedCode"
                                                }
                                            ) {
                                                Text("Resend Code", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = {
                                                if (enteredOtp.isBlank()) {
                                                    validationError = "Please enter the OTP."
                                                } else {
                                                    val ok = viewModel.verifyOtpAndLogin(loginPhoneOrEmail, enteredOtp)
                                                    if (!ok) validationError = "Invalid OTP code."
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("verify_otp_button"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = OrangePrimary,
                                                contentColor = PureWhite
                                            )
                                        ) {
                                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Verify OTP & Sign In", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenSuperAdmin() }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = WarmTextMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Platform Owner Access", fontSize = 12.sp, color = WarmTextMuted, fontWeight = FontWeight.Bold)
                        }
                    }

                    // -------------------------------------------------------------
                    // 2. REGISTER MODE (3 Structured Sections)
                    // -------------------------------------------------------------
                    else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Register New Library",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = WarmTextDark
                            )
                            Text(
                                text = "Fill in the details to set up your smart library dashboard",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmTextMuted
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 3-Section Stepper / Progress Tabs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                RegSectionHeaderPill(
                                    stepNumber = "1",
                                    title = "Personal",
                                    isActive = regSection == RegSection.PERSONAL_DETAILS,
                                    isDone = regSection == RegSection.LIBRARY_DETAILS || regSection == RegSection.SHIFT_DETAILS,
                                    onClick = { regSection = RegSection.PERSONAL_DETAILS },
                                    modifier = Modifier.weight(1f)
                                )

                                RegSectionHeaderPill(
                                    stepNumber = "2",
                                    title = "Library",
                                    isActive = regSection == RegSection.LIBRARY_DETAILS,
                                    isDone = regSection == RegSection.SHIFT_DETAILS,
                                    onClick = {
                                        if (validatePersonalDetails(ownerName, ownerPhone, ownerEmail, regPassword, regConfirmPassword) == null) {
                                            regSection = RegSection.LIBRARY_DETAILS
                                            validationError = null
                                        } else {
                                            validationError = validatePersonalDetails(ownerName, ownerPhone, ownerEmail, regPassword, regConfirmPassword)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                RegSectionHeaderPill(
                                    stepNumber = "3",
                                    title = "Shifts",
                                    isActive = regSection == RegSection.SHIFT_DETAILS,
                                    isDone = false,
                                    onClick = {
                                        val pErr = validatePersonalDetails(ownerName, ownerPhone, ownerEmail, regPassword, regConfirmPassword)
                                        val lErr = validateLibraryDetails(libraryName, libraryAddress, libraryLocation)
                                        if (pErr != null) {
                                            validationError = pErr
                                            regSection = RegSection.PERSONAL_DETAILS
                                        } else if (lErr != null) {
                                            validationError = lErr
                                            regSection = RegSection.LIBRARY_DETAILS
                                        } else {
                                            regSection = RegSection.SHIFT_DETAILS
                                            validationError = null
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // --- SECTION 1: PERSONAL DETAILS ---
                            AnimatedVisibility(visible = regSection == RegSection.PERSONAL_DETAILS) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    SectionTitleCard(
                                        icon = Icons.Default.Person,
                                        title = "1. Personal Details",
                                        subtitle = "Owner name, contact & login security"
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = ownerName,
                                        onValueChange = { ownerName = it },
                                        label = { Text("Owner Full Name *") },
                                        placeholder = { Text("e.g. Ratnesh Ankit") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = ownerPhone,
                                        onValueChange = {
                                            ownerPhone = it
                                            if (isSameWhatsapp) ownerWhatsapp = it
                                            if (isSameLibraryContact) libraryContactNumber = it
                                        },
                                        label = { Text("Phone Number *") },
                                        placeholder = { Text("+91 9876543210") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = OrangePrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Same as phone toggle
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                isSameWhatsapp = !isSameWhatsapp
                                                if (isSameWhatsapp) ownerWhatsapp = ownerPhone
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSameWhatsapp,
                                            onCheckedChange = {
                                                isSameWhatsapp = it
                                                if (it) ownerWhatsapp = ownerPhone
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                                        )
                                        Text(
                                            text = "WhatsApp Number is same as Phone Number",
                                            fontSize = 12.sp,
                                            color = WarmTextDark
                                        )
                                    }

                                    if (!isSameWhatsapp) {
                                        OutlinedTextField(
                                            value = ownerWhatsapp,
                                            onValueChange = { ownerWhatsapp = it },
                                            label = { Text("WhatsApp Phone Number *") },
                                            placeholder = { Text("+91 9876543210") },
                                            textStyle = AppInputTextStyle,
                                            colors = appOutlinedTextFieldColors(),
                                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = OrangePrimary) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    OutlinedTextField(
                                        value = ownerEmail,
                                        onValueChange = {
                                            ownerEmail = it
                                            if (isSameLibraryEmail) libraryEmail = it
                                        },
                                        label = { Text("Email Address *") },
                                        placeholder = { Text("owner@library.com") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OrangePrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = regPassword,
                                        onValueChange = { regPassword = it },
                                        label = { Text("Password *") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OrangePrimary) },
                                        trailingIcon = {
                                            IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isRegPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = regConfirmPassword,
                                        onValueChange = { regConfirmPassword = it },
                                        label = { Text("Confirm Password *") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = OrangePrimary) },
                                        trailingIcon = {
                                            IconButton(onClick = { isRegConfirmPasswordVisible = !isRegConfirmPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isRegConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        visualTransformation = if (isRegConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Button(
                                        onClick = {
                                            val err = validatePersonalDetails(ownerName, ownerPhone, ownerEmail, regPassword, regConfirmPassword)
                                            if (err != null) {
                                                validationError = err
                                            } else {
                                                validationError = null
                                                if (libraryContactNumber.isBlank()) libraryContactNumber = ownerPhone
                                                if (libraryEmail.isBlank()) libraryEmail = ownerEmail
                                                regSection = RegSection.LIBRARY_DETAILS
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = PureWhite)
                                    ) {
                                        Text("Continue to Library Details", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                                    }
                                }
                            }

                            // --- SECTION 2: LIBRARY DETAILS ---
                            AnimatedVisibility(visible = regSection == RegSection.LIBRARY_DETAILS) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    SectionTitleCard(
                                        icon = Icons.Default.Storefront,
                                        title = "2. Library Details",
                                        subtitle = "Library name, address, location & seats capacity"
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = libraryName,
                                        onValueChange = { libraryName = it },
                                        label = { Text("Library Name *") },
                                        placeholder = { Text("e.g. Saraswati Study Point & Library") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        leadingIcon = { Icon(Icons.Default.LocalLibrary, contentDescription = null, tint = OrangePrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = libraryContactNumber,
                                        onValueChange = { libraryContactNumber = it },
                                        label = { Text("Contact Number *") },
                                        placeholder = { Text("+91 9876543210") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = OrangePrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = libraryEmail,
                                        onValueChange = { libraryEmail = it },
                                        label = { Text("Library Official Email *") },
                                        placeholder = { Text("contact@mylibrary.com") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OrangePrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                    )

                                    LaunchedEffect(libraryPincode) {
                                        val cleanPin = libraryPincode.trim().filter { it.isDigit() }
                                        if (cleanPin.length == 6) {
                                            viewModel.lookupPincode(cleanPin) { detectedCity, detectedState ->
                                                libraryCity = detectedCity
                                                libraryState = detectedState
                                                if (libraryLocation.isBlank()) {
                                                    libraryLocation = "Near $detectedCity"
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = libraryAddress,
                                        onValueChange = { libraryAddress = it },
                                        label = { Text("Street Address / Building *") },
                                        placeholder = { Text("e.g. Plot 42, Knowledge Park III") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = OrangePrimary) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = libraryPincode,
                                            onValueChange = { libraryPincode = it },
                                            label = { Text("Pincode (6 digits)") },
                                            placeholder = { Text("e.g. 201310") },
                                            textStyle = AppInputTextStyle,
                                            colors = appOutlinedTextFieldColors(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )

                                        OutlinedTextField(
                                            value = libraryCity,
                                            onValueChange = { libraryCity = it },
                                            label = { Text("City") },
                                            placeholder = { Text("e.g. Greater Noida") },
                                            textStyle = AppInputTextStyle,
                                            colors = appOutlinedTextFieldColors(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = libraryState,
                                        onValueChange = { libraryState = it },
                                        label = { Text("State") },
                                        placeholder = { Text("e.g. Uttar Pradesh") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = libraryLocation,
                                        onValueChange = { libraryLocation = it },
                                        label = { Text("Landmark / Location Detail *") },
                                        placeholder = { Text("e.g. Near Knowledge Park Metro") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = OrangePrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Seats Capacity Selection
                                    Text(
                                        text = "Seats Capacity: $seatsCapacity Seats",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WarmTextDark
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(40, 60, 80, 100, 150).forEach { cap ->
                                            FilterChip(
                                                selected = seatsCapacity == cap,
                                                onClick = {
                                                    seatsCapacity = cap
                                                    customSeatInput = cap.toString()
                                                },
                                                label = { Text("$cap", fontSize = 11.sp, fontWeight = if (seatsCapacity == cap) FontWeight.Bold else FontWeight.Normal) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = OrangePrimaryContainer,
                                                    selectedLabelColor = OrangePrimaryDark,
                                                    containerColor = PureWhite
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    enabled = true,
                                                    selected = seatsCapacity == cap,
                                                    borderColor = if (seatsCapacity == cap) OrangePrimary else Color(0xFFE5DECE)
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = customSeatInput,
                                        onValueChange = {
                                            customSeatInput = it
                                            val parsed = it.toIntOrNull()
                                            if (parsed != null && parsed in 5..500) {
                                                seatsCapacity = parsed
                                            }
                                        },
                                        label = { Text("Or Custom Seat Count") },
                                        textStyle = AppInputTextStyle,
                                        colors = appOutlinedTextFieldColors(),
                                        leadingIcon = { Icon(Icons.Default.Chair, contentDescription = null, tint = OrangePrimary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { regSection = RegSection.PERSONAL_DETAILS },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(50.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCCFC0))
                                        ) {
                                            Text("Back", color = WarmTextDark)
                                        }

                                        Button(
                                            onClick = {
                                                val err = validateLibraryDetails(libraryName, libraryAddress, libraryLocation)
                                                if (err != null) {
                                                    validationError = err
                                                } else {
                                                    validationError = null
                                                    regSection = RegSection.SHIFT_DETAILS
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1.5f)
                                                .height(50.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = PureWhite)
                                        ) {
                                            Text("Continue to Shifts", fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                                        }
                                    }
                                }
                            }

                            // --- SECTION 3: LIBRARY SHIFT DETAILS ---
                            AnimatedVisibility(visible = regSection == RegSection.SHIFT_DETAILS) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    SectionTitleCard(
                                        icon = Icons.Default.Schedule,
                                        title = "3. Library Shift Details",
                                        subtitle = "Configure shifts, timings & monthly pricing"
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                     Spacer(modifier = Modifier.height(12.dp))

                                     // Inline Custom Shift Creator
                                     if (regShowAddForm) {
                                         Card(
                                             modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                             shape = RoundedCornerShape(12.dp),
                                             colors = CardDefaults.cardColors(containerColor = SlateBackground.copy(alpha = 0.5f)),
                                             border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f))
                                         ) {
                                             Column(modifier = Modifier.padding(12.dp)) {
                                                 Text("Create Custom Shift", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmTextDark)
                                                 Spacer(modifier = Modifier.height(8.dp))
                                                 OutlinedTextField(
                                                     value = regNewName,
                                                     onValueChange = { regNewName = it },
                                                     label = { Text("Shift Name") },
                                                     singleLine = true,
                                                     textStyle = AppInputTextStyle,
                                                     colors = appOutlinedTextFieldColors(),
                                                     modifier = Modifier.fillMaxWidth()
                                                 )
                                                 Spacer(modifier = Modifier.height(6.dp))
                                                 Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                     OutlinedTextField(
                                                         value = regNewStart,
                                                         onValueChange = { regNewStart = it },
                                                         label = { Text("Start Time") },
                                                         singleLine = true,
                                                         textStyle = AppInputTextStyle,
                                                         colors = appOutlinedTextFieldColors(),
                                                         modifier = Modifier.weight(1f)
                                                     )
                                                     OutlinedTextField(
                                                         value = regNewEnd,
                                                         onValueChange = { regNewEnd = it },
                                                         label = { Text("End Time") },
                                                         singleLine = true,
                                                         textStyle = AppInputTextStyle,
                                                         colors = appOutlinedTextFieldColors(),
                                                         modifier = Modifier.weight(1f)
                                                     )
                                                 }
                                                 Spacer(modifier = Modifier.height(6.dp))
                                                 OutlinedTextField(
                                                     value = regNewPrice,
                                                     onValueChange = { regNewPrice = it },
                                                     label = { Text("Monthly Fee (₹)") },
                                                     singleLine = true,
                                                     textStyle = AppInputTextStyle,
                                                     colors = appOutlinedTextFieldColors(),
                                                     modifier = Modifier.fillMaxWidth(),
                                                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                                 )
                                                 Spacer(modifier = Modifier.height(10.dp))
                                                 Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                     OutlinedButton(
                                                         onClick = { regShowAddForm = false },
                                                         modifier = Modifier.weight(1f),
                                                         shape = RoundedCornerShape(8.dp)
                                                     ) {
                                                         Text("Cancel", color = WarmTextDark)
                                                     }
                                                     Button(
                                                         onClick = {
                                                             val priceVal = regNewPrice.toIntOrNull() ?: 0
                                                             if (regNewName.isNotBlank() && regNewStart.isNotBlank() && regNewEnd.isNotBlank() && priceVal > 0) {
                                                                 defaultShifts.add(
                                                                     EditableShiftItem(
                                                                         name = regNewName,
                                                                         startTime = regNewStart,
                                                                         endTime = regNewEnd,
                                                                         price = priceVal,
                                                                         isEnabled = true
                                                                     )
                                                                 )
                                                                 regNewName = ""
                                                                 regNewStart = ""
                                                                 regNewEnd = ""
                                                                 regNewPrice = ""
                                                                 regShowAddForm = false
                                                             }
                                                         },
                                                         modifier = Modifier.weight(1.2f),
                                                         colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                                         shape = RoundedCornerShape(8.dp)
                                                     ) {
                                                         Text("Add Shift", color = PureWhite)
                                                     }
                                                 }
                                             }
                                         }
                                     } else {
                                         Button(
                                             onClick = { regShowAddForm = true },
                                             modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                             colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                             shape = RoundedCornerShape(12.dp)
                                         ) {
                                             Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                             Spacer(modifier = Modifier.width(6.dp))
                                             Text("Create Custom Shift", fontWeight = FontWeight.Bold)
                                         }
                                     }

                                     Spacer(modifier = Modifier.height(8.dp))

                                     defaultShifts.forEachIndexed { index, shiftItem ->
                                         val isEditing = regEditingShiftId == shiftItem.id
                                         Surface(
                                             modifier = Modifier
                                                 .fillMaxWidth()
                                                 .padding(vertical = 4.dp),
                                             shape = RoundedCornerShape(12.dp),
                                             color = if (shiftItem.isEnabled) OrangePrimaryContainer.copy(alpha = 0.4f) else PureWhite,
                                             border = androidx.compose.foundation.BorderStroke(
                                                 1.dp,
                                                 if (shiftItem.isEnabled) OrangePrimary.copy(alpha = 0.5f) else Color(0xFFE5DECE)
                                             )
                                         ) {
                                             Column(modifier = Modifier.padding(12.dp)) {
                                                 if (isEditing) {
                                                     Text("Edit Shift: ${shiftItem.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OrangePrimaryDark)
                                                     Spacer(modifier = Modifier.height(8.dp))
                                                     Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                         OutlinedTextField(
                                                             value = regEditStart,
                                                             onValueChange = { regEditStart = it },
                                                             label = { Text("Start Time") },
                                                             singleLine = true,
                                                             textStyle = AppInputTextStyle,
                                                             colors = appOutlinedTextFieldColors(),
                                                             modifier = Modifier.weight(1f)
                                                         )
                                                         OutlinedTextField(
                                                             value = regEditEnd,
                                                             onValueChange = { regEditEnd = it },
                                                             label = { Text("End Time") },
                                                             singleLine = true,
                                                             textStyle = AppInputTextStyle,
                                                             colors = appOutlinedTextFieldColors(),
                                                             modifier = Modifier.weight(1f)
                                                         )
                                                     }
                                                     Spacer(modifier = Modifier.height(6.dp))
                                                     OutlinedTextField(
                                                         value = regEditPrice,
                                                         onValueChange = { regEditPrice = it },
                                                         label = { Text("Monthly Fee (₹)") },
                                                         singleLine = true,
                                                         textStyle = AppInputTextStyle,
                                                         colors = appOutlinedTextFieldColors(),
                                                         modifier = Modifier.fillMaxWidth(),
                                                         keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                                     )
                                                     Spacer(modifier = Modifier.height(8.dp))
                                                     Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                         OutlinedButton(
                                                             onClick = { regEditingShiftId = null },
                                                             modifier = Modifier.weight(1f),
                                                             shape = RoundedCornerShape(8.dp)
                                                         ) {
                                                             Text("Cancel", color = WarmTextDark)
                                                         }
                                                         Button(
                                                             onClick = {
                                                                 val priceVal = regEditPrice.toIntOrNull() ?: shiftItem.price
                                                                 defaultShifts[index] = shiftItem.copy(
                                                                     startTime = regEditStart,
                                                                     endTime = regEditEnd,
                                                                     price = priceVal
                                                                 )
                                                                 regEditingShiftId = null
                                                             },
                                                             modifier = Modifier.weight(1.2f),
                                                             colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                             shape = RoundedCornerShape(8.dp)
                                                         ) {
                                                             Text("Save", color = PureWhite)
                                                         }
                                                     }
                                                 } else {
                                                     Row(
                                                         modifier = Modifier.fillMaxWidth(),
                                                         verticalAlignment = Alignment.CenterVertically,
                                                         horizontalArrangement = Arrangement.SpaceBetween
                                                     ) {
                                                         Row(
                                                             verticalAlignment = Alignment.CenterVertically,
                                                             modifier = Modifier.weight(1f)
                                                         ) {
                                                             Switch(
                                                                 checked = shiftItem.isEnabled,
                                                                 onCheckedChange = { defaultShifts[index] = shiftItem.copy(isEnabled = it) },
                                                                 colors = SwitchDefaults.colors(
                                                                     checkedThumbColor = PureWhite,
                                                                     checkedTrackColor = OrangePrimary
                                                                 )
                                                             )
                                                             Spacer(modifier = Modifier.width(10.dp))
                                                             Column {
                                                                 Text(
                                                                     text = shiftItem.name,
                                                                     fontWeight = FontWeight.Bold,
                                                                     fontSize = 13.sp,
                                                                     color = WarmTextDark
                                                                 )
                                                                 Text(
                                                                     text = "${shiftItem.startTime} - ${shiftItem.endTime}",
                                                                     fontSize = 11.sp,
                                                                     color = WarmTextMuted
                                                                 )
                                                             }
                                                         }

                                                         Row(verticalAlignment = Alignment.CenterVertically) {
                                                             Surface(
                                                                 color = PureWhite,
                                                                 shape = RoundedCornerShape(8.dp),
                                                                 border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE0D5C5))
                                                             ) {
                                                                 Text(
                                                                     text = "₹${shiftItem.price}/mo",
                                                                     fontSize = 11.sp,
                                                                     fontWeight = FontWeight.ExtraBold,
                                                                     color = OrangePrimaryDark,
                                                                     modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                                 )
                                                             }
                                                             Spacer(modifier = Modifier.width(6.dp))
                                                             IconButton(
                                                                 onClick = {
                                                                     regEditingShiftId = shiftItem.id
                                                                     regEditStart = shiftItem.startTime
                                                                     regEditEnd = shiftItem.endTime
                                                                     regEditPrice = shiftItem.price.toString()
                                                                 },
                                                                 modifier = Modifier.size(28.dp)
                                                             ) {
                                                                 Icon(
                                                                     imageVector = Icons.Default.Edit,
                                                                     contentDescription = "Edit shift",
                                                                     tint = WarmTextDark,
                                                                     modifier = Modifier.size(16.dp)
                                                                 )
                                                             }
                                                         }
                                                     }
                                                 }
                                             }
                                         }
                                     }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { regSection = RegSection.LIBRARY_DETAILS },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(50.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCCFC0))
                                        ) {
                                            Text("Back", color = WarmTextDark)
                                        }

                                        Button(
                                            onClick = {
                                                // Convert active shifts to Shift data models
                                                val configuredShifts = defaultShifts
                                                    .filter { it.isEnabled }
                                                    .map {
                                                        Shift(
                                                            name = it.name,
                                                            startTime = it.startTime,
                                                            endTime = it.endTime,
                                                            defaultPrice = it.price,
                                                            capacity = seatsCapacity
                                                        )
                                                    }
                                                    .ifEmpty {
                                                        listOf(
                                                            Shift(name = "Full Day (24x7)", startTime = "06:00 AM", endTime = "11:00 PM", defaultPrice = 1200, capacity = seatsCapacity)
                                                        )
                                                    }

                                                viewModel.registerFullLibrary(
                                                    ownerName = ownerName.ifBlank { "Library Owner" },
                                                    phone = ownerPhone.ifBlank { "+91 9876543210" },
                                                    whatsapp = ownerWhatsapp.ifBlank { ownerPhone },
                                                    email = ownerEmail.ifBlank { "admin@library.com" },
                                                    password = regPassword.ifBlank { "admin123" },
                                                    libraryName = libraryName.ifBlank { "My Study Point & Library" },
                                                    contactNumber = libraryContactNumber.ifBlank { ownerPhone },
                                                    libraryEmail = libraryEmail.ifBlank { ownerEmail },
                                                    address = libraryAddress.ifBlank { "Plot 1, Main Road" },
                                                    location = libraryLocation.ifBlank { "City Center" },
                                                    city = libraryCity,
                                                    state = libraryState,
                                                    pincode = libraryPincode,
                                                    seatCapacity = seatsCapacity,
                                                    shifts = configuredShifts
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1.8f)
                                                .height(50.dp)
                                                .testTag("complete_registration_button"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = OrangePrimary,
                                                contentColor = PureWhite
                                            )
                                        ) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Complete & Launch", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = WarmTextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "End-to-End Encrypted Cloud Attendance & Library Data",
                    fontSize = 11.sp,
                    color = WarmTextMuted
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RegSectionHeaderPill(
    stepNumber: String,
    title: String,
    isActive: Boolean,
    isDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if (isActive) OrangePrimaryContainer else if (isDone) Color(0xFFECFDF5) else PureWhite,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) OrangePrimary else if (isDone) Color(0xFF10B981) else Color(0xFFE5DECE)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) OrangePrimary
                        else if (isDone) Color(0xFF10B981)
                        else Color(0xFFD4C8B8)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(12.dp))
                } else {
                    Text(text = stepNumber, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isActive) OrangePrimaryDark else if (isDone) Color(0xFF065F46) else WarmTextMuted
            )
        }
    }
}

@Composable
private fun SectionTitleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        color = Color(0xFFFDFBF7),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1EAE0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangePrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmTextDark)
                Text(text = subtitle, fontSize = 11.sp, color = WarmTextMuted)
            }
        }
    }
}

private fun validatePersonalDetails(name: String, phone: String, email: String, pass: String, confirmPass: String): String? {
    if (name.isBlank()) return "Please enter Owner Full Name."
    if (phone.isBlank()) return "Please enter Owner Phone Number."
    if (email.isBlank() || !email.contains("@")) return "Please enter a valid Email Address."
    if (pass.length < 4) return "Password must be at least 4 characters long."
    if (pass != confirmPass) return "Passwords do not match."
    return null
}

private fun validateLibraryDetails(name: String, address: String, location: String): String? {
    if (name.isBlank()) return "Please enter Library Name."
    if (address.isBlank()) return "Please enter Library Address."
    if (location.isBlank()) return "Please enter Library Location / City."
    return null
}
