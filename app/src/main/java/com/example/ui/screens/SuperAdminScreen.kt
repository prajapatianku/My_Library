package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.SavedLibraryAccount
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class SuperAdminTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OVERVIEW("Analytics", Icons.Default.Analytics),
    DIRECTORY("Owners", Icons.Default.People),
    PRICING("Pricing & Coupons", Icons.Default.PriceCheck),
    BROADCASTS("Broadcasts", Icons.Default.Campaign),
    APP_CONTROL("System", Icons.Default.SettingsSuggest)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(
    viewModel: LibraryViewModel,
    onExitToApp: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(SuperAdminTab.OVERVIEW) }

    val platformPricing by viewModel.platformPricing.collectAsState()
    val platformCoupons by viewModel.platformCoupons.collectAsState()
    val platformTransactions by viewModel.platformTransactions.collectAsState()
    val platformBroadcasts by viewModel.platformBroadcasts.collectAsState()
    val platformAppControl by viewModel.platformAppControl.collectAsState()

    var showGrantSubDialogForAccount by remember { mutableStateOf<SavedLibraryAccount?>(null) }
    var showOwnerDetailDialog by remember { mutableStateOf<SavedLibraryAccount?>(null) }
    var showCreateCouponDialog by remember { mutableStateOf(false) }
    var showCreateBroadcastDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Vidyara Platform Owner", fontSize = 17.sp, fontWeight = FontWeight.Black, color = PureWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = AmberTertiary, shape = RoundedCornerShape(4.dp)) {
                                Text("SUPER ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Black, color = NavyPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text("Business Operations & Subscriptions", fontSize = 11.sp, color = PureWhite.copy(alpha = 0.7f))
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = onExitToApp,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(15.dp), tint = PureWhite)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exit to Library", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }

                    IconButton(onClick = {
                        viewModel.logoutSuperAdmin()
                        onExitToApp()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = PureWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 8.dp
            ) {
                SuperAdminTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(imageVector = tab.icon, contentDescription = tab.title)
                        },
                        label = {
                            Text(tab.title, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyPrimary,
                            selectedTextColor = NavyPrimary,
                            indicatorColor = NavyPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = WarmTextMuted,
                            unselectedTextColor = WarmTextMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SlateBackground)
        ) {
            when (selectedTab) {
                SuperAdminTab.OVERVIEW -> SuperAdminOverviewTab(
                    transactions = platformTransactions,
                    allAccounts = viewModel.platformRepository.getAllLibraryOwners(),
                    onExportRevenue = {
                        val csv = viewModel.platformRepository.exportPlatformRevenueCsv()
                        shareCsvContent(context, "Vidyara-Revenue-Report.csv", csv)
                    },
                    onExportDirectory = {
                        val csv = viewModel.platformRepository.exportOwnerDirectoryCsv()
                        shareCsvContent(context, "Vidyara-Owners-Directory.csv", csv)
                    }
                )

                SuperAdminTab.DIRECTORY -> SuperAdminDirectoryTab(
                    viewModel = viewModel,
                    onInspectOwner = { showOwnerDetailDialog = it },
                    onGrantSubscription = { showGrantSubDialogForAccount = it }
                )

                SuperAdminTab.PRICING -> SuperAdminPricingTab(
                    pricing = platformPricing,
                    coupons = platformCoupons,
                    onSavePricing = { viewModel.platformRepository.updatePricing(it) },
                    onCreateCouponClick = { showCreateCouponDialog = true },
                    onToggleCoupon = { viewModel.platformRepository.toggleCouponActive(it) },
                    onDeleteCoupon = { viewModel.platformRepository.deleteCoupon(it) }
                )

                SuperAdminTab.BROADCASTS -> SuperAdminBroadcastsTab(
                    broadcasts = platformBroadcasts,
                    onComposeBroadcastClick = { showCreateBroadcastDialog = true }
                )

                SuperAdminTab.APP_CONTROL -> SuperAdminAppControlTab(
                    appControl = platformAppControl,
                    onUpdateMaintenance = { en, msg -> viewModel.platformRepository.updateMaintenanceMode(en, msg) },
                    onUpdateWhatsNew = { t, b -> viewModel.platformRepository.updateWhatsNew(t, b) }
                )
            }
        }
    }

    // Modal: Grant Complimentary Subscription
    showGrantSubDialogForAccount?.let { account ->
        GrantSubscriptionModal(
            account = account,
            onDismiss = { showGrantSubDialogForAccount = null },
            onGrant = { plan, days, isLifetime ->
                viewModel.platformRepository.grantComplimentarySubscription(account.accountId, plan, days, isLifetime)
                showGrantSubDialogForAccount = null
            }
        )
    }

    // Modal: Inspect Owner Full Details
    showOwnerDetailDialog?.let { account ->
        SuperAdminOwnerDetailModal(
            account = account,
            onDismiss = { showOwnerDetailDialog = null },
            onToggleSuspension = { suspend, reason ->
                viewModel.platformRepository.toggleAccountSuspension(account.accountId, suspend, reason)
                showOwnerDetailDialog = null
            }
        )
    }

    // Modal: Create Promo Coupon
    if (showCreateCouponDialog) {
        CreateCouponModal(
            onDismiss = { showCreateCouponDialog = false },
            onCreate = { code, type, value, plan, days, maxUses ->
                viewModel.platformRepository.createCoupon(code, type, value, plan, days, maxUses)
                showCreateCouponDialog = false
            }
        )
    }

    // Modal: Compose Broadcast
    if (showCreateBroadcastDialog) {
        CreateBroadcastModal(
            onDismiss = { showCreateBroadcastDialog = false },
            onSend = { title, msg, audience ->
                viewModel.platformRepository.sendBroadcast(title, msg, audience)
                showCreateBroadcastDialog = false
            }
        )
    }
}

