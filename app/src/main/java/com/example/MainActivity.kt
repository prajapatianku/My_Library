package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.data.model.SaaSPlanType
import com.example.data.model.BillingPeriod

enum class MainNavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    STUDENTS("Students", Icons.Filled.People, Icons.Outlined.People),
    ATTENDANCE("Check-In", Icons.Filled.AssignmentTurnedIn, Icons.Outlined.AssignmentTurnedIn),
    SEAT_MAP("Seats", Icons.Filled.Chair, Icons.Outlined.Chair),
    PAYMENTS("Finance", Icons.Filled.Payments, Icons.Outlined.Payments),
    REPORTS("Reports", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SETTINGS("Menu", Icons.Filled.Menu, Icons.Outlined.Menu)
}

class MainActivity : ComponentActivity(), com.razorpay.PaymentResultListener {
    private val viewModel: LibraryViewModel by lazy {
        androidx.lifecycle.ViewModelProvider(this)[LibraryViewModel::class.java]
    }

    companion object {
        var pendingUpgradePlan: SaaSPlanType? = null
        var pendingUpgradePeriod: BillingPeriod? = null
        var pendingBranchCount: Int = 1
        var isRenewalPayment: Boolean = false
        var isBranchPurchasePayment: Boolean = false
        var pendingCouponCode: String? = null
        var pendingDiscountAmount: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.razorpay.Checkout.preload(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }

    fun startSaaSPayment(
        plan: SaaSPlanType, 
        period: BillingPeriod, 
        isRenewal: Boolean = false,
        isBranchPurchase: Boolean = false,
        branchCount: Int = 1,
        couponCode: String? = null,
        discountAmount: Int = 0
    ) {
        pendingUpgradePlan = plan
        pendingUpgradePeriod = period
        isRenewalPayment = isRenewal
        isBranchPurchasePayment = isBranchPurchase
        pendingBranchCount = branchCount
        pendingCouponCode = couponCode
        pendingDiscountAmount = discountAmount

        val pricing = viewModel.platformPricing.value
        val rawBaseAmount = if (isBranchPurchase) {
            viewModel.calculateProratedBranchPrice()
        } else {
            val basePrice = when (plan) {
                SaaSPlanType.PREMIUM -> if (period == BillingPeriod.MONTHLY) pricing.proMonthlyPrice else pricing.proYearlyPrice
                SaaSPlanType.BUSINESS -> if (period == BillingPeriod.MONTHLY) pricing.businessMonthlyPrice else pricing.businessYearlyPrice
                else -> 0
            }
            if (plan == SaaSPlanType.BUSINESS && branchCount > 1) {
                val additionalCount = branchCount - 1
                val additionalBranchBasePrice = if (period == BillingPeriod.MONTHLY) pricing.additionalBranchMonthlyPrice else pricing.additionalBranchYearlyPrice
                basePrice + (additionalCount * additionalBranchBasePrice)
            } else {
                basePrice
            }
        }

        val amountInRupees = (rawBaseAmount - discountAmount).coerceAtLeast(0)

        if (amountInRupees == 0) {
            if (isBranchPurchase) {
                viewModel.purchaseAdditionalBranch(paidAmount = 0)
            } else if (isRenewal) {
                viewModel.renewSaaS(paidAmount = 0, discountAmount = discountAmount, couponCode = couponCode)
            } else {
                viewModel.upgradeSaaS(
                    planType = plan,
                    period = period,
                    allowedBranches = branchCount,
                    paidAmount = 0,
                    discountAmount = discountAmount,
                    couponCode = couponCode
                )
            }
            return
        }

        val checkout = com.razorpay.Checkout()
        checkout.setKeyID(com.example.BuildConfig.RAZORPAY_KEY_ID)

        try {
            val options = org.json.JSONObject()
            options.put("name", "Vidyara")
            val desc = if (isBranchPurchase) {
                "Add additional branch (prorated)"
            } else if (isRenewal) {
                "Renew ${plan.displayName} (${period.name.lowercase().replace("_", " ")})"
            } else {
                "SaaS ${plan.displayName} subscription (${period.name.lowercase().replace("_", " ")}) for $branchCount branch(es)"
            }
            options.put("description", desc)
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("theme.color", "#1E293B")
            options.put("currency", "INR")
            options.put("amount", (amountInRupees * 100).toString())

            val owner = viewModel.ownerProfile.value
            val prefill = org.json.JSONObject()
            prefill.put("email", owner.email.ifBlank { "owner@library.com" })
            prefill.put("contact", owner.phone.ifBlank { "9876543210" })
            options.put("prefill", prefill)

            val retryObj = org.json.JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting payment: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        val targetPlan = pendingUpgradePlan
        val targetPeriod = pendingUpgradePeriod
        val coupon = pendingCouponCode
        val discount = pendingDiscountAmount

        if (targetPlan != null && targetPeriod != null) {
            if (isBranchPurchasePayment) {
                viewModel.purchaseAdditionalBranch(razorpayPaymentId)
                Toast.makeText(this, "Branch successfully added to your subscription!", Toast.LENGTH_LONG).show()
            } else if (isRenewalPayment) {
                viewModel.renewSaaS(
                    razorpayPaymentId = razorpayPaymentId,
                    discountAmount = discount,
                    couponCode = coupon
                )
                Toast.makeText(this, "Subscription renewed successfully!", Toast.LENGTH_LONG).show()
            } else {
                viewModel.upgradeSaaS(
                    planType = targetPlan,
                    period = targetPeriod,
                    allowedBranches = pendingBranchCount,
                    razorpayPaymentId = razorpayPaymentId,
                    discountAmount = discount,
                    couponCode = coupon
                )
                Toast.makeText(this, "Payment successful! Upgraded to ${targetPlan.displayName}", Toast.LENGTH_LONG).show()
            }
        }
        pendingUpgradePlan = null
        pendingUpgradePeriod = null
        pendingBranchCount = 1
        isRenewalPayment = false
        isBranchPurchasePayment = false
        pendingCouponCode = null
        pendingDiscountAmount = 0
    }

    override fun onPaymentError(code: Int, description: String?) {
        Toast.makeText(this, "Payment cancelled / failed: $description", Toast.LENGTH_LONG).show()
        pendingUpgradePlan = null
        pendingUpgradePeriod = null
        pendingBranchCount = 1
        isRenewalPayment = false
        isBranchPurchasePayment = false
        pendingCouponCode = null
        pendingDiscountAmount = 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isOnboardingDone by viewModel.isOnboardingCompleted.collectAsState()
    val showUpgradeModal by viewModel.showUpgradeModal.collectAsState()
    val toastMessage by viewModel.uiToastMessage.collectAsState()
    val isHindi by viewModel.isHindi.collectAsState()

    // Global Modal States
    val showAddStudentDialog by viewModel.showAddStudentDialog.collectAsState()
    val selectedStudentForDetail by viewModel.selectedStudentForDetail.collectAsState()
    val showCollectFeeDialog by viewModel.showCollectFeeDialog.collectAsState()
    val showAddExpenseDialog by viewModel.showAddExpenseDialog.collectAsState()
    val activeReceipt by viewModel.activeReceipt.collectAsState()
    val selectedSeatForAction by viewModel.selectedSeatForAction.collectAsState()
    val showBranchDialog by viewModel.showBranchManagerDialog.collectAsState()
    val showQrDialog by viewModel.showQrDialog.collectAsState()
    val showWhatsAppReminderDialog by viewModel.showWhatsAppReminderDialog.collectAsState()
    val selectedStudentForWhatsAppReminder by viewModel.selectedStudentForWhatsAppReminder.collectAsState()

    val students by viewModel.students.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val activeBranchId by viewModel.activeBranchId.collectAsState()
    val library by viewModel.library.collectAsState()
    val shifts by viewModel.shifts.collectAsState()
    val ownerProfile by viewModel.ownerProfile.collectAsState()
    val isSuperAdminAuth by viewModel.isSuperAdminAuthenticated.collectAsState()

    var selectedTab by remember { mutableStateOf(MainNavigationTab.DASHBOARD) }
    var showSplash by remember { mutableStateOf(true) }
    var showSuperAdminScreen by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(isSuperAdminAuth) {
        if (isSuperAdminAuth) {
            showSuperAdminScreen = true
        }
    }

    if (showSuperAdminScreen && isSuperAdminAuth) {
        SuperAdminScreen(
            viewModel = viewModel,
            onExitToApp = { showSuperAdminScreen = false }
        )
    } else if (showSplash) {
        SplashScreen(
            onAnimationFinished = { showSplash = false }
        )
    } else if (ownerProfile.isSuspended) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SlateBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = DangerRed, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Account Deactivated", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = WarmTextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ownerProfile.suspensionReason.ifBlank { "Your library account has been temporarily deactivated by the platform administrator." },
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = WarmTextMuted
                )
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Platform Administrator Contact:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmTextDark)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("+91 8709489716", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = WarmTextDark)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ratneshankit123@gmail.com", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = WarmTextDark)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:8709489716"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Admin", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:ratneshankit123@gmail.com?subject=Account%20Reactivation%20Request"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Email Admin", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Out & Switch Account", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else if (!isLoggedIn) {
        LoginScreen(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )
    } else if (!isOnboardingDone) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            OnboardingWizardScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = PureWhite,
                    tonalElevation = 4.dp
                ) {
                    // 5 primary tabs for clean bottom navigation
                    val primaryTabs = listOf(
                        MainNavigationTab.DASHBOARD,
                        MainNavigationTab.STUDENTS,
                        MainNavigationTab.ATTENDANCE,
                        MainNavigationTab.PAYMENTS,
                        MainNavigationTab.SETTINGS
                    )

                    primaryTabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = com.example.ui.theme.translate(tab.title, isHindi),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OrangePrimaryDark,
                                selectedTextColor = OrangePrimaryDark,
                                indicatorColor = OrangePrimaryContainer,
                                unselectedIconColor = WarmTextMuted,
                                unselectedTextColor = WarmTextMuted
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    MainNavigationTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToStudents = { selectedTab = MainNavigationTab.STUDENTS },
                        onNavigateToSeats = { selectedTab = MainNavigationTab.SEAT_MAP },
                        onNavigateToAttendance = { selectedTab = MainNavigationTab.ATTENDANCE },
                        onNavigateToReports = { selectedTab = MainNavigationTab.REPORTS },
                        onNavigateToExpenses = {
                            viewModel.setFinanceSubTab(1)
                            selectedTab = MainNavigationTab.PAYMENTS
                        }
                    )
                    MainNavigationTab.STUDENTS -> StudentsScreen(viewModel = viewModel)
                    MainNavigationTab.SEAT_MAP -> SeatMapScreen(viewModel = viewModel)
                    MainNavigationTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                    MainNavigationTab.PAYMENTS -> PaymentsExpensesScreen(viewModel = viewModel)
                    MainNavigationTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                    MainNavigationTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onOpenSuperAdmin = {
                            if (isSuperAdminAuth) showSuperAdminScreen = true
                        }
                    )
                }
            }
        }
    }

    // Global Modal & Dialog Controllers (Accessible across all tabs & home quick actions)
    if (showAddStudentDialog) {
        AddStudentDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showAddStudentDialog(false) }
        )
    }

    selectedStudentForDetail?.let { student ->
        StudentDetailDialog(
            student = student,
            viewModel = viewModel,
            onDismiss = { viewModel.selectStudentForDetail(null) }
        )
    }

    showCollectFeeDialog?.let { student ->
        CollectFeeDialog(
            student = student,
            viewModel = viewModel,
            onDismiss = { viewModel.showCollectFeeDialog(null) }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showAddExpenseDialog(false) }
        )
    }

    activeReceipt?.let { receipt ->
        ReceiptDialog(
            payment = receipt,
            viewModel = viewModel,
            onDismiss = { viewModel.showActiveReceipt(null) }
        )
    }

    selectedSeatForAction?.let { seat ->
        SeatActionDialog(
            seat = seat,
            students = students,
            shifts = shifts.map { it.name },
            viewModel = viewModel,
            onDismiss = { viewModel.selectSeatForAction(null) }
        )
    }

    if (showBranchDialog) {
        BranchManagerDialog(
            branches = branches,
            activeBranchId = activeBranchId,
            viewModel = viewModel,
            onDismiss = { viewModel.showBranchManagerDialog(false) }
        )
    }

    if (showQrDialog) {
        RegistrationQrDialog(
            library = library,
            onDismiss = { viewModel.showQrDialog(false) }
        )
    }

    if (showWhatsAppReminderDialog) {
        WhatsAppReminderDialog(
            viewModel = viewModel,
            preselectedStudent = selectedStudentForWhatsAppReminder,
            onDismiss = {
                viewModel.selectStudentForWhatsAppReminder(null)
                viewModel.showWhatsAppReminderDialog(false)
            }
        )
    }

    // SaaS Upgrade Modal
    if (showUpgradeModal) {
        UpgradeModal(
            viewModel = viewModel,
            onDismiss = { viewModel.dismissUpgradeModal() }
        )
    }
}
