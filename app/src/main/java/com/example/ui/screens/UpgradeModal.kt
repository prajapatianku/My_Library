package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import com.example.MainActivity
import com.example.data.model.BillingPeriod
import com.example.data.model.SaaSPlanType
import com.example.ui.theme.*
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun UpgradeModal(
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentPlan by viewModel.saasSubscription.collectAsState()
    val targetFeature by viewModel.upgradeTargetFeature.collectAsState()

    var selectedPlan by remember {
        mutableStateOf(
            if (targetFeature?.contains("branch") == true) SaaSPlanType.BUSINESS else SaaSPlanType.PREMIUM
        )
    }
    var billingPeriod by remember { mutableStateOf(BillingPeriod.SIX_MONTH) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var selectedBranchCount by remember { mutableStateOf(1) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp)),
            color = PureWhite,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp
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
                    Column {
                        Text(
                            text = "Upgrade Vidyara",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = WarmTextDark
                        )
                        Text(
                            text = "Choose the plan that fits your scale",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmTextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = WarmTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (currentPlan.planType != SaaSPlanType.FREE) {
                    val daysRemaining = remember(currentPlan) {
                        viewModel.getSubscriptionDaysRemaining()
                    }
                    val canRenew = daysRemaining in 0..7

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.5.dp, OrangePrimary, RoundedCornerShape(20.dp)),
                            color = OrangePrimaryContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "ACTIVE SUBSCRIPTION",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangePrimaryDark
                                )
                                Text(
                                    text = currentPlan.planType.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = WarmTextDark
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = OrangePrimary.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Billing Cycle: ${if (currentPlan.billingPeriod == BillingPeriod.MONTHLY) "Monthly (28 Days)" else "6 Months (168 Days)"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = WarmTextDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Expires on: ${currentPlan.endDate}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmTextDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Allowed Branches: ${currentPlan.allowedBranchesCount}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangePrimaryDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (daysRemaining >= 0) {
                                    Surface(
                                        color = if (daysRemaining <= 3) DangerRed.copy(alpha = 0.1f) else Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "$daysRemaining days remaining",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (daysRemaining <= 3) DangerRed else Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (currentPlan.planType == SaaSPlanType.BUSINESS) {
                        val proratedPrice = viewModel.calculateProratedBranchPrice()
                        Button(
                            onClick = {
                                val activity = context as? MainActivity
                                if (activity != null) {
                                    activity.startSaaSPayment(currentPlan.planType, currentPlan.billingPeriod, isBranchPurchase = true)
                                    onDismiss()
                                } else {
                                    viewModel.purchaseAdditionalBranch()
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Text(
                                text = "Add Branch (₹$proratedPrice for remaining $daysRemaining days)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (canRenew) {
                        Button(
                            onClick = {
                                val activity = context as? MainActivity
                                if (activity != null) {
                                    activity.startSaaSPayment(currentPlan.planType, currentPlan.billingPeriod, isRenewal = true)
                                    onDismiss()
                                } else {
                                    viewModel.renewSaaS()
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text(
                                text = "Renew ${currentPlan.planType.displayName} Now",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    } else {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE5DECE),
                                disabledContainerColor = Color(0xFFE5DECE)
                            )
                        ) {
                            Text(
                                text = "Renewal available 7 days before expiry",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = WarmTextMuted
                            )
                        }
                    }
                } else {
                    // Billing Period Toggle (Monthly vs 6-Month Save)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF6F0E7))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { billingPeriod = BillingPeriod.MONTHLY },
                            color = if (billingPeriod == BillingPeriod.MONTHLY) PureWhite else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Monthly Billing",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (billingPeriod == BillingPeriod.MONTHLY) FontWeight.Bold else FontWeight.Normal,
                                color = if (billingPeriod == BillingPeriod.MONTHLY) OrangePrimaryDark else WarmTextMuted,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { billingPeriod = BillingPeriod.SIX_MONTH },
                            color = if (billingPeriod == BillingPeriod.SIX_MONTH) PureWhite else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "6 Months",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (billingPeriod == BillingPeriod.SIX_MONTH) FontWeight.Bold else FontWeight.Normal,
                                    color = if (billingPeriod == BillingPeriod.SIX_MONTH) OrangePrimaryDark else WarmTextMuted
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = Color(0xFF16A34A),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "SAVE 33%",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrollable Plan Cards
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Plan: FREE
                        PlanSelectionCard(
                            title = "VIDYARA FREE",
                            price = "₹0",
                            subtitle = "Forever basic library operations",
                            isSelected = selectedPlan == SaaSPlanType.FREE,
                            isCurrent = currentPlan.planType == SaaSPlanType.FREE,
                            features = listOf(
                                "Full student & seat management",
                                "Daily shift & attendance tracking",
                                "Payment collection & receipts",
                                "Expense logging",
                                "Basic financial report view",
                                "Single branch access",
                                "✕ WhatsApp Fee Due Alerts (Requires Pro/Business)"
                            ),
                            onSelect = { selectedPlan = SaaSPlanType.FREE }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Plan: PREMIUM (2nd Plan)
                        val premiumPrice = if (billingPeriod == BillingPeriod.MONTHLY) "₹99/mo" else "₹399 / 6 mos"
                        PlanSelectionCard(
                            title = "VIDYARA PRO",
                            badge = "POPULAR",
                            price = premiumPrice,
                            subtitle = "For single-library owners needing WhatsApp alerts & reports",
                            isSelected = selectedPlan == SaaSPlanType.PREMIUM,
                            isCurrent = currentPlan.planType == SaaSPlanType.PREMIUM,
                            features = listOf(
                                "Everything in Vidyara Free",
                                "📲 WhatsApp Student Fee Due Alerts to Owner",
                                "💬 1-Tap Student WhatsApp Fee Due Reminders",
                                "Download & export Revenue reports",
                                "PDF & CSV Financial Exports",
                                "Advanced Student Analytics & Trends",
                                "Priority WhatsApp/Email Support"
                            ),
                            onSelect = { selectedPlan = SaaSPlanType.PREMIUM }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Plan: BUSINESS (3rd Plan)
                        val businessPrice = if (billingPeriod == BillingPeriod.MONTHLY) {
                            val base = 199
                            val additional = (selectedBranchCount - 1) * 99
                            "₹${base + additional}/mo"
                        } else {
                            val base = 999
                            val additional = (selectedBranchCount - 1) * 499
                            "₹${base + additional} / 6 mos"
                        }
                        PlanSelectionCard(
                            title = "VIDYARA BUSINESS",
                            badge = "MULTI-BRANCH",
                            price = businessPrice,
                            subtitle = "For owners managing multiple library branches with full alerts",
                            isSelected = selectedPlan == SaaSPlanType.BUSINESS,
                            isCurrent = currentPlan.planType == SaaSPlanType.BUSINESS,
                            features = listOf(
                                "Everything in Vidyara Pro",
                                "📲 Multi-Branch WhatsApp Due Alerts to Owner",
                                "💬 Bulk WhatsApp Student Fee Reminders",
                                "Manage Multiple Library Branches",
                                "Instant Branch Switching",
                                "Consolidated Multi-Branch Dashboard",
                                "Centralized Owner Control & Audit"
                            ),
                            onSelect = { selectedPlan = SaaSPlanType.BUSINESS }
                        )
                    }

                    if (selectedPlan == SaaSPlanType.BUSINESS) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF8F3)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DECE))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Branches to Manage",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = WarmTextDark
                                    )
                                    Text(
                                        text = "Base price covers 1 branch",
                                        fontSize = 11.sp,
                                        color = WarmTextMuted
                                    )
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (selectedBranchCount > 1) selectedBranchCount-- },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = PureWhite),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                    }
                                    
                                    Text(
                                        text = selectedBranchCount.toString(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = OrangePrimaryDark
                                    )
                                    
                                    IconButton(
                                        onClick = { selectedBranchCount++ },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = PureWhite),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Action Button
                    if (selectedPlan == currentPlan.planType) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCCFC0))
                        ) {
                            Text("Current Active Plan", color = WarmTextDark, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                isProcessingPayment = true
                                val activity = context as? MainActivity
                                if (activity != null) {
                                    activity.startSaaSPayment(selectedPlan, billingPeriod, branchCount = selectedBranchCount)
                                    onDismiss()
                                } else {
                                    // Fallback
                                    viewModel.upgradeSaaS(selectedPlan, billingPeriod, selectedBranchCount)
                                    onDismiss()
                                }
                                isProcessingPayment = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary
                            ),
                            enabled = !isProcessingPayment
                        ) {
                            if (isProcessingPayment) {
                                CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    text = "Activate ${selectedPlan.displayName} Now",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanSelectionCard(
    title: String,
    price: String,
    subtitle: String,
    isSelected: Boolean,
    isCurrent: Boolean,
    features: List<String>,
    badge: String? = null,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) OrangePrimary else Color(0xFFE5DECE),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() },
        color = if (isSelected) OrangePrimaryContainer.copy(alpha = 0.35f) else PureWhite,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) OrangePrimaryDark else WarmTextDark
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = OrangePrimary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isCurrent) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "CURRENT PLAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = price,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = if (isSelected) OrangePrimaryDark else WarmTextDark
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WarmTextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFECE4D8))
            Spacer(modifier = Modifier.height(8.dp))

            features.forEach { feat ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feat,
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmTextDark
                    )
                }
            }
        }
    }
}
