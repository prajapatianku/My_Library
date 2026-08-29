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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel

enum class MainNavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    STUDENTS("Students", Icons.Filled.People, Icons.Outlined.People),
    ATTENDANCE("Attendance", Icons.Filled.AssignmentTurnedIn, Icons.Outlined.AssignmentTurnedIn),
    SEAT_MAP("Seats", Icons.Filled.Chair, Icons.Outlined.Chair),
    PAYMENTS("Finance", Icons.Filled.Payments, Icons.Outlined.Payments),
    REPORTS("Reports", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SETTINGS("Menu", Icons.Filled.Menu, Icons.Outlined.Menu)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
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

    var selectedTab by remember { mutableStateOf(MainNavigationTab.DASHBOARD) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (!isLoggedIn) {
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
                                    tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                    MainNavigationTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
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