// =============================================================================
// SUB-TAB 1: OVERVIEW & ANALYTICS
// =============================================================================

@Composable
fun SuperAdminOverviewTab(
    transactions: List<PlatformTransaction>,
    allAccounts: List<SavedLibraryAccount>,
    onExportRevenue: () -> Unit,
    onExportDirectory: () -> Unit
) {
    val totalRevenue = remember(transactions) {
        transactions.filter { it.status == "SUCCESS" }.sumOf { it.amount }
    }
    val proRevenue = remember(transactions) {
        transactions.filter { it.status == "SUCCESS" && it.planName.contains("Pro", ignoreCase = true) }.sumOf { it.amount }
    }
    val businessRevenue = remember(transactions) {
        transactions.filter { it.status == "SUCCESS" && it.planName.contains("Business", ignoreCase = true) }.sumOf { it.amount }
    }

    val totalOwners = allAccounts.size
    val activeProCount = allAccounts.count { it.saasSubscription.planType == SaaSPlanType.PREMIUM }
    val activeBusinessCount = allAccounts.count { it.saasSubscription.planType == SaaSPlanType.BUSINESS }
    val freeCount = allAccounts.count { it.saasSubscription.planType == SaaSPlanType.FREE }
    val suspendedCount = allAccounts.count { it.ownerProfile.isSuspended }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Platform Revenue Headline Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = NavyPrimary)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL PLATFORM REVENUE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberTertiary)
                        Text("₹${String.format(Locale.US, "%,d", totalRevenue)}", fontSize = 30.sp, fontWeight = FontWeight.Black, color = PureWhite)
                    }
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Razorpay Live", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = PureWhite.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Pro Plan Revenue", fontSize = 10.sp, color = PureWhite.copy(alpha = 0.7f))
                        Text("₹${String.format(Locale.US, "%,d", proRevenue)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                    Column {
                        Text("Business Revenue", fontSize = 10.sp, color = PureWhite.copy(alpha = 0.7f))
                        Text("₹${String.format(Locale.US, "%,d", businessRevenue)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Paid Transactions", fontSize = 10.sp, color = PureWhite.copy(alpha = 0.7f))
                        Text("${transactions.count { it.status == "SUCCESS" }} Txns", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Export Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onExportRevenue,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Revenue (CSV)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            }

            OutlinedButton(
                onClick = onExportDirectory,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Icon(Icons.Default.PeopleAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = OrangePrimaryDark)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Owners (CSV)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangePrimaryDark)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subscriber Metrics Cards Grid
        Text("Subscriber Base & Footprint", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarmTextDark)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KpiMetricBox(
                title = "Total Owners",
                value = "$totalOwners",
                subtext = "Registered libraries",
                badgeColor = NavyPrimary,
                modifier = Modifier.weight(1f)
            )
            KpiMetricBox(
                title = "Pro Subscribers",
                value = "$activeProCount",
                subtext = "₹99/month active",
                badgeColor = OrangePrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KpiMetricBox(
                title = "Business Plans",
                value = "$activeBusinessCount",
                subtext = "Multi-branch active",
                badgeColor = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
            KpiMetricBox(
                title = "Free Tier",
                value = "$freeCount",
                subtext = "Upgrade potential",
                badgeColor = Color(0xFF64748B),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Recent Platform Transactions Stream
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Platform Subscriptions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarmTextDark)
            Text("${transactions.size} Total", fontSize = 12.sp, color = WarmTextMuted)
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No platform transactions recorded yet.",
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center,
                    color = WarmTextMuted
                )
            }
        } else {
            transactions.take(10).forEach { tx ->
                PlatformTransactionCard(tx)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun KpiMetricBox(
    title: String,
    value: String,
    subtext: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmTextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = badgeColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtext, fontSize = 10.sp, color = WarmTextMuted)
        }
    }
}

