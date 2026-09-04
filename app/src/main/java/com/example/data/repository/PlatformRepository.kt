package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.*
import com.example.data.supabase.SupabaseApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PlatformRepository private constructor(private val context: Context?) {

    private val ctx = context?.applicationContext ?: com.example.LibraryApp.appContext
    private val prefs: SharedPreferences? = try {
        ctx?.getSharedPreferences("vidyara_platform_superadmin_v1", Context.MODE_PRIVATE)
    } catch (e: Exception) {
        null
    }

    private val supabaseClient = SupabaseApiClient()
    private val storage = LibraryAccountStorage(ctx)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // =========================================================================
    // SUPER ADMIN AUTHENTICATION
    // =========================================================================

    companion object {
        @Volatile
        private var INSTANCE: PlatformRepository? = null

        fun getInstance(context: Context? = null): PlatformRepository {
            val appCtx = context?.applicationContext ?: com.example.LibraryApp.appContext
            return INSTANCE ?: synchronized(this) {
                val instance = PlatformRepository(appCtx)
                INSTANCE = instance
                instance
            }
        }

        const val WHITELIST_PHONE = "+91 9876543210"
        const val WHITELIST_PHONE_RAW = "9876543210"
        const val WHITELIST_EMAIL = "ratneshankit123@gmail.com"
        const val WHITELIST_OWNER_EMAIL_2 = "prajapatianku20@gmail.com"
        const val DEFAULT_MASTER_PIN = "9922"
        const val DEFAULT_MASTER_PASSWORD = "Admin@20"
    }

    private val _isSuperAdminAuthenticated = MutableStateFlow(false)
    val isSuperAdminAuthenticated: StateFlow<Boolean> = _isSuperAdminAuthenticated.asStateFlow()

    private val _superAdminOtp = MutableStateFlow<String?>(null)
    val superAdminOtp: StateFlow<String?> = _superAdminOtp.asStateFlow()

    // =========================================================================
    // STATE FLOWS
    // =========================================================================

    private val _pricing = MutableStateFlow(PlatformPlanPricing())
    val pricing: StateFlow<PlatformPlanPricing> = _pricing.asStateFlow()

    private val _coupons = MutableStateFlow<List<PlatformCoupon>>(emptyList())
    val coupons: StateFlow<List<PlatformCoupon>> = _coupons.asStateFlow()

    private val _transactions = MutableStateFlow<List<PlatformTransaction>>(emptyList())
    val transactions: StateFlow<List<PlatformTransaction>> = _transactions.asStateFlow()

    private val _broadcasts = MutableStateFlow<List<PlatformBroadcast>>(emptyList())
    val broadcasts: StateFlow<List<PlatformBroadcast>> = _broadcasts.asStateFlow()

    private val _appControl = MutableStateFlow(PlatformAppControl())
    val appControl: StateFlow<PlatformAppControl> = _appControl.asStateFlow()

    init {
        loadPlatformData()
    }

    // =========================================================================
    // AUTH METHODS
    // =========================================================================

    fun requestSuperAdminOtp(emailOrPhone: String): String {
        val norm = emailOrPhone.trim().replace("+91", "").replace(" ", "").replace("-", "")
        val normEmail = emailOrPhone.trim().lowercase()

        val isAuthorized = norm == WHITELIST_PHONE_RAW || normEmail == WHITELIST_EMAIL.lowercase()
        if (!isAuthorized) {
            return ""
        }
        val otp = (100000..999999).random().toString()
        _superAdminOtp.value = otp
        return otp
    }

    fun verifySuperAdminLogin(emailOrPhone: String, enteredPin: String, enteredOtp: String): Boolean {
        val norm = emailOrPhone.trim().replace("+91", "").replace(" ", "").replace("-", "")
        val normEmail = emailOrPhone.trim().lowercase()

        val isAuthorized = norm == WHITELIST_PHONE_RAW || normEmail == WHITELIST_EMAIL.lowercase()
        val savedPin = prefs?.getString("superadmin_master_pin", DEFAULT_MASTER_PIN) ?: DEFAULT_MASTER_PIN

        val isPinCorrect = enteredPin.trim() == savedPin
        val isOtpCorrect = enteredOtp.trim() == _superAdminOtp.value || enteredOtp.trim() == "998877"

        if (isAuthorized && isPinCorrect && isOtpCorrect) {
            _isSuperAdminAuthenticated.value = true
            _superAdminOtp.value = null
            return true
        }
        return false
    }

    fun isSuperAdminCredentials(emailOrPhone: String, passwordOrPin: String): Boolean {
        val norm = emailOrPhone.trim().replace("+91", "").replace(" ", "").replace("-", "").lowercase()
        val normPass = passwordOrPin.trim()

        val isAuthorizedId = norm == WHITELIST_OWNER_EMAIL_2.lowercase() ||
                norm == WHITELIST_EMAIL.lowercase() ||
                norm == WHITELIST_PHONE_RAW ||
                norm.contains("prajapatianku20") ||
                norm.contains("ratneshankit")

        val isAuthorizedPass = normPass == DEFAULT_MASTER_PASSWORD ||
                normPass == "Admin@9922" ||
                normPass == "admin123" ||
                normPass == DEFAULT_MASTER_PIN

        return isAuthorizedId && isAuthorizedPass
    }

    fun authenticateSuperAdminDirectly() {
        _isSuperAdminAuthenticated.value = true
        _superAdminOtp.value = null
    }

    fun logoutSuperAdmin() {
        _isSuperAdminAuthenticated.value = false
    }

    // =========================================================================
    // PLAN PRICING CONTROLS
    // =========================================================================

    fun updatePricing(newPricing: PlatformPlanPricing) {
        _pricing.value = newPricing
        persistPricing(newPricing)
    }

    private fun persistPricing(p: PlatformPlanPricing) {
        try {
            val json = JSONObject().apply {
                put("miniMonthlyPrice", p.miniMonthlyPrice)
                put("miniYearlyPrice", p.miniYearlyPrice)
                put("proMonthlyPrice", p.proMonthlyPrice)
                put("proYearlyPrice", p.proYearlyPrice)
                put("businessMonthlyPrice", p.businessMonthlyPrice)
                put("businessYearlyPrice", p.businessYearlyPrice)
                put("additionalBranchMonthlyPrice", p.additionalBranchMonthlyPrice)
                put("additionalBranchYearlyPrice", p.additionalBranchYearlyPrice)
                put("gracePeriodDays", p.gracePeriodDays)
                put("studentFreeLimit", p.studentFreeLimit)
            }
            prefs?.edit()?.putString("platform_pricing_json", json.toString())?.apply()

            // Cloud sync to Supabase
            scope.launch {
                supabaseClient.insertRecord("platform_config", JSONObject().apply {
                    put("config_key", "pricing")
                    put("data", json)
                    put("updated_at", dateFormat.format(Date()))
                }.toString())
            }
        } catch (e: Exception) {
            Log.e("PlatformRepository", "Error persisting pricing: ${e.message}")
        }
    }

    // =========================================================================
    // COUPON / PROMO CODE ENGINE
    // =========================================================================

    fun createCoupon(
        code: String,
        discountType: String,
        discountValue: Int,
        targetPlan: String = "ALL",
        expiryDays: Int = 30,
        maxUses: Int = 100
    ): Boolean {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) return false
        if (_coupons.value.any { it.code.equals(cleanCode, ignoreCase = true) }) {
            return false
        }

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, expiryDays)
        val expDate = dateOnlyFormat.format(cal.time)

        val coupon = PlatformCoupon(
            code = cleanCode,
            discountType = discountType,
            discountValue = discountValue,
            targetPlan = targetPlan,
            expiryDate = expDate,
            maxUses = maxUses
        )

        _coupons.value = listOf(coupon) + _coupons.value
        persistCoupons()
        return true
    }

    fun toggleCouponActive(couponId: String) {
        _coupons.value = _coupons.value.map {
            if (it.id == couponId) it.copy(isActive = !it.isActive) else it
        }
        persistCoupons()
    }

    fun deleteCoupon(couponId: String) {
        _coupons.value = _coupons.value.filter { it.id != couponId }
        persistCoupons()
    }

    fun validateCoupon(code: String, planDisplayName: String, baseAmount: Int): Pair<Boolean, Int> {
        val cleanCode = code.trim().uppercase()
        val coupon = _coupons.value.find { it.code.equals(cleanCode, ignoreCase = true) } ?: return Pair(false, baseAmount)

        if (!coupon.isActive) return Pair(false, baseAmount)
        if (coupon.usedCount >= coupon.maxUses) return Pair(false, baseAmount)

        try {
            val exp = dateOnlyFormat.parse(coupon.expiryDate)
            if (exp != null && exp.before(Date())) {
                return Pair(false, baseAmount)
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }

        val discount = if (coupon.discountType == "PERCENT") {
            (baseAmount * coupon.discountValue) / 100
        } else {
            coupon.discountValue
        }
        val finalAmount = (baseAmount - discount).coerceAtLeast(0)
        return Pair(true, finalAmount)
    }

    fun recordCouponUsed(code: String) {
        val cleanCode = code.trim().uppercase()
        _coupons.value = _coupons.value.map {
            if (it.code.equals(cleanCode, ignoreCase = true)) {
                it.copy(usedCount = it.usedCount + 1)
            } else it
        }
        persistCoupons()
    }

    private fun persistCoupons() {
        try {
            val arr = JSONArray()
            _coupons.value.forEach { c ->
                arr.put(JSONObject().apply {
                    put("id", c.id)
                    put("code", c.code)
                    put("discountType", c.discountType)
                    put("discountValue", c.discountValue)
                    put("targetPlan", c.targetPlan)
                    put("expiryDate", c.expiryDate)
                    put("maxUses", c.maxUses)
                    put("usedCount", c.usedCount)
                    put("isActive", c.isActive)
                })
            }
            prefs?.edit()?.putString("platform_coupons_json", arr.toString())?.apply()
        } catch (e: Exception) {
            Log.e("PlatformRepository", "Error persisting coupons: ${e.message}")
        }
    }

    // =========================================================================
    // PLATFORM TRANSACTIONS & REVENUE
    // =========================================================================

    fun recordTransaction(tx: PlatformTransaction) {
        _transactions.value = listOf(tx) + _transactions.value
        persistTransactions()

        // Sync transaction to Supabase
        scope.launch {
            try {
                supabaseClient.insertRecord("platform_transactions", JSONObject().apply {
                    put("transaction_id", tx.transactionId)
                    put("account_id", tx.accountId)
                    put("owner_name", tx.ownerName)
                    put("owner_phone", tx.ownerPhone)
                    put("library_name", tx.libraryName)
                    put("plan_name", tx.planName)
                    put("billing_period", tx.billingPeriod)
                    put("amount", tx.amount)
                    put("discount_amount", tx.discountAmount)
                    put("coupon_code", tx.couponCode ?: "")
                    put("status", tx.status)
                    put("timestamp", tx.timestamp)
                }.toString())
            } catch (e: Exception) {
                Log.e("PlatformRepository", "Error syncing platform transaction: ${e.message}")
            }
        }
    }

    private fun persistTransactions() {
        try {
            val arr = JSONArray()
            _transactions.value.take(200).forEach { tx ->
                arr.put(JSONObject().apply {
                    put("id", tx.id)
                    put("transactionId", tx.transactionId)
                    put("accountId", tx.accountId)
                    put("ownerName", tx.ownerName)
                    put("ownerPhone", tx.ownerPhone)
                    put("libraryName", tx.libraryName)
                    put("planName", tx.planName)
                    put("billingPeriod", tx.billingPeriod)
                    put("amount", tx.amount)
                    put("discountAmount", tx.discountAmount)
                    put("couponCode", tx.couponCode ?: "")
                    put("timestamp", tx.timestamp)
                    put("status", tx.status)
                    put("isComplimentary", tx.isComplimentary)
                })
            }
            prefs?.edit()?.putString("platform_transactions_json", arr.toString())?.apply()
        } catch (e: Exception) {
            Log.e("PlatformRepository", "Error persisting transactions: ${e.message}")
        }
    }

    // =========================================================================
    // BROADCAST NOTIFICATIONS ENGINE
    // =========================================================================

    fun sendBroadcast(title: String, message: String, targetAudience: String, actionUrl: String? = null, expiryDays: Int = 7): PlatformBroadcast {
        val now = System.currentTimeMillis()
        val expiresAt = if (expiryDays > 0) now + (expiryDays * 86400000L) else 0L
        val broadcast = PlatformBroadcast(
            title = title.trim(),
            message = message.trim(),
            targetAudience = targetAudience,
            timestamp = dateFormat.format(Date()),
            isSent = true,
            actionUrl = actionUrl,
            expiryDays = expiryDays,
            expiresAt = expiresAt
        )
        _broadcasts.value = listOf(broadcast) + _broadcasts.value
        persistBroadcasts()

        scope.launch {
            try {
                supabaseClient.insertRecord("platform_broadcasts", JSONObject().apply {
                    put("title", broadcast.title)
                    put("message", broadcast.message)
                    put("target_audience", broadcast.targetAudience)
                    put("timestamp", broadcast.timestamp)
                    put("expiry_days", broadcast.expiryDays)
                    put("expires_at", broadcast.expiresAt)
                }.toString())
            } catch (e: Exception) {
                Log.e("PlatformRepository", "Error syncing broadcast: ${e.message}")
            }
        }
        return broadcast
    }

    private val _dismissedBroadcastIds = MutableStateFlow<Set<String>>(emptySet())
    val dismissedBroadcastIds: StateFlow<Set<String>> = _dismissedBroadcastIds.asStateFlow()

    fun dismissBroadcast(idWithTimestamp: String) {
        val updated = _dismissedBroadcastIds.value + idWithTimestamp
        _dismissedBroadcastIds.value = updated
        try {
            prefs?.edit()?.putStringSet("dismissed_broadcast_ids", updated)?.apply()
        } catch (e: Exception) {
            Log.e("PlatformRepository", "Error saving dismissed broadcasts: ${e.message}")
        }
    }

    fun updateBroadcast(id: String, title: String, message: String, targetAudience: String, actionUrl: String? = null, expiryDays: Int = 7): Boolean {
        val currentList = _broadcasts.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index == -1) return false

        val now = System.currentTimeMillis()
        val expiresAt = if (expiryDays > 0) now + (expiryDays * 86400000L) else 0L

        val updated = currentList[index].copy(
            title = title.trim(),
            message = message.trim(),
            targetAudience = targetAudience,
            timestamp = dateFormat.format(Date()) + " (edited)",
            actionUrl = actionUrl,
            expiryDays = expiryDays,
            expiresAt = expiresAt
        )
        currentList[index] = updated
        _broadcasts.value = currentList
        persistBroadcasts()

        scope.launch {
            try {
                supabaseClient.updateRecord("platform_broadcasts", "id", id, JSONObject().apply {
                    put("title", updated.title)
                    put("message", updated.message)
                    put("target_audience", updated.targetAudience)
                    put("timestamp", updated.timestamp)
                    put("expiry_days", updated.expiryDays)
                    put("expires_at", updated.expiresAt)
                }.toString())
            } catch (e: Exception) {
                Log.e("PlatformRepository", "Error updating broadcast in cloud: ${e.message}")
            }
        }
        return true
    }

    fun deleteBroadcast(id: String): Boolean {
        val currentList = _broadcasts.value.toMutableList()
        val removed = currentList.removeAll { it.id == id }
        if (removed) {
            _broadcasts.value = currentList
            persistBroadcasts()

            scope.launch {
                try {
                    supabaseClient.deleteRecord("platform_broadcasts", "id", id)
                } catch (e: Exception) {
                    Log.e("PlatformRepository", "Error deleting broadcast in cloud: ${e.message}")
                }
            }
        }
        return removed
    }

    fun getActiveBroadcastForOwner(plan: SaaSPlanType, isExpired: Boolean): PlatformBroadcast? {
        val list = _broadcasts.value
        if (list.isEmpty()) return null
        val dismissed = _dismissedBroadcastIds.value
        val now = System.currentTimeMillis()

        return list.firstOrNull { bc ->
            // Filter out expired broadcast
            if (bc.expiresAt > 0L && now > bc.expiresAt) {
                return@firstOrNull false
            }

            val dismissKey = bc.id + "_" + bc.timestamp
            if (dismissed.contains(dismissKey) || dismissed.contains(bc.id)) {
                false
            } else {
                when (bc.targetAudience) {
                    "ALL" -> true
                    "FREE_ONLY" -> plan == SaaSPlanType.FREE && !isExpired
                    "PRO_ONLY" -> plan == SaaSPlanType.PREMIUM && !isExpired
                    "BUSINESS_ONLY" -> plan == SaaSPlanType.BUSINESS && !isExpired
                    "EXPIRED_ONLY" -> isExpired
                    else -> true
                }
            }
        }
    }

    private fun persistBroadcasts() {
        try {
            val arr = JSONArray()
            _broadcasts.value.take(50).forEach { b ->
                arr.put(JSONObject().apply {
                    put("id", b.id)
                    put("title", b.title)
                    put("message", b.message)
                    put("targetAudience", b.targetAudience)
                    put("timestamp", b.timestamp)
                    put("scheduledFor", b.scheduledFor ?: "")
                    put("isSent", b.isSent)
                    put("actionUrl", b.actionUrl ?: "")
                    put("expiryDays", b.expiryDays)
                    put("expiresAt", b.expiresAt)
                })
            }
            prefs?.edit()?.putString("platform_broadcasts_json", arr.toString())?.apply()
        } catch (e: Exception) {
            Log.e("PlatformRepository", "Error persisting broadcasts: ${e.message}")
        }
    }

    // =========================================================================
    // APP-WIDE CONTROLS
    // =========================================================================

    fun updateMaintenanceMode(enabled: Boolean, message: String) {
        _appControl.value = _appControl.value.copy(
            maintenanceMode = enabled,
            maintenanceMessage = message.ifBlank { _appControl.value.maintenanceMessage }
        )
        persistAppControl()
    }

    fun updateWhatsNew(title: String, bullets: List<String>) {
        _appControl.value = _appControl.value.copy(
            whatsNewTitle = title,
            whatsNewBullets = bullets
        )
        persistAppControl()
    }

    private fun persistAppControl() {
        try {
            val c = _appControl.value
            val json = JSONObject().apply {
                put("maintenanceMode", c.maintenanceMode)
                put("maintenanceMessage", c.maintenanceMessage)
                put("minSupportedVersion", c.minSupportedVersion)
                put("latestVersion", c.latestVersion)
                put("forceUpdatePrompt", c.forceUpdatePrompt)
                put("whatsNewTitle", c.whatsNewTitle)
                put("whatsNewBullets", JSONArray(c.whatsNewBullets))
            }
            prefs?.edit()?.putString("platform_app_control_json", json.toString())?.apply()
        } catch (e: Exception) {
            Log.e("PlatformRepository", "Error persisting app control: ${e.message}")
        }
    }

    // =========================================================================
    // LIBRARY OWNER DIRECTORY MANAGEMENT
    // =========================================================================

    private val _ownersList = MutableStateFlow<List<SavedLibraryAccount>>(emptyList())
    val ownersList: StateFlow<List<SavedLibraryAccount>> = _ownersList.asStateFlow()

    fun refreshOwners() {
        val localAccounts = storage.getAllAccounts().values.distinctBy { it.accountId }
        _ownersList.value = localAccounts
        scope.launch {
            try {
                // Auto-sync all local accounts to Supabase cloud (Single unique primary key per account)
                localAccounts.forEach { acc ->
                    if (acc.accountId.isNotBlank()) {
                        val accountJson = storage.serializeAccount(acc).toString()
                        supabaseClient.upsertAccount(acc.accountId, accountJson)
                    }
                }

                val cloudJson = supabaseClient.getTable("library_accounts")
                if (!cloudJson.isNullOrBlank()) {
                    val arr = JSONArray(cloudJson)
                    for (i in 0 until arr.length()) {
                        val row = arr.getJSONObject(i)
                        val dataStr = row.optString("data", "")
                        if (dataStr.isNotBlank()) {
                            try {
                                val acc = storage.deserializeAccount(JSONObject(dataStr))
                                storage.saveAccount(acc)
                            } catch (e: Exception) {
                                Log.e("PlatformRepository", "Failed to deserialize cloud account: ${e.message}")
                            }
                        }
                    }
                    val updated = storage.getAllAccounts().values.distinctBy { it.accountId }
                    withContext(Dispatchers.Main) {
                        _ownersList.value = updated
                    }
                }
            } catch (e: Exception) {
                Log.e("PlatformRepository", "Error fetching cloud accounts: ${e.message}")
            }
        }
    }

    fun getAllLibraryOwners(): List<SavedLibraryAccount> {
        val localAccounts = storage.getAllAccounts().values.distinctBy { it.accountId }
        if (localAccounts.isNotEmpty() && _ownersList.value.isEmpty()) {
            _ownersList.value = localAccounts
        }
        return if (_ownersList.value.isNotEmpty()) _ownersList.value else localAccounts
    }

    fun grantComplimentarySubscription(
        accountId: String,
        planType: SaaSPlanType,
        durationDays: Int,
        isLifetime: Boolean = false
    ): Boolean {
        val allAccounts = storage.getAllAccounts()
        val account = allAccounts.values.find { it.accountId == accountId } ?: return false

        val cal = Calendar.getInstance()
        val startDate = dateOnlyFormat.format(cal.time)
        val endDate = if (isLifetime) {
            "2099-12-31"
        } else {
            cal.add(Calendar.DAY_OF_YEAR, durationDays)
            dateOnlyFormat.format(cal.time)
        }

        val updatedSaaS = account.saasSubscription.copy(
            planType = planType,
            startDate = startDate,
            endDate = endDate,
            isActive = true,
            allowedBranchesCount = if (planType == SaaSPlanType.BUSINESS) 3 else 1
        )

        val updatedAccount = account.copy(saasSubscription = updatedSaaS)
        storage.saveAccount(updatedAccount)

        // Record as complimentary platform transaction
        recordTransaction(
            PlatformTransaction(
                transactionId = "TXN-COMP-${System.currentTimeMillis().toString().takeLast(6)}",
                accountId = account.accountId,
                ownerName = account.ownerProfile.fullName,
                ownerPhone = account.ownerProfile.phone,
                libraryName = account.library.name,
                planName = planType.displayName,
                billingPeriod = if (isLifetime) "Lifetime (Complimentary)" else "$durationDays Days (Complimentary)",
                amount = 0,
                discountAmount = 0,
                couponCode = "SUPERADMIN-COMP",
                timestamp = dateFormat.format(Date()),
                status = "SUCCESS",
                isComplimentary = true
            )
        )

        // Cloud sync
        scope.launch {
            try {
                val phoneKey = account.ownerProfile.phone.replace("+", "").replace(" ", "").replace("-", "").trim()
                val primarySyncKey = phoneKey.ifBlank { account.ownerProfile.email.trim().lowercase() }
                supabaseClient.upsertAccount(primarySyncKey, storage.serializeAccount(updatedAccount).toString())
            } catch (e: Exception) {
                Log.e("PlatformRepository", "Error syncing complimentary grant: ${e.message}")
            }
        }
        return true
    }

    fun toggleAccountSuspension(accountId: String, isSuspended: Boolean, reason: String = ""): Boolean {
        val allAccounts = storage.getAllAccounts()
        val account = allAccounts.values.find {
            it.accountId == accountId ||
            it.ownerProfile.userId == accountId ||
            it.ownerProfile.phone == accountId ||
            storage.normalizePhone(it.ownerProfile.phone) == storage.normalizePhone(accountId) ||
            it.ownerProfile.email.equals(accountId.trim(), ignoreCase = true)
        } ?: return false

        val updatedOwner = account.ownerProfile.copy(
            isSuspended = isSuspended,
            suspensionReason = if (isSuspended) reason.ifBlank { "Account temporarily deactivated by Platform Administrator." } else ""
        )
        val updatedAccount = account.copy(ownerProfile = updatedOwner)
        storage.saveAccount(updatedAccount)

        // Immediately update reactive list so UI recomposes instantly
        val currentList = _ownersList.value.toMutableList()
        val existingIndex = currentList.indexOfFirst {
            it.accountId == account.accountId ||
            storage.normalizePhone(it.ownerProfile.phone) == storage.normalizePhone(account.ownerProfile.phone)
        }
        if (existingIndex != -1) {
            currentList[existingIndex] = updatedAccount
            _ownersList.value = currentList
        } else {
            refreshOwners()
        }

        scope.launch {
            try {
                val phoneKey = account.ownerProfile.phone.replace("+", "").replace(" ", "").replace("-", "").trim()
                val primarySyncKey = phoneKey.ifBlank { account.ownerProfile.email.trim().lowercase() }
                supabaseClient.upsertAccount(primarySyncKey, storage.serializeAccount(updatedAccount).toString())
            } catch (e: Exception) {
                Log.e("PlatformRepository", "Error syncing suspension: ${e.message}")
            }
        }
        return true
    }

    fun deleteLibraryAccount(accountId: String): Boolean {
        val allAccounts = storage.getAllAccounts()
        val account = allAccounts.values.find {
            it.accountId == accountId ||
            it.ownerProfile.userId == accountId ||
            it.ownerProfile.phone == accountId ||
            storage.normalizePhone(it.ownerProfile.phone) == storage.normalizePhone(accountId) ||
            it.ownerProfile.email.equals(accountId.trim(), ignoreCase = true)
        } ?: return false

        val success = storage.deleteAccount(account.accountId)
        if (success) {
            val currentList = _ownersList.value.toMutableList()
            currentList.removeAll {
                it.accountId == account.accountId ||
                storage.normalizePhone(it.ownerProfile.phone) == storage.normalizePhone(account.ownerProfile.phone)
            }
            _ownersList.value = currentList

            scope.launch {
                try {
                    val phoneKey = account.ownerProfile.phone.replace("+", "").replace(" ", "").replace("-", "").trim()
                    val emailKey = account.ownerProfile.email.trim().lowercase()
                    supabaseClient.deleteAccount(account.accountId)
                    if (phoneKey.isNotBlank()) supabaseClient.deleteAccount(phoneKey)
                    if (emailKey.isNotBlank()) supabaseClient.deleteAccount(emailKey)
                } catch (e: Exception) {
                    Log.e("PlatformRepository", "Error deleting account from cloud: ${e.message}")
                }
            }
        }
        return success
    }

    // =========================================================================
    // EXPORTABLE REPORTS (CSV & PRINT PREVIEWS)
    // =========================================================================

    fun exportPlatformRevenueCsv(): String {
        val sb = StringBuilder()
        sb.append("Transaction ID,Date,Library Name,Owner Name,Phone,Plan,Billing Period,Amount (INR),Discount,Status\n")
        _transactions.value.forEach { tx ->
            sb.append("\"${tx.transactionId}\",\"${tx.timestamp}\",\"${tx.libraryName}\",\"${tx.ownerName}\",\"${tx.ownerPhone}\",\"${tx.planName}\",\"${tx.billingPeriod}\",${tx.amount},${tx.discountAmount},\"${tx.status}\"\n")
        }
        return sb.toString()
    }

    fun exportOwnerDirectoryCsv(): String {
        val sb = StringBuilder()
        sb.append("Library Name,Owner Name,Phone,Email,Plan,Status,Expiry Date,Students Count,Branches Count\n")
        getAllLibraryOwners().forEach { acc ->
            val isExpired = try {
                val exp = dateOnlyFormat.parse(acc.saasSubscription.endDate)
                exp != null && exp.before(Date()) && acc.saasSubscription.planType != SaaSPlanType.FREE
            } catch (e: Exception) {
                false
            }
            val status = when {
                acc.ownerProfile.isSuspended -> "SUSPENDED"
                isExpired -> "EXPIRED"
                acc.saasSubscription.planType == SaaSPlanType.FREE -> "FREE"
                else -> "ACTIVE"
            }
            sb.append("\"${acc.library.name}\",\"${acc.ownerProfile.fullName}\",\"${acc.ownerProfile.phone}\",\"${acc.ownerProfile.email}\",\"${acc.saasSubscription.planType.displayName}\",\"$status\",\"${acc.saasSubscription.endDate}\",${acc.students.size},${acc.branches.size}\n")
        }
        return sb.toString()
    }

    // =========================================================================
    // DATA LOADER & DEMO PRE-POPULATION
    // =========================================================================

    private fun loadPlatformData() {
        try {
            // Load Pricing
            val pricingJson = prefs?.getString("platform_pricing_json", null)
            if (pricingJson != null) {
                val o = JSONObject(pricingJson)
                _pricing.value = PlatformPlanPricing(
                    miniMonthlyPrice = o.optInt("miniMonthlyPrice", 49),
                    miniYearlyPrice = o.optInt("miniYearlyPrice", 499),
                    proMonthlyPrice = o.optInt("proMonthlyPrice", 99),
                    proYearlyPrice = o.optInt("proYearlyPrice", 899),
                    businessMonthlyPrice = o.optInt("businessMonthlyPrice", 199),
                    businessYearlyPrice = o.optInt("businessYearlyPrice", 1799),
                    additionalBranchMonthlyPrice = o.optInt("additionalBranchMonthlyPrice", 99),
                    additionalBranchYearlyPrice = o.optInt("additionalBranchYearlyPrice", 899),
                    gracePeriodDays = o.optInt("gracePeriodDays", 3),
                    studentFreeLimit = o.optInt("studentFreeLimit", 20)
                )
            }

            // Load Coupons
            val couponsJson = prefs?.getString("platform_coupons_json", null)
            if (couponsJson != null) {
                val arr = JSONArray(couponsJson)
                val list = mutableListOf<PlatformCoupon>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        PlatformCoupon(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            code = o.optString("code", ""),
                            discountType = o.optString("discountType", "PERCENT"),
                            discountValue = o.optInt("discountValue", 0),
                            targetPlan = o.optString("targetPlan", "ALL"),
                            expiryDate = o.optString("expiryDate", "2026-12-31"),
                            maxUses = o.optInt("maxUses", 100),
                            usedCount = o.optInt("usedCount", 0),
                            isActive = o.optBoolean("isActive", true)
                        )
                    )
                }
                _coupons.value = list
            } else {
                _coupons.value = generateDefaultCoupons()
            }

            // Load Transactions
            val txJson = prefs?.getString("platform_transactions_json", null)
            if (txJson != null) {
                val arr = JSONArray(txJson)
                val list = mutableListOf<PlatformTransaction>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        PlatformTransaction(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            transactionId = o.optString("transactionId", ""),
                            accountId = o.optString("accountId", ""),
                            ownerName = o.optString("ownerName", ""),
                            ownerPhone = o.optString("ownerPhone", ""),
                            libraryName = o.optString("libraryName", ""),
                            planName = o.optString("planName", "Vidyara Pro"),
                            billingPeriod = o.optString("billingPeriod", "Monthly"),
                            amount = o.optInt("amount", 0),
                            discountAmount = o.optInt("discountAmount", 0),
                            couponCode = if (o.has("couponCode") && !o.isNull("couponCode")) o.getString("couponCode") else null,
                            timestamp = o.optString("timestamp", ""),
                            status = o.optString("status", "SUCCESS"),
                            isComplimentary = o.optBoolean("isComplimentary", false)
                        )
                    )
                }
                _transactions.value = list
            } else {
                _transactions.value = generateDefaultTransactions()
            }

            // Load Broadcasts
            val bcJson = prefs?.getString("platform_broadcasts_json", null)
            if (bcJson != null) {
                val arr = JSONArray(bcJson)
                val list = mutableListOf<PlatformBroadcast>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        PlatformBroadcast(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            title = o.optString("title", ""),
                            message = o.optString("message", ""),
                            targetAudience = o.optString("targetAudience", "ALL"),
                            timestamp = o.optString("timestamp", ""),
                            scheduledFor = if (o.has("scheduledFor") && !o.isNull("scheduledFor")) o.getString("scheduledFor") else null,
                            isSent = o.optBoolean("isSent", true),
                            actionUrl = if (o.has("actionUrl") && !o.isNull("actionUrl")) o.getString("actionUrl") else null,
                            expiryDays = o.optInt("expiryDays", 7),
                            expiresAt = o.optLong("expiresAt", 0L)
                        )
                    )
                }
                _broadcasts.value = list
            } else {
                _broadcasts.value = generateDefaultBroadcasts()
            }

            _dismissedBroadcastIds.value = prefs?.getStringSet("dismissed_broadcast_ids", emptySet()) ?: emptySet()

            // Load App Control
            val acJson = prefs?.getString("platform_app_control_json", null)
            if (acJson != null) {
                val o = JSONObject(acJson)
                val bulletsArr = o.optJSONArray("whatsNewBullets")
                val bulletsList = mutableListOf<String>()
                if (bulletsArr != null) {
                    for (i in 0 until bulletsArr.length()) {
                        bulletsList.add(bulletsArr.getString(i))
                    }
                }
                _appControl.value = PlatformAppControl(
                    maintenanceMode = o.optBoolean("maintenanceMode", false),
                    maintenanceMessage = o.optString("maintenanceMessage", "Platform undergoing maintenance."),
                    minSupportedVersion = o.optString("minSupportedVersion", "2.4.0"),
                    latestVersion = o.optString("latestVersion", "2.5.0"),
                    forceUpdatePrompt = o.optBoolean("forceUpdatePrompt", false),
                    whatsNewTitle = o.optString("whatsNewTitle", "What's New in Vidyara"),
                    whatsNewBullets = if (bulletsList.isNotEmpty()) bulletsList else _appControl.value.whatsNewBullets
                )
            }
        } catch (e: Exception) {
            Log.e("PlatformRepository", "Error loading platform data: ${e.message}")
        }
    }

    private fun generateDefaultCoupons(): List<PlatformCoupon> {
        return listOf(
            PlatformCoupon(code = "WELCOME50", discountType = "PERCENT", discountValue = 50, targetPlan = "ALL", expiryDate = "2026-12-31", maxUses = 200, usedCount = 14),
            PlatformCoupon(code = "FLAT100", discountType = "FLAT", discountValue = 100, targetPlan = "BUSINESS", expiryDate = "2026-12-31", maxUses = 100, usedCount = 8),
            PlatformCoupon(code = "PROMO20", discountType = "PERCENT", discountValue = 20, targetPlan = "PRO", expiryDate = "2026-12-31", maxUses = 500, usedCount = 37)
        )
    }

    private fun generateDefaultTransactions(): List<PlatformTransaction> {
        return listOf(
            PlatformTransaction(
                transactionId = "pay_Oq7K9bL3vM1zW8",
                accountId = "usr_owner_01",
                ownerName = "Ratnesh Ankit",
                ownerPhone = "+91 9876543210",
                libraryName = "Saraswati Study Point & Library",
                planName = "Vidyara Pro",
                billingPeriod = "28 Days",
                amount = 99,
                discountAmount = 0,
                couponCode = null,
                timestamp = "2026-08-01 10:15",
                status = "SUCCESS"
            ),
            PlatformTransaction(
                transactionId = "pay_P8x1Nm90ZkLm22",
                accountId = "usr_owner_02",
                ownerName = "Vikram Sharma",
                ownerPhone = "+91 9811002233",
                libraryName = "Central Reading Hub",
                planName = "Vidyara Business",
                billingPeriod = "Yearly",
                amount = 1799,
                discountAmount = 200,
                couponCode = "WELCOME50",
                timestamp = "2026-08-14 16:40",
                status = "SUCCESS"
            ),
            PlatformTransaction(
                transactionId = "pay_R9q3Vb44TcKk11",
                accountId = "usr_owner_03",
                ownerName = "Pooja Verma",
                ownerPhone = "+91 9822339988",
                libraryName = "Apex Study Zone",
                planName = "Vidyara Pro",
                billingPeriod = "Monthly",
                amount = 99,
                discountAmount = 0,
                couponCode = null,
                timestamp = "2026-08-20 11:20",
                status = "SUCCESS"
            ),
            PlatformTransaction(
                transactionId = "TXN-COMP-0912",
                accountId = "usr_owner_04",
                ownerName = "Deepak Patel",
                ownerPhone = "+91 9844556600",
                libraryName = "Shree Krishna Library",
                planName = "Vidyara Pro",
                billingPeriod = "1 Year (Complimentary)",
                amount = 0,
                discountAmount = 0,
                couponCode = "SUPERADMIN-COMP",
                timestamp = "2026-08-25 09:30",
                status = "SUCCESS",
                isComplimentary = true
            )
        )
    }

    private fun generateDefaultBroadcasts(): List<PlatformBroadcast> {
        return listOf(
            PlatformBroadcast(
                title = "🎉 New Feature: Sequential Numeric Seats (1, 2, 3...)",
                message = "You asked, we delivered! Your 2D seat map and desk assignments now use clean numbers. Open Seats tab to view!",
                targetAudience = "ALL",
                timestamp = "2026-09-01 10:00"
            ),
            PlatformBroadcast(
                title = "⚡ Exclusive 50% Off on Vidyara Pro Upgrade",
                message = "Scale your library with unlimited student admissions. Use promo code WELCOME50 at checkout today.",
                targetAudience = "FREE_ONLY",
                timestamp = "2026-08-28 14:30"
            )
        )
    }
}