@Composable
fun PlatformTransactionCard(tx: PlatformTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tx.libraryName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmTextDark)
                    if (tx.isComplimentary) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(4.dp)) {
                            Text("FREE GRANT", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF92400E), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("${tx.ownerName} • ${tx.ownerPhone}", fontSize = 11.sp, color = WarmTextMuted)
                Text("${tx.planName} (${tx.billingPeriod}) • ${tx.timestamp}", fontSize = 10.sp, color = WarmTextMuted)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (tx.amount > 0) "₹${tx.amount}" else "Free",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (tx.amount > 0) Color(0xFF16A34A) else OrangePrimary
                )
                if (tx.discountAmount > 0) {
                    Text("Saved ₹${tx.discountAmount}", fontSize = 10.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                }
                Text(tx.transactionId.take(16), fontSize = 9.sp, color = WarmTextMuted)
            }
        }
    }
}

// =============================================================================
// SUB-TAB 2: DIRECTORY (USER MANAGEMENT)
// =============================================================================

@Composable
fun SuperAdminDirectoryTab(
    viewModel: LibraryViewModel,
    onInspectOwner: (SavedLibraryAccount) -> Unit,
    onGrantSubscription: (SavedLibraryAccount) -> Unit
) {
    val allAccounts = remember { viewModel.platformRepository.getAllLibraryOwners() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlanFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val filteredAccounts = remember(searchQuery, selectedPlanFilter, selectedStatusFilter, allAccounts) {
        allAccounts.filter { acc ->
            val matchesQuery = acc.ownerProfile.fullName.contains(searchQuery, ignoreCase = true) ||
                    acc.ownerProfile.phone.contains(searchQuery) ||
                    acc.ownerProfile.email.contains(searchQuery, ignoreCase = true) ||
                    acc.library.name.contains(searchQuery, ignoreCase = true)

            val matchesPlan = when (selectedPlanFilter) {
                "ALL" -> true
                "FREE" -> acc.saasSubscription.planType == SaaSPlanType.FREE
                "PRO" -> acc.saasSubscription.planType == SaaSPlanType.PREMIUM
                "BUSINESS" -> acc.saasSubscription.planType == SaaSPlanType.BUSINESS
                else -> true
            }

            val matchesStatus = when (selectedStatusFilter) {
                "ALL" -> true
                "SUSPENDED" -> acc.ownerProfile.isSuspended
                "ACTIVE" -> !acc.ownerProfile.isSuspended
                else -> true
            }

            matchesQuery && matchesPlan && matchesStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search owner name, phone, library...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            textStyle = AppInputTextStyle,
            colors = appOutlinedTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ALL", "PRO", "BUSINESS", "FREE").forEach { plan ->
                FilterChip(
                    selected = selectedPlanFilter == plan,
                    onClick = { selectedPlanFilter = plan },
                    label = { Text(plan, fontSize = 10.sp, fontWeight = if (selectedPlanFilter == plan) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results counter
        Text("${filteredAccounts.size} Registered Library Owners", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmTextMuted)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredAccounts) { acc ->
                OwnerDirectoryCard(
                    account = acc,
                    onInspect = { onInspectOwner(acc) },
                    onGrantSubscription = { onGrantSubscription(acc) },
                    onToggleSuspension = {
                        viewModel.platformRepository.toggleAccountSuspension(acc.accountId, !acc.ownerProfile.isSuspended)
                    }
                )
            }
        }
    }
}

@Composable
fun OwnerDirectoryCard(
    account: SavedLibraryAccount,
    onInspect: () -> Unit,
    onGrantSubscription: () -> Unit,
    onToggleSuspension: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (account.ownerProfile.isSuspended) DangerRed.copy(alpha = 0.5f) else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(account.library.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarmTextDark)
                    Text("${account.ownerProfile.fullName} • ${account.ownerProfile.phone}", fontSize = 11.sp, color = WarmTextMuted)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = when (account.saasSubscription.planType) {
                            SaaSPlanType.BUSINESS -> Color(0xFFEDE9FE)
                            SaaSPlanType.PREMIUM -> OrangePrimaryContainer
                            else -> Color(0xFFF1F5F9)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = account.saasSubscription.planType.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (account.saasSubscription.planType) {
                                SaaSPlanType.BUSINESS -> Color(0xFF6D28D9)
                                SaaSPlanType.PREMIUM -> OrangePrimaryDark
                                else -> Color(0xFF475569)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (account.ownerProfile.isSuspended) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(4.dp)) {
                            Text("SUSPENDED", fontSize = 8.sp, fontWeight = FontWeight.Black, color = DangerRed, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("👥 ${account.students.size} Students", fontSize = 11.sp, color = WarmTextDark, fontWeight = FontWeight.Medium)
                Text("🪑 ${account.seats.size} Desks", fontSize = 11.sp, color = WarmTextDark, fontWeight = FontWeight.Medium)
                Text("🏢 ${account.branches.size} Branch(es)", fontSize = 11.sp, color = WarmTextDark, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Admin Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onInspect,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text("Inspect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onGrantSubscription,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Grant Plan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onToggleSuspension,
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (account.ownerProfile.isSuspended) Color(0xFF16A34A) else DangerRed
                    ),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(if (account.ownerProfile.isSuspended) "Activate" else "Suspend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =============================================================================
// SUB-TAB 3: PRICING & COUPONS
// =============================================================================

@Composable
fun SuperAdminPricingTab(
    pricing: PlatformPlanPricing,
    coupons: List<PlatformCoupon>,
    onSavePricing: (PlatformPlanPricing) -> Unit,
    onCreateCouponClick: () -> Unit,
    onToggleCoupon: (String) -> Unit,
    onDeleteCoupon: (String) -> Unit
) {
    var proMonthly by remember(pricing) { mutableStateOf(pricing.proMonthlyPrice.toString()) }
    var proYearly by remember(pricing) { mutableStateOf(pricing.proYearlyPrice.toString()) }
    var bizMonthly by remember(pricing) { mutableStateOf(pricing.businessMonthlyPrice.toString()) }
    var bizYearly by remember(pricing) { mutableStateOf(pricing.businessYearlyPrice.toString()) }
    var addBranchMonthly by remember(pricing) { mutableStateOf(pricing.additionalBranchMonthlyPrice.toString()) }
    var graceDays by remember(pricing) { mutableStateOf(pricing.gracePeriodDays.toString()) }
    var freeLimit by remember(pricing) { mutableStateOf(pricing.studentFreeLimit.toString()) }

    var saveFeedback by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section: Live Subscription Pricing
        Text("Dynamic Subscription Pricing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarmTextDark)
        Text("Changes apply immediately to new library owner purchases & renewals", fontSize = 11.sp, color = WarmTextMuted)
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Vidyara Pro
                Text("Vidyara Pro (Single Branch)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OrangePrimaryDark)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = proMonthly,
                        onValueChange = { proMonthly = it },
                        label = { Text("Monthly Rate (₹)") },
                        textStyle = AppInputTextStyle,
                        colors = appOutlinedTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = proYearly,
                        onValueChange = { proYearly = it },
                        label = { Text("Yearly Rate (₹)") },
                        textStyle = AppInputTextStyle,
                        colors = appOutlinedTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Vidyara Business
                Text("Vidyara Business (Multi-Branch)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF6D28D9))
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bizMonthly,
                        onValueChange = { bizMonthly = it },
                        label = { Text("Monthly Rate (₹)") },
                        textStyle = AppInputTextStyle,
                        colors = appOutlinedTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bizYearly,
                        onValueChange = { bizYearly = it },
                        label = { Text("Yearly Rate (₹)") },
                        textStyle = AppInputTextStyle,
                        colors = appOutlinedTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Additional Branch Add-On & Grace Days
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = addBranchMonthly,
                        onValueChange = { addBranchMonthly = it },
                        label = { Text("Add Branch (₹/mo)") },
                        textStyle = AppInputTextStyle,
                        colors = appOutlinedTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = graceDays,
                        onValueChange = { graceDays = it },
                        label = { Text("Grace Period (Days)") },
                        textStyle = AppInputTextStyle,
                        colors = appOutlinedTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val newPricing = pricing.copy(
                            proMonthlyPrice = proMonthly.toIntOrNull() ?: 99,
                            proYearlyPrice = proYearly.toIntOrNull() ?: 899,
                            businessMonthlyPrice = bizMonthly.toIntOrNull() ?: 199,
                            businessYearlyPrice = bizYearly.toIntOrNull() ?: 1799,
                            additionalBranchMonthlyPrice = addBranchMonthly.toIntOrNull() ?: 99,
                            gracePeriodDays = graceDays.toIntOrNull() ?: 3
                        )
                        onSavePricing(newPricing)
                        saveFeedback = "Pricing updated and synced platform-wide!"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save & Publish Live Pricing", fontWeight = FontWeight.Bold)
                }

                saveFeedback?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Discount Coupons & Offers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Promotional Coupon Codes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Text("Redeemable at Razorpay checkout", fontSize = 11.sp, color = WarmTextMuted)
            }

            Button(
                onClick = onCreateCouponClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        coupons.forEach { c ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(c.code, fontWeight = FontWeight.Black, fontSize = 14.sp, color = NavyPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    text = if (c.discountType == "PERCENT") "${c.discountValue}% OFF" else "₹${c.discountValue} OFF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text("Applies to: ${c.targetPlan} • Expires: ${c.expiryDate}", fontSize = 10.sp, color = WarmTextMuted)
                        Text("Used: ${c.usedCount} / ${c.maxUses} times", fontSize = 10.sp, color = WarmTextDark, fontWeight = FontWeight.Medium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = c.isActive,
                            onCheckedChange = { onToggleCoupon(c.id) },
                            modifier = Modifier.scale(0.8f)
                        )
                        IconButton(onClick = { onDeleteCoupon(c.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// SUB-TAB 4: BROADCASTS & OFFERS
// =============================================================================

@Composable
fun SuperAdminBroadcastsTab(
    broadcasts: List<PlatformBroadcast>,
    onComposeBroadcastClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Broadcast Announcements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Text("Push in-app banners to targeted owner segments", fontSize = 11.sp, color = WarmTextMuted)
            }

            Button(
                onClick = onComposeBroadcastClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Compose", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (broadcasts.isEmpty()) {
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("No broadcasts sent yet.", modifier = Modifier.padding(20.dp), textAlign = TextAlign.Center, color = WarmTextMuted)
            }
        } else {
            broadcasts.forEach { bc ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bc.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WarmTextDark, modifier = Modifier.weight(1f))
                            Surface(
                                color = when (bc.targetAudience) {
                                    "FREE_ONLY" -> Color(0xFFFEF3C7)
                                    "PRO_ONLY" -> OrangePrimaryContainer
                                    "BUSINESS_ONLY" -> Color(0xFFEDE9FE)
                                    else -> Color(0xFFE2E8F0)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = bc.targetAudience.replace("_", " "),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NavyPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(bc.message, fontSize = 12.sp, color = WarmTextDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sent on: ${bc.timestamp}", fontSize = 10.sp, color = WarmTextMuted)
                    }
                }
            }
        }
    }
}

// =============================================================================
// SUB-TAB 5: APP CONTROL & MAINTENANCE
// =============================================================================

@Composable
fun SuperAdminAppControlTab(
    appControl: PlatformAppControl,
    onUpdateMaintenance: (Boolean, String) -> Unit,
    onUpdateWhatsNew: (String, List<String>) -> Unit
) {
    var isMaintenance by remember(appControl) { mutableStateOf(appControl.maintenanceMode) }
    var maintenanceMsg by remember(appControl) { mutableStateOf(appControl.maintenanceMessage) }
    var whatsNewTitle by remember(appControl) { mutableStateOf(appControl.whatsNewTitle) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("App-Wide Maintenance & Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarmTextDark)
        Spacer(modifier = Modifier.height(10.dp))

        // Maintenance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isMaintenance) DangerRed else Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Platform Maintenance Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isMaintenance) DangerRed else WarmTextDark)
                        Text("Display a maintenance banner to all library owners", fontSize = 11.sp, color = WarmTextMuted)
                    }
                    Switch(
                        checked = isMaintenance,
                        onCheckedChange = {
                            isMaintenance = it
                            onUpdateMaintenance(it, maintenanceMsg)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = maintenanceMsg,
                    onValueChange = { maintenanceMsg = it },
                    label = { Text("Maintenance Notice Message") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onUpdateMaintenance(isMaintenance, maintenanceMsg) },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Update Maintenance Notice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // What's New Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("What's New Changelog", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = whatsNewTitle,
                    onValueChange = { whatsNewTitle = it },
                    label = { Text("Changelog Title") },
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Release Highlights:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmTextDark)
                appControl.whatsNewBullets.forEach { bullet ->
                    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("• ", fontWeight = FontWeight.Black, color = OrangePrimary)
                        Text(bullet, fontSize = 12.sp, color = WarmTextDark)
                    }
                }
            }
        }
    }
}

// =============================================================================
// MODALS
// =============================================================================

@Composable
fun GrantSubscriptionModal(
    account: SavedLibraryAccount,
    onDismiss: () -> Unit,
    onGrant: (SaaSPlanType, Int, Boolean) -> Unit
) {
    var selectedPlan by remember { mutableStateOf(SaaSPlanType.PREMIUM) }
    var selectedDurationDays by remember { mutableStateOf(30) }
    var isLifetime by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Grant Complimentary Access", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = NavyPrimary)
                Text("Comp a plan for ${account.ownerProfile.fullName} (${account.library.name})", fontSize = 11.sp, color = WarmTextMuted)

                Spacer(modifier = Modifier.height(14.dp))

                Text("Select Plan:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(SaaSPlanType.PREMIUM, SaaSPlanType.BUSINESS).forEach { plan ->
                        FilterChip(
                            selected = selectedPlan == plan,
                            onClick = { selectedPlan = plan },
                            label = { Text(plan.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Select Duration:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(Pair("30 Days", 30), Pair("90 Days", 90), Pair("1 Year", 365)).forEach { (lbl, days) ->
                        FilterChip(
                            selected = selectedDurationDays == days && !isLifetime,
                            onClick = {
                                selectedDurationDays = days
                                isLifetime = false
                            },
                            label = { Text(lbl, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isLifetime, onCheckedChange = { isLifetime = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Grant Lifetime Free Access (No Expiry)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OrangePrimaryDark)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onGrant(selectedPlan, selectedDurationDays, isLifetime) },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Apply Free Grant", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SuperAdminOwnerDetailModal(
    account: SavedLibraryAccount,
    onDismiss: () -> Unit,
    onToggleSuspension: (Boolean, String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Owner Inspection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = NavyPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(account.library.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WarmTextDark)
                        Text("Owner: ${account.ownerProfile.fullName}", fontSize = 12.sp, color = WarmTextDark)
                        Text("Phone: ${account.ownerProfile.phone}", fontSize = 12.sp, color = WarmTextDark)
                        Text("Email: ${account.ownerProfile.email}", fontSize = 12.sp, color = WarmTextDark)
                        Text("City: ${account.library.city}, ${account.library.state}", fontSize = 12.sp, color = WarmTextMuted)
                        Text("UPI ID: ${account.library.upiId}", fontSize = 12.sp, color = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Operational Statistics:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Students", fontSize = 10.sp, color = WarmTextMuted)
                        Text("${account.students.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Desks", fontSize = 10.sp, color = WarmTextMuted)
                        Text("${account.seats.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Shifts", fontSize = 10.sp, color = WarmTextMuted)
                        Text("${account.shifts.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Branches", fontSize = 10.sp, color = WarmTextMuted)
                        Text("${account.branches.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Subscription & Billing:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Plan: ${account.saasSubscription.planType.displayName}", fontSize = 12.sp)
                Text("Cycle: ${account.saasSubscription.billingPeriod.name}", fontSize = 12.sp)
                Text("Expires On: ${account.saasSubscription.endDate}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { onToggleSuspension(!account.ownerProfile.isSuspended, "Deactivated by Super Admin.") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (account.ownerProfile.isSuspended) Color(0xFF16A34A) else DangerRed)
                ) {
                    Text(if (account.ownerProfile.isSuspended) "Reactivate Account" else "Deactivate / Suspend Account", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreateCouponModal(
    onDismiss: () -> Unit,
    onCreate: (String, String, Int, String, Int, Int) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("PERCENT") }
    var discountValue by remember { mutableStateOf("50") }
    var targetPlan by remember { mutableStateOf("ALL") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Create Promo Coupon", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = NavyPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Coupon Code (e.g. FESTIVE50)") },
                    singleLine = true,
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("PERCENT", "FLAT").forEach { t ->
                        FilterChip(
                            selected = discountType == t,
                            onClick = { discountType = t },
                            label = { Text(if (t == "PERCENT") "% Discount" else "Flat ₹ Discount") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = discountValue,
                    onValueChange = { discountValue = it },
                    label = { Text(if (discountType == "PERCENT") "Discount Percentage (%)" else "Flat Amount (₹)") },
                    singleLine = true,
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val v = discountValue.toIntOrNull() ?: 10
                            onCreate(code, discountType, v, targetPlan, 30, 200)
                        },
                        modifier = Modifier.weight(1.5f),
                        enabled = code.isNotBlank() && discountValue.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Create Coupon", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateBroadcastModal(
    onDismiss: () -> Unit,
    onSend: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("ALL") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Compose Broadcast Announcement", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = NavyPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Announcement Title *") },
                    singleLine = true,
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Content *") },
                    maxLines = 4,
                    textStyle = AppInputTextStyle,
                    colors = appOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Target Audience:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmTextDark)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ALL", "FREE_ONLY", "PRO_ONLY", "EXPIRED_ONLY").forEach { aud ->
                        FilterChip(
                            selected = audience == aud,
                            onClick = { audience = aud },
                            label = { Text(aud.replace("_", " "), fontSize = 9.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSend(title, message, audience) },
                        modifier = Modifier.weight(1.5f),
                        enabled = title.isNotBlank() && message.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Send Broadcast", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Utility: Share CSV via Intent
private fun shareCsvContent(context: Context, filename: String, content: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, filename)
        putExtra(Intent.EXTRA_TEXT, content)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Export $filename"))
}

private fun Modifier.scale(scale: Float): Modifier = this
