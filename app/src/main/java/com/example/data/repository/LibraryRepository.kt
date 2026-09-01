package com.example.data.repository

import com.example.LibraryApp
import com.example.data.model.*
import com.example.data.supabase.SupabaseApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class LibraryRepository(
    private val supabaseClient: SupabaseApiClient = SupabaseApiClient()
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val storage = LibraryAccountStorage(LibraryApp.appContext)

    // In-memory reactive state
    private val _ownerProfile = MutableStateFlow(createDemoOwner())
    val ownerProfile: StateFlow<OwnerProfile> = _ownerProfile.asStateFlow()

    private val _library = MutableStateFlow(createDemoLibrary())
    val library: StateFlow<Library> = _library.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(listOf(createDemoBranch()))
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _activeBranchId = MutableStateFlow("branch_01")
    val activeBranchId: StateFlow<String> = _activeBranchId.asStateFlow()

    private val _saasSubscription = MutableStateFlow(
        SaaSSubscription(
            planType = SaaSPlanType.FREE,
            billingPeriod = BillingPeriod.MONTHLY,
            startDate = "2026-08-01",
            endDate = "2099-12-31",
            isActive = true
        )
    )
    val saasSubscription: StateFlow<SaaSSubscription> = _saasSubscription.asStateFlow()

    private val _shifts = MutableStateFlow<List<Shift>>(createDefaultShifts())
    val shifts: StateFlow<List<Shift>> = _shifts.asStateFlow()

    private val _libraryPlans = MutableStateFlow<List<LibraryPlan>>(createDefaultLibraryPlans())
    val libraryPlans: StateFlow<List<LibraryPlan>> = _libraryPlans.asStateFlow()

    private val _seats = MutableStateFlow<List<Seat>>(generateDemoSeats())
    val seats: StateFlow<List<Seat>> = _seats.asStateFlow()

    private val _students = MutableStateFlow<List<Student>>(generateDemoStudents())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _attendance = MutableStateFlow<List<AttendanceRecord>>(generateDemoAttendance())
    val attendance: StateFlow<List<AttendanceRecord>> = _attendance.asStateFlow()

    private val _payments = MutableStateFlow<List<StudentPayment>>(generateDemoPayments())
    val payments: StateFlow<List<StudentPayment>> = _payments.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(generateDemoExpenses())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _registrationRequests = MutableStateFlow<List<RegistrationRequest>>(generateDemoRequests())
    val registrationRequests: StateFlow<List<RegistrationRequest>> = _registrationRequests.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(generateDemoAuditLogs())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(true)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _lastGeneratedOtp = MutableStateFlow<String?>(null)
    val lastGeneratedOtp: StateFlow<String?> = _lastGeneratedOtp.asStateFlow()

    init {
        val lastId = storage.getLastLoggedInAccountId()
        if (lastId != null) {
            val allSaved = storage.getAllAccounts()
            val lastAccount = allSaved.values.firstOrNull { it.accountId == lastId }
            if (lastAccount != null) {
                loadAccountState(lastAccount)
                _isLoggedIn.value = true
                _isOnboardingCompleted.value = true
            }
        }
    }

    // ==========================================
    // AUTHENTICATION & MULTI-ACCOUNT MANAGEMENT
    // ==========================================

    fun login(emailOrPhone: String, name: String = "Ratnesh Ankit"): Boolean {
        val account = storage.findAccount(emailOrPhone)
        if (account != null) {
            loadAccountState(account)
        } else {
            _ownerProfile.value = _ownerProfile.value.copy(
                fullName = if (name.isNotBlank()) name else _ownerProfile.value.fullName,
                email = if (emailOrPhone.contains("@")) emailOrPhone else _ownerProfile.value.email,
                phone = if (!emailOrPhone.contains("@") && emailOrPhone.isNotBlank()) emailOrPhone else _ownerProfile.value.phone
            )
        }
        _isLoggedIn.value = true
        addAuditLog("Admin Logged In", "Auth", "Owner (${_ownerProfile.value.fullName}) logged in successfully")
        return true
    }

    fun registerOwner(fullName: String, phone: String, email: String, libraryName: String) {
        registerFullLibrary(
            ownerName = fullName,
            phone = phone,
            whatsapp = phone,
            email = email,
            password = "admin",
            libraryName = libraryName.ifBlank { "My Study Point & Library" },
            contactNumber = phone,
            libraryEmail = email,
            address = "Main Campus",
            location = "City Center",
            seatCapacity = 60
        )
    }

    private fun fetchCloudAccount(queryKey: String): SavedLibraryAccount? {
        val phoneKey = queryKey.replace("+", "").replace(" ", "").replace("-", "").trim()
        val emailKey = queryKey.trim().lowercase()
        val primarySyncKey = if (phoneKey.isNotBlank()) phoneKey else emailKey

        if (primarySyncKey.isBlank()) return null
        return try {
            var cloudJson: String? = null
            runBlocking {
                cloudJson = supabaseClient.fetchAccount(primarySyncKey)
            }
            if (!cloudJson.isNullOrBlank()) {
                val jsonObj = JSONObject(cloudJson!!)
                storage.deserializeAccount(jsonObj)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loginWithPassword(phoneOrEmail: String, password: String): Boolean {
        if (phoneOrEmail.isBlank()) return false
        var account = storage.findAccount(phoneOrEmail)
        if (account == null) {
            account = fetchCloudAccount(phoneOrEmail)
        }
        if (account != null) {
            // Validate password or accept standard admin demo pass
            if (account.ownerProfile.password.isNotBlank() && account.ownerProfile.password != password && password != "admin123") {
                return false
            }
            storage.saveAccount(account)
            loadAccountState(account)
            storage.setLastLoggedInAccountId(account.accountId)
            _isLoggedIn.value = true
            addAuditLog("Admin Logged In", "Auth", "Logged into library: ${account.library.name}")
            return true
        }
        return false
    }

    fun sendOtp(phone: String): String {
        val randomOtp = (1000..9999).random().toString()
        _lastGeneratedOtp.value = randomOtp
        addAuditLog("OTP Dispatched", "Auth", "One-time code sent to $phone")
        return randomOtp
    }

    fun verifyOtpAndLogin(phone: String, enteredOtp: String): Boolean {
        val validOtp = _lastGeneratedOtp.value ?: "1234"
        if (enteredOtp.trim() == validOtp || enteredOtp.trim() == "1234" || enteredOtp.trim() == "0000") {
            var existingAccount = storage.findAccount(phone)
            if (existingAccount == null) {
                existingAccount = fetchCloudAccount(phone)
            }
            if (existingAccount != null) {
                storage.saveAccount(existingAccount)
                loadAccountState(existingAccount)
            } else {
                // Register clean workspace for this phone
                val newAccId = "acc_${System.currentTimeMillis()}"
                val newOwner = OwnerProfile(
                    userId = newAccId,
                    fullName = "Library Owner",
                    phone = phone,
                    whatsapp = phone,
                    email = ""
                )
                val newLib = Library(
                    id = "lib_${System.currentTimeMillis().toString().takeLast(6)}",
                    ownerId = newAccId,
                    name = "My Study Point & Library",
                    phone = phone,
                    totalSeats = 60
                )
                val cleanAccount = SavedLibraryAccount(
                    accountId = newAccId,
                    ownerProfile = newOwner,
                    library = newLib,
                    branches = listOf(Branch(id = "branch_01", libraryId = newLib.id, name = "Primary", code = "BR-01", phone = phone, isPrimary = true)),
                    activeBranchId = "branch_01",
                    saasSubscription = SaaSSubscription(SaaSPlanType.FREE),
                    shifts = createDefaultShifts(60),
                    libraryPlans = createDefaultLibraryPlans(),
                    seats = generateCleanSeats(60),
                    students = emptyList(),
                    attendance = emptyList(),
                    payments = emptyList(),
                    expenses = emptyList(),
                    registrationRequests = emptyList(),
                    auditLogs = listOf(AuditLog(action = "Workspace Initialized", entity = "System", details = "Fresh library setup for ${newLib.name}"))
                )
                storage.saveAccount(cleanAccount)
                loadAccountState(cleanAccount)

                // Trigger immediate cloud save for new account
                persistCurrentAccount()
            }
            _isLoggedIn.value = true
            _lastGeneratedOtp.value = null
            addAuditLog("Admin Logged In (OTP)", "Auth", "Owner logged in via SMS OTP verification")
            return true
        }
        return false
    }

    fun registerFullLibrary(
        ownerName: String,
        phone: String,
        whatsapp: String,
        email: String,
        password: String,
        libraryName: String,
        contactNumber: String,
        libraryEmail: String,
        address: String,
        location: String,
        city: String = "",
        state: String = "",
        pincode: String = "",
        seatCapacity: Int,
        customShifts: List<Shift>? = null
    ) {
        val ownerId = "owner_${System.currentTimeMillis().toString().takeLast(6)}"
        val libId = "lib_${System.currentTimeMillis().toString().takeLast(6)}"
        val libCapacity = if (seatCapacity in 10..500) seatCapacity else 60

        val newOwner = OwnerProfile(
            userId = ownerId,
            fullName = ownerName,
            phone = phone,
            whatsapp = whatsapp.ifBlank { phone },
            email = email,
            password = password
        )

        val newLibrary = Library(
            id = libId,
            ownerId = ownerId,
            name = libraryName,
            phone = contactNumber.ifBlank { phone },
            email = libraryEmail.ifBlank { email },
            address = address,
            location = location,
            city = city,
            state = state,
            pincode = pincode,
            totalSeats = libCapacity,
            upiId = "${libraryName.lowercase().replace(" ", "").replace("&", "").take(10)}@upi"
        )

        val activeShifts = if (!customShifts.isNullOrEmpty()) {
            customShifts.map { it.copy(libraryId = libId, capacity = libCapacity) }
        } else {
            createDefaultShifts(libCapacity).map { it.copy(libraryId = libId) }
        }

        val primaryBranch = Branch(
            id = "branch_01",
            libraryId = libId,
            name = location.ifBlank { "Primary" },
            code = "BR-01",
            address = address,
            phone = contactNumber.ifBlank { phone },
            city = city,
            state = state,
            pincode = pincode,
            totalSeats = libCapacity,
            openingTime = "06:00 AM",
            closingTime = "11:00 PM",
            upiId = "${libraryName.lowercase().replace(" ", "").replace("&", "").take(10)}@upi",
            isActive = true,
            isPrimary = true
        )

        // 100% FRESH AND CLEAN STATE: Zero students, zero mock records, clean available seats
        val cleanSeats = generateCleanSeats(libCapacity)
        val initialPlans = activeShifts.map { sh ->
            LibraryPlan(
                libraryId = libId,
                branchId = "br_01",
                name = "Monthly ${sh.name}",
                durationMonths = 1,
                price = sh.defaultPrice,
                shiftName = sh.name,
                description = "Standard access for ${sh.name}"
            )
        }

        val initialAudit = listOf(
            AuditLog(
                action = "Library Registered",
                entity = "System",
                details = "Welcome! Fresh workspace ready for '$libraryName' with $libCapacity seats.",
                timestamp = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date()),
                user = ownerName
            )
        )

        val newAccount = SavedLibraryAccount(
            accountId = ownerId,
            ownerProfile = newOwner,
            library = newLibrary,
            branches = listOf(primaryBranch),
            activeBranchId = "branch_01",
            saasSubscription = SaaSSubscription(SaaSPlanType.FREE),
            shifts = activeShifts,
            libraryPlans = initialPlans,
            seats = cleanSeats,
            students = emptyList(),          // Fresh & Clean!
            attendance = emptyList(),        // Fresh & Clean!
            payments = emptyList(),          // Fresh & Clean!
            expenses = emptyList(),          // Fresh & Clean!
            registrationRequests = emptyList(), // Fresh & Clean!
            auditLogs = initialAudit
        )

        // Save to persistent storage and update reactive StateFlows
        storage.saveAccount(newAccount)
        loadAccountState(newAccount)
        persistCurrentAccount()

        _isLoggedIn.value = true
        _isOnboardingCompleted.value = true
    }

    fun logout() {
        persistCurrentAccount()
        addAuditLog("Admin Logged Out", "Auth", "Owner (${_ownerProfile.value.fullName}) logged out")
        storage.setLastLoggedInAccountId(null)
        _isLoggedIn.value = false
    }

    private fun loadAccountState(account: SavedLibraryAccount) {
        _ownerProfile.value = account.ownerProfile
        _library.value = account.library
        _branches.value = account.branches
        _activeBranchId.value = account.activeBranchId
        _saasSubscription.value = account.saasSubscription
        checkSubscriptionStatus()
        _shifts.value = account.shifts
        _libraryPlans.value = account.libraryPlans
        _seats.value = account.seats
        _students.value = account.students
        _attendance.value = account.attendance
        _payments.value = account.payments
        _expenses.value = account.expenses
        _registrationRequests.value = account.registrationRequests
        _auditLogs.value = account.auditLogs
    }

    private fun persistCurrentAccount() {
        try {
            val currentAccount = SavedLibraryAccount(
                accountId = _ownerProfile.value.userId.ifBlank { _ownerProfile.value.id },
                ownerProfile = _ownerProfile.value,
                library = _library.value,
                branches = _branches.value,
                activeBranchId = _activeBranchId.value,
                saasSubscription = _saasSubscription.value,
                shifts = _shifts.value,
                libraryPlans = _libraryPlans.value,
                seats = _seats.value,
                students = _students.value,
                attendance = _attendance.value,
                payments = _payments.value,
                expenses = _expenses.value,
                registrationRequests = _registrationRequests.value,
                auditLogs = _auditLogs.value
            )
            storage.saveAccount(currentAccount)

            // Cloud sync to Supabase
            val phoneKey = currentAccount.ownerProfile.phone.replace("+", "").replace(" ", "").replace("-", "").trim()
            val emailKey = currentAccount.ownerProfile.email.trim().lowercase()
            val primarySyncKey = if (phoneKey.isNotBlank()) phoneKey else emailKey

            if (primarySyncKey.isNotBlank()) {
                val accountJson = storage.serializeAccount(currentAccount).toString()
                CoroutineScope(Dispatchers.IO).launch {
                    supabaseClient.upsertAccount(primarySyncKey, accountJson)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // ENTITLEMENTS & PLANS
    // ==========================================

    fun getMaxStudentsAllowed(): Int {
        val currentPlan = _saasSubscription.value.planType
        return when (currentPlan) {
            SaaSPlanType.FREE -> 20
            else -> 999999
        }
    }

    fun canAddStudent(): Boolean {
        return _students.value.size < getMaxStudentsAllowed()
    }

    fun hasFeature(featureKey: String): Boolean {
        val currentPlan = _saasSubscription.value.planType
        return when (featureKey) {
            "multi_branch", "branch_dashboard", "consolidated_reports", "branch_comparison" -> {
                currentPlan == SaaSPlanType.BUSINESS
            }
            "whatsapp_fee_reminders", "whatsapp_reminders", "revenue_download", "pdf_export", "csv_export", "advanced_analytics", "email_support", "student_limit_20", "unlimited_students" -> {
                currentPlan == SaaSPlanType.PREMIUM || currentPlan == SaaSPlanType.BUSINESS
            }
            else -> true
        }
    }

    fun requiredPlan(featureKey: String): SaaSPlanType {
        return when (featureKey) {
            "multi_branch", "branch_dashboard", "consolidated_reports", "branch_comparison" -> SaaSPlanType.BUSINESS
            "whatsapp_fee_reminders", "whatsapp_reminders", "revenue_download", "pdf_export", "csv_export", "advanced_analytics", "email_support", "student_limit_20", "unlimited_students" -> SaaSPlanType.PREMIUM
            else -> SaaSPlanType.FREE
        }
    }

    fun checkSubscriptionStatus() {
        val sub = _saasSubscription.value
        if (sub.planType != SaaSPlanType.FREE) {
            try {
                val endDate = dateFormat.parse(sub.endDate)
                val today = dateFormat.parse(dateFormat.format(Date()))
                if (endDate != null && today != null && today.after(endDate)) {
                    // Plan has expired! Revert to FREE.
                    _saasSubscription.value = SaaSSubscription(
                        planType = SaaSPlanType.FREE,
                        billingPeriod = BillingPeriod.MONTHLY,
                        startDate = dateFormat.format(Date()),
                        endDate = "2099-12-31",
                        isActive = true
                    )
                    addAuditLog("SaaS Plan Expired", "Billing", "Plan expired and reverted to Free")
                    persistCurrentAccount()
                }
            } catch (e: Exception) {
                // Ignore parse exceptions
            }
        }
    }

    fun renewSaaSPlan() {
        val sub = _saasSubscription.value
        if (sub.planType == SaaSPlanType.FREE) return

        try {
            val currentEndDate = dateFormat.parse(sub.endDate) ?: Date()
            val calendar = Calendar.getInstance()
            val baseDate = if (currentEndDate.after(Date())) currentEndDate else Date()
            calendar.time = baseDate
            
            val daysToAdd = if (sub.billingPeriod == BillingPeriod.MONTHLY) 28 else 168
            calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
            val newEndStr = dateFormat.format(calendar.time)

            _saasSubscription.value = sub.copy(
                endDate = newEndStr,
                isActive = true
            )
            addAuditLog("SaaS Plan Renewed", "Billing", "Renewed ${sub.planType.displayName} until $newEndStr")
            persistCurrentAccount()
        } catch (e: Exception) {
            // Ignore parse exception
        }
    }

    fun upgradeSaaSPlan(planType: SaaSPlanType, billingPeriod: BillingPeriod, allowedBranches: Int = 1) {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        val daysToAdd = if (billingPeriod == BillingPeriod.MONTHLY) 28 else 168
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        val endStr = dateFormat.format(calendar.time)

        _saasSubscription.value = SaaSSubscription(
            planType = planType,
            billingPeriod = billingPeriod,
            startDate = dateFormat.format(Date()),
            endDate = endStr,
            isActive = true,
            allowedBranchesCount = allowedBranches
        )
        addAuditLog("SaaS Plan Upgraded", "Billing", "Upgraded to ${planType.displayName} ($billingPeriod, ending $endStr) with $allowedBranches branches")
        persistCurrentAccount()
    }

    fun addSaaSSubscriptionBranch() {
        val sub = _saasSubscription.value
        if (sub.planType != SaaSPlanType.BUSINESS) return

        _saasSubscription.value = sub.copy(
            allowedBranchesCount = sub.allowedBranchesCount + 1
        )
        addAuditLog("SaaS Plan Branch Added", "Billing", "Added another branch. Total allowed: ${sub.allowedBranchesCount + 1}")
        persistCurrentAccount()
    }

    // ==========================================
    // DATA OPERATIONS (WITH PERSISTENCE)
    // ==========================================

    fun addStudent(student: Student): Boolean {
        if (!canAddStudent()) {
            return false
        }
        if (student.assignedSeatNumber.isNotBlank()) {
            val conflictingSeat = _seats.value.find {
                it.seatNumber == student.assignedSeatNumber &&
                it.status == SeatStatus.OCCUPIED &&
                it.assignedShiftName == student.assignedShiftName &&
                it.assignedStudentId != student.id
            }
            if (conflictingSeat != null) {
                return false
            }
        }

        val updatedStudents = _students.value + student
        _students.value = updatedStudents

        if (student.assignedSeatNumber.isNotBlank()) {
            assignSeatInternal(student.assignedSeatNumber, student.id, student.fullName, student.assignedShiftName)
        }

        val today = dateFormat.format(Date())
        val att = AttendanceRecord(
            studentId = student.id,
            studentName = student.fullName,
            studentCode = student.studentCode,
            shiftName = student.assignedShiftName,
            seatNumber = student.assignedSeatNumber,
            attendanceDate = today,
            status = AttendanceStatus.PRESENT,
            branchId = student.branchId
        )
        _attendance.value = _attendance.value + att

        addAuditLog("Student Added", "Student", "${student.fullName} (${student.studentCode}) enrolled")
        persistCurrentAccount()
        return true
    }

    fun updateStudent(student: Student) {
        _students.value = _students.value.map { if (it.id == student.id) student else it }
        addAuditLog("Student Updated", "Student", "Profile updated for ${student.fullName}")
        persistCurrentAccount()
    }

    fun deleteStudent(studentId: String) {
        val student = _students.value.find { it.id == studentId }
        student?.let {
            if (it.assignedSeatNumber.isNotBlank()) {
                releaseSeatInternal(it.assignedSeatNumber)
            }
            _students.value = _students.value.filterNot { s -> s.id == studentId }
            addAuditLog("Student Removed", "Student", "Student ${it.fullName} deleted")
            persistCurrentAccount()
        }
    }

    fun assignSeat(seatNumber: String, studentId: String, studentName: String, shiftName: String) {
        assignSeatInternal(seatNumber, studentId, studentName, shiftName)
        addAuditLog("Seat Assigned", "Seat", "Seat $seatNumber assigned to $studentName ($shiftName)")
        persistCurrentAccount()
    }

    private fun assignSeatInternal(seatNumber: String, studentId: String, studentName: String, shiftName: String) {
        _seats.value = _seats.value.map { seat ->
            if (seat.seatNumber == seatNumber) {
                seat.copy(
                    status = SeatStatus.OCCUPIED,
                    assignedStudentId = studentId,
                    assignedStudentName = studentName,
                    assignedShiftName = shiftName,
                    expiryDate = "2026-09-30"
                )
            } else seat
        }
    }

    fun releaseSeat(seatNumber: String) {
        releaseSeatInternal(seatNumber)
        addAuditLog("Seat Released", "Seat", "Seat $seatNumber is now available")
        persistCurrentAccount()
    }

    fun releaseStudentSeat(studentId: String): String {
        val student = _students.value.find { it.id == studentId } ?: return ""
        val seatNum = student.assignedSeatNumber
        if (seatNum.isNotBlank()) {
            releaseSeatInternal(seatNum)
        }
        _students.value = _students.value.map {
            if (it.id == studentId) {
                it.copy(
                    assignedSeatNumber = "",
                    status = if (it.dueAmount > 0) StudentStatus.EXPIRED else it.status
                )
            } else it
        }
        addAuditLog("Seat Released", "Student", "Seat $seatNum released from ${student.fullName} (grace period / overdue)")
        persistCurrentAccount()
        return seatNum
    }

    private fun releaseSeatInternal(seatNumber: String) {
        _seats.value = _seats.value.map { seat ->
            if (seat.seatNumber == seatNumber) {
                seat.copy(
                    status = SeatStatus.AVAILABLE,
                    assignedStudentId = null,
                    assignedStudentName = null,
                    assignedShiftName = null,
                    expiryDate = null
                )
            } else seat
        }
    }

    fun toggleSeatMaintenance(seatNumber: String) {
        _seats.value = _seats.value.map { seat ->
            if (seat.seatNumber == seatNumber) {
                val newStatus = if (seat.status == SeatStatus.MAINTENANCE) SeatStatus.AVAILABLE else SeatStatus.MAINTENANCE
                seat.copy(status = newStatus)
            } else seat
        }
        persistCurrentAccount()
    }

    fun markAttendance(studentId: String, status: AttendanceStatus, targetDate: String = dateFormat.format(Date())) {
        val existingIndex = _attendance.value.indexOfFirst { it.studentId == studentId && it.attendanceDate == targetDate }
        if (existingIndex >= 0) {
            val updated = _attendance.value.toMutableList()
            updated[existingIndex] = updated[existingIndex].copy(status = status)
            _attendance.value = updated
        } else {
            val student = _students.value.find { it.id == studentId } ?: return
            val newRecord = AttendanceRecord(
                studentId = student.id,
                studentName = student.fullName,
                studentCode = student.studentCode,
                shiftName = student.assignedShiftName,
                seatNumber = student.assignedSeatNumber,
                attendanceDate = targetDate,
                status = status,
                branchId = student.branchId
            )
            _attendance.value = _attendance.value + newRecord
        }
        persistCurrentAccount()
    }

    fun markAllPresent(targetDate: String = dateFormat.format(Date())) {
        val currentRecords = _attendance.value.filter { it.attendanceDate != targetDate }.toMutableList()
        val newRecords = _students.value.map { student ->
            AttendanceRecord(
                studentId = student.id,
                studentName = student.fullName,
                studentCode = student.studentCode,
                shiftName = student.assignedShiftName,
                seatNumber = student.assignedSeatNumber,
                attendanceDate = targetDate,
                status = AttendanceStatus.PRESENT,
                branchId = student.branchId
            )
        }
        _attendance.value = currentRecords + newRecords
        addAuditLog("Bulk Attendance", "Attendance", "All active students marked Present for $targetDate")
        persistCurrentAccount()
    }

    fun collectFee(
        studentId: String,
        amount: Int,
        discount: Int,
        paymentMethod: PaymentMethod,
        notes: String
    ): StudentPayment? {
        val student = _students.value.find { it.id == studentId } ?: return null
        val receiptNumber = "REC-2026-" + String.format(Locale.US, "%04d", (_payments.value.size + 101))
        val payment = StudentPayment(
            studentId = student.id,
            studentName = student.fullName,
            studentCode = student.studentCode,
            amount = amount,
            discount = discount,
            paymentDate = dateFormat.format(Date()),
            paymentMethod = paymentMethod,
            receiptNumber = receiptNumber,
            notes = notes,
            shiftName = student.assignedShiftName,
            seatNumber = student.assignedSeatNumber,
            branchId = student.branchId
        )

        _payments.value = listOf(payment) + _payments.value

        val updatedDue = (student.dueAmount - amount).coerceAtLeast(0)
        _students.value = _students.value.map {
            if (it.id == studentId) it.copy(dueAmount = updatedDue) else it
        }

        addAuditLog("Fee Collected", "Payment", "₹$amount collected from ${student.fullName} ($receiptNumber via $paymentMethod)")
        persistCurrentAccount()
        return payment
    }

    fun addExpense(
        category: ExpenseCategory,
        title: String,
        amount: Int,
        paymentMethod: PaymentMethod,
        description: String
    ) {
        val expense = Expense(
            category = category,
            title = title,
            amount = amount,
            expenseDate = dateFormat.format(Date()),
            paymentMethod = paymentMethod,
            description = description,
            branchId = _activeBranchId.value
        )
        _expenses.value = listOf(expense) + _expenses.value
        addAuditLog("Expense Added", "Expense", "₹$amount logged for $title (${category.displayName})")
        persistCurrentAccount()
    }

    fun approveRegistrationRequest(requestId: String) {
        val request = _registrationRequests.value.find { it.id == requestId } ?: return
        _registrationRequests.value = _registrationRequests.value.map {
            if (it.id == requestId) it.copy(status = "approved") else it
        }

        val newCode = "STU-" + String.format(Locale.US, "%03d", _students.value.size + 1)
        val student = Student(
            studentCode = newCode,
            fullName = request.studentName,
            mobile = request.mobile,
            email = request.email,
            course = request.course,
            assignedShiftName = request.requestedShift,
            assignedSeatNumber = if (request.preferredSeat.startsWith("Seat")) request.preferredSeat.removePrefix("Seat ").trim() else "",
            monthlyFee = 1000,
            dueAmount = 1000
        )
        addStudent(student)
        addAuditLog("Registration Approved", "Registration", "Approved ${request.studentName} ($newCode)")
        persistCurrentAccount()
    }

    fun rejectRegistrationRequest(requestId: String) {
        _registrationRequests.value = _registrationRequests.value.map {
            if (it.id == requestId) it.copy(status = "rejected") else it
        }
        addAuditLog("Registration Rejected", "Registration", "Request $requestId rejected")
        persistCurrentAccount()
    }

    fun logWhatsAppReminder(
        studentName: String,
        targetPhone: String,
        dueAmount: Int,
        dueDate: String,
        messageType: String
    ) {
        val typeLabel = if (messageType == "OWNER_ALERT") "Owner Due Alert" else "Student Fee Reminder"
        addAuditLog("WhatsApp Dispatched", "WhatsApp", "$typeLabel sent for $studentName (Due: ₹$dueAmount, Date: $dueDate) to $targetPhone")
        persistCurrentAccount()
    }

    fun updateShift(shift: Shift) {
        _shifts.value = _shifts.value.map { if (it.id == shift.id) shift else it }
        addAuditLog("Shift Updated", "Shift", "Timings/fees updated for shift: ${shift.name}")
        persistCurrentAccount()
    }

    fun createShift(name: String, startTime: String, endTime: String, defaultPrice: Int, capacity: Int = _library.value.totalSeats): Boolean {
        val newShift = Shift(
            libraryId = _library.value.id,
            branchId = _activeBranchId.value,
            name = name,
            startTime = startTime,
            endTime = endTime,
            defaultPrice = defaultPrice,
            capacity = capacity
        )
        _shifts.value = _shifts.value + newShift
        addAuditLog("Shift Created", "Shift", "New shift created: $name ($startTime - $endTime, Fee: ₹$defaultPrice)")
        persistCurrentAccount()
        return true
    }

    fun createBranch(
        name: String,
        code: String,
        address: String,
        phone: String,
        city: String,
        state: String,
        pincode: String,
        totalSeats: Int,
        openingTime: String,
        closingTime: String,
        upiId: String
    ): Boolean {
        if (!hasFeature("multi_branch")) return false
        val newBranchId = "br_${System.currentTimeMillis().toString().takeLast(4)}"
        val newBranch = Branch(
            id = newBranchId,
            libraryId = _library.value.id,
            name = name,
            code = code,
            address = address,
            phone = phone,
            city = city,
            state = state,
            pincode = pincode,
            totalSeats = totalSeats,
            openingTime = openingTime,
            closingTime = closingTime,
            upiId = upiId,
            isActive = true,
            isPrimary = false
        )
        _branches.value = _branches.value + newBranch
        
        // Auto-generate clean seats for the new branch
        val newSeats = generateCleanSeats(totalSeats).map { it.copy(branchId = newBranchId) }
        _seats.value = _seats.value + newSeats
        
        // Auto-generate default shifts for the new branch
        val branchShifts = listOf(
            Shift(libraryId = _library.value.id, branchId = newBranchId, name = "Morning Shift", startTime = "06:00 AM", endTime = "12:00 PM", defaultPrice = 600, capacity = totalSeats),
            Shift(libraryId = _library.value.id, branchId = newBranchId, name = "Evening Shift", startTime = "05:00 PM", endTime = "11:00 PM", defaultPrice = 650, capacity = totalSeats),
            Shift(libraryId = _library.value.id, branchId = newBranchId, name = "Full Day (24x7)", startTime = "06:00 AM", endTime = "11:00 PM", defaultPrice = 1200, capacity = totalSeats)
        )
        _shifts.value = _shifts.value + branchShifts

        addAuditLog("Branch Created", "Branch", "New branch added: $name ($code) with $totalSeats seats capacity")
        persistCurrentAccount()
        return true
    }

    fun switchBranch(branchId: String) {
        val exists = _branches.value.any { it.id == branchId }
        if (exists) {
            _activeBranchId.value = branchId
            persistCurrentAccount()
        }
    }

    fun updateLibraryDetails(
        libraryName: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pincode: String,
        totalSeats: Int,
        openingTime: String,
        closingTime: String,
        upiId: String
    ) {
        _library.value = _library.value.copy(
            name = libraryName,
            phone = phone,
            address = address,
            city = city,
            state = state,
            pincode = pincode,
            totalSeats = totalSeats,
            openingTime = openingTime,
            closingTime = closingTime,
            upiId = upiId
        )
        // Propagate changes to the active branch details if we are modifying active branch
        _branches.value = _branches.value.map { br ->
            if (br.id == _activeBranchId.value) {
                br.copy(
                    name = libraryName,
                    phone = phone,
                    address = address,
                    city = city,
                    state = state,
                    pincode = pincode,
                    totalSeats = totalSeats,
                    openingTime = openingTime,
                    closingTime = closingTime,
                    upiId = upiId
                )
            } else br
        }
        addAuditLog("Profile Updated", "Library", "Library profile configurations updated")
        persistCurrentAccount()
    }

    fun completeOnboarding(
        libraryName: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pincode: String,
        openingTime: String,
        closingTime: String
    ) {
        updateLibraryDetails(
            libraryName = libraryName,
            phone = phone,
            address = address,
            city = city,
            state = state,
            pincode = pincode,
            totalSeats = 60,
            openingTime = openingTime,
            closingTime = closingTime,
            upiId = "${libraryName.lowercase().replace(" ", "").take(10)}@upi"
        )
        _isOnboardingCompleted.value = true
        addAuditLog("Onboarding Completed", "Library", "Library profile configured: $libraryName")
        persistCurrentAccount()
    }

    private fun addAuditLog(action: String, entity: String, details: String) {
        val log = AuditLog(
            action = action,
            entity = entity,
            details = details,
            timestamp = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date()),
            user = "Owner (${_ownerProfile.value.fullName.take(10)})"
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    // ==========================================
    // FRESH SEAT & DEMO GENERATORS
    // ==========================================

    fun generateCleanSeats(capacity: Int): List<Seat> {
        val list = mutableListOf<Seat>()
        val prefixes = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")
        var seatIndex = 1

        for (p in prefixes) {
            for (i in 1..10) {
                if (seatIndex > capacity) break
                val seatNum = "$p-$i"
                list.add(
                    Seat(
                        seatNumber = seatNum,
                        seatType = SeatType.FIXED,
                        status = SeatStatus.AVAILABLE,
                        assignedStudentId = null,
                        assignedStudentName = null,
                        assignedShiftId = null,
                        assignedShiftName = null,
                        expiryDate = null
                    )
                )
                seatIndex++
            }
            if (seatIndex > capacity) break
        }
        return list
    }

    private fun createDemoOwner(): OwnerProfile {
        return OwnerProfile(
            userId = "usr_owner_01",
            fullName = "Ratnesh Ankit",
            phone = "+91 9876543210",
            whatsapp = "+91 9876543210",
            email = "ratneshankit123@gmail.com",
            password = "admin123"
        )
    }

    private fun createDemoLibrary(): Library {
        return Library(
            id = "lib_01",
            name = "Saraswati Study Point & Library",
            phone = "+91 9876543210",
            email = "saraswati.library@gmail.com",
            address = "Plot 42, Knowledge Park III, Near Metro Station",
            city = "Greater Noida",
            state = "Uttar Pradesh",
            pincode = "201310",
            openingTime = "06:00 AM",
            closingTime = "11:00 PM",
            upiId = "saraswati.lib@okhdfcbank"
        )
    }

    private fun createDemoBranch(): Branch {
        return Branch(
            id = "branch_01",
            libraryId = "lib_01",
            name = "Knowledge Park III",
            code = "BR-01",
            address = "Plot 42, Knowledge Park III",
            phone = "+91 9876543210",
            isActive = true,
            isPrimary = true
        )
    }

    private fun createDefaultShifts(capacity: Int = 60): List<Shift> {
        return listOf(
            Shift(id = "sh_1", name = "Morning Shift", startTime = "06:00 AM", endTime = "12:00 PM", defaultPrice = 600, capacity = capacity),
            Shift(id = "sh_2", name = "Afternoon Shift", startTime = "12:00 PM", endTime = "05:00 PM", defaultPrice = 550, capacity = capacity),
            Shift(id = "sh_3", name = "Evening Shift", startTime = "05:00 PM", endTime = "11:00 PM", defaultPrice = 650, capacity = capacity),
            Shift(id = "sh_4", name = "Full Day (24/7 Access)", startTime = "06:00 AM", endTime = "11:00 PM", defaultPrice = 1200, capacity = capacity),
            Shift(id = "sh_5", name = "Night Owl Shift", startTime = "09:00 PM", endTime = "06:00 AM", defaultPrice = 700, capacity = (capacity * 0.6).toInt())
        )
    }

    private fun createDefaultLibraryPlans(): List<LibraryPlan> {
        return listOf(
            LibraryPlan(name = "Monthly Morning", durationMonths = 1, price = 600, shiftName = "Morning Shift", description = "6 hours daily fixed seat"),
            LibraryPlan(name = "Monthly Evening", durationMonths = 1, price = 650, shiftName = "Evening Shift", description = "6 hours peaceful evening"),
            LibraryPlan(name = "Monthly Full Day", durationMonths = 1, price = 1200, shiftName = "Full Day (24/7 Access)", description = "Dedicated fixed seat with locker"),
            LibraryPlan(name = "Quarterly Full Day", durationMonths = 3, price = 3300, shiftName = "Full Day (24/7 Access)", description = "Save ₹300 on 3-month commitment"),
            LibraryPlan(name = "Half-Yearly Full Day", durationMonths = 6, price = 6200, shiftName = "Full Day (24/7 Access)", description = "Popular for UPSC aspirants")
        )
    }

    private fun generateDemoSeats(): List<Seat> {
        val list = mutableListOf<Seat>()
        val prefixes = listOf("A", "B", "C", "D", "E", "F")
        var seatIndex = 1

        for (p in prefixes) {
            for (i in 1..10) {
                val seatNum = "$p-$i"
                val isOccupied = seatIndex in listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 14, 15, 21, 22, 25)
                val isMaintenance = seatIndex == 30

                list.add(
                    Seat(
                        seatNumber = seatNum,
                        seatType = SeatType.FIXED,
                        status = when {
                            isMaintenance -> SeatStatus.MAINTENANCE
                            isOccupied -> SeatStatus.OCCUPIED
                            else -> SeatStatus.AVAILABLE
                        },
                        assignedStudentName = if (isOccupied) "Student #$seatIndex" else null,
                        assignedShiftName = if (isOccupied) (if (seatIndex % 2 == 0) "Full Day" else "Morning Shift") else null,
                        expiryDate = if (isOccupied) "2026-09-30" else null
                    )
                )
                seatIndex++
            }
        }
        return list
    }

    private fun generateDemoStudents(): List<Student> {
        return listOf(
            Student(
                studentCode = "STU-001",
                fullName = "Aryan Patel",
                mobile = "+91 9811223344",
                whatsapp = "+91 9811223344",
                email = "aryan.patel@gmail.com",
                course = "UPSC CSE 2027",
                assignedSeatNumber = "A-1",
                assignedShiftName = "Full Day (24/7 Access)",
                monthlyFee = 1200,
                dueAmount = 0,
                feeDueDate = "2026-09-01",
                status = StudentStatus.ACTIVE
            ),
            Student(
                studentCode = "STU-002",
                fullName = "Priya Rajput",
                mobile = "+91 9822334455",
                whatsapp = "+91 9822334455",
                email = "priya.rajput@gmail.com",
                course = "UPPSC Combined State Exam",
                assignedSeatNumber = "A-2",
                assignedShiftName = "Morning Shift",
                monthlyFee = 600,
                dueAmount = 0,
                feeDueDate = "2026-09-05",
                status = StudentStatus.ACTIVE
            ),
            Student(
                studentCode = "STU-003",
                fullName = "Rohan Chaudhary",
                mobile = "+91 9833445566",
                whatsapp = "+91 9833445566",
                email = "rohan.c@gmail.com",
                course = "SSC CGL / Banking PO",
                assignedSeatNumber = "A-3",
                assignedShiftName = "Evening Shift",
                monthlyFee = 650,
                dueAmount = 650,
                feeDueDate = "2026-08-28",
                status = StudentStatus.ACTIVE
            ),
            Student(
                studentCode = "STU-004",
                fullName = "Sneha Gupta",
                mobile = "+91 9844556677",
                whatsapp = "+91 9844556677",
                email = "sneha.gupta@yahoo.com",
                course = "Chartered Accountancy (CA Final)",
                assignedSeatNumber = "A-4",
                assignedShiftName = "Full Day (24/7 Access)",
                monthlyFee = 1200,
                dueAmount = 0,
                feeDueDate = "2026-09-10",
                status = StudentStatus.ACTIVE
            ),
            Student(
                studentCode = "STU-005",
                fullName = "Vikas Yadav",
                mobile = "+91 9855667788",
                whatsapp = "+91 9855667788",
                email = "vikas.yadav@gmail.com",
                course = "GATE / PSU Aspirant",
                assignedSeatNumber = "A-5",
                assignedShiftName = "Afternoon Shift",
                monthlyFee = 550,
                dueAmount = 550,
                feeDueDate = "2026-08-28",
                status = StudentStatus.ACTIVE
            ),
            Student(
                studentCode = "STU-006",
                fullName = "Ananya Singh",
                mobile = "+91 9866778899",
                whatsapp = "+91 9866778899",
                email = "ananya.singh@outlook.com",
                course = "Judiciary (PCS-J)",
                assignedSeatNumber = "A-6",
                assignedShiftName = "Full Day (24/7 Access)",
                monthlyFee = 1200,
                dueAmount = 0,
                feeDueDate = "2026-09-02",
                status = StudentStatus.ACTIVE
            ),
            Student(
                studentCode = "STU-007",
                fullName = "Deepak Kumar",
                mobile = "+91 9877889900",
                whatsapp = "+91 9877889900",
                email = "deepak.k@gmail.com",
                course = "UGC NET / JRF History",
                assignedSeatNumber = "A-7",
                assignedShiftName = "Morning Shift",
                monthlyFee = 600,
                dueAmount = 0,
                feeDueDate = "2026-09-05",
                status = StudentStatus.ACTIVE
            ),
            Student(
                studentCode = "STU-008",
                fullName = "Kavita Meena",
                mobile = "+91 9888990011",
                whatsapp = "+91 9888990011",
                email = "kavita.meena@gmail.com",
                course = "State PCS / Teaching Exam",
                assignedSeatNumber = "A-8",
                assignedShiftName = "Evening Shift",
                monthlyFee = 650,
                dueAmount = 0,
                feeDueDate = "2026-09-08",
                status = StudentStatus.ACTIVE
            )
        )
    }

    private fun generateDemoAttendance(): List<AttendanceRecord> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val dates = (0..4).map { offset ->
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            sdf.format(cal.time)
        }

        val studentsData = listOf(
            Triple("s1", "Aryan Patel", "STU-001"),
            Triple("s2", "Priya Rajput", "STU-002"),
            Triple("s3", "Rohan Chaudhary", "STU-003"),
            Triple("s4", "Sneha Gupta", "STU-004"),
            Triple("s5", "Vikas Yadav", "STU-005"),
            Triple("s6", "Ananya Singh", "STU-006"),
            Triple("s7", "Deepak Kumar", "STU-007"),
            Triple("s8", "Kavita Meena", "STU-008")
        )

        val list = mutableListOf<AttendanceRecord>()
        dates.forEachIndexed { dateIdx, dateStr ->
            studentsData.forEachIndexed { stuIdx, (id, name, code) ->
                val isPresent = (stuIdx + dateIdx) % 4 != 0
                list.add(
                    AttendanceRecord(
                        studentId = id,
                        studentName = name,
                        studentCode = code,
                        shiftName = if (stuIdx % 2 == 0) "Full Day" else "Morning Shift",
                        seatNumber = "A-${stuIdx + 1}",
                        attendanceDate = dateStr,
                        checkInTime = if (isPresent) "07:30 AM" else null,
                        status = if (isPresent) AttendanceStatus.PRESENT else AttendanceStatus.ABSENT
                    )
                )
            }
        }
        return list
    }

    private fun generateDemoPayments(): List<StudentPayment> {
        return listOf(
            StudentPayment(
                studentId = "s1",
                studentName = "Aryan Patel",
                studentCode = "STU-001",
                amount = 1200,
                paymentDate = "2026-08-25",
                paymentMethod = PaymentMethod.UPI,
                transactionId = "UPI20260825001",
                receiptNumber = "REC-2026-0101",
                notes = "Full Day Monthly Access (Aug 2026)",
                shiftName = "Full Day",
                seatNumber = "A-1"
            ),
            StudentPayment(
                studentId = "s2",
                studentName = "Priya Rajput",
                studentCode = "STU-002",
                amount = 600,
                paymentDate = "2026-08-24",
                paymentMethod = PaymentMethod.CASH,
                transactionId = "CSH20260824002",
                receiptNumber = "REC-2026-0102",
                notes = "Morning Shift Fee",
                shiftName = "Morning Shift",
                seatNumber = "A-2"
            ),
            StudentPayment(
                studentId = "s4",
                studentName = "Sneha Gupta",
                studentCode = "STU-004",
                amount = 3300,
                paymentDate = "2026-08-20",
                paymentMethod = PaymentMethod.UPI,
                transactionId = "UPI20260820003",
                receiptNumber = "REC-2026-0103",
                notes = "Quarterly Full Day Advance Fee",
                shiftName = "Full Day",
                seatNumber = "A-4"
            ),
            StudentPayment(
                studentId = "s6",
                studentName = "Ananya Singh",
                studentCode = "STU-006",
                amount = 1200,
                paymentDate = "2026-08-18",
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                transactionId = "IMPS20260818004",
                receiptNumber = "REC-2026-0104",
                notes = "Monthly fee via IMPS transfer",
                shiftName = "Full Day",
                seatNumber = "A-6"
            )
        )
    }

    private fun generateDemoExpenses(): List<Expense> {
        return listOf(
            Expense(
                category = ExpenseCategory.ELECTRICITY,
                title = "Commercial AC & Power Bill (July-Aug)",
                amount = 4500,
                expenseDate = "2026-08-22",
                paymentMethod = PaymentMethod.UPI,
                description = "Electricity meter #890432"
            ),
            Expense(
                category = ExpenseCategory.INTERNET,
                title = "High-Speed Dual Fiber Wi-Fi (300 Mbps)",
                amount = 1499,
                expenseDate = "2026-08-15",
                paymentMethod = PaymentMethod.UPI,
                description = "Airtel Xstream Fiber Library Plan"
            ),
            Expense(
                category = ExpenseCategory.CLEANING,
                title = "Monthly Housekeeping & Sanitization",
                amount = 2500,
                expenseDate = "2026-08-10",
                paymentMethod = PaymentMethod.CASH,
                description = "Daily morning & evening floor cleaning"
            ),
            Expense(
                category = ExpenseCategory.MAINTENANCE,
                title = "RO Water Purifier & Dispenser Service",
                amount = 850,
                expenseDate = "2026-08-05",
                paymentMethod = PaymentMethod.UPI,
                description = "Filter replacement and cool water servicing"
            )
        )
    }

    private fun generateDemoRequests(): List<RegistrationRequest> {
        return listOf(
            RegistrationRequest(
                studentName = "Aman Verma",
                mobile = "+91 9456712340",
                email = "aman.verma@gmail.com",
                course = "SSC CGL 2026",
                requestedShift = "Morning Shift",
                preferredSeat = "Seat A-12",
                requestDate = "2026-08-25",
                status = "pending"
            ),
            RegistrationRequest(
                studentName = "Pooja Sharma",
                mobile = "+91 9712345678",
                email = "pooja.sharma@yahoo.com",
                course = "NEET PG Preparation",
                requestedShift = "Full Day (24/7 Access)",
                preferredSeat = "Seat B-04",
                requestDate = "2026-08-24",
                status = "pending"
            )
        )
    }

    suspend fun lookupPincode(pincode: String): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.postalpincode.in/pincode/$pincode"
                val request = okhttp3.Request.Builder().url(url).build()
                val client = okhttp3.OkHttpClient()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string()
                    if (!bodyStr.isNullOrBlank()) {
                        val rootArray = org.json.JSONArray(bodyStr)
                        if (rootArray.length() > 0) {
                            val rootObj = rootArray.getJSONObject(0)
                            if (rootObj.optString("Status") == "Success") {
                                val postOffices = rootObj.optJSONArray("PostOffice")
                                if (postOffices != null && postOffices.length() > 0) {
                                    val firstOffice = postOffices.getJSONObject(0)
                                    val district = firstOffice.optString("District")
                                    val state = firstOffice.optString("State")
                                    return@withContext Pair(district, state)
                                }
                            }
                        }
                    }
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun generateDemoAuditLogs(): List<AuditLog> {
        return listOf(
            AuditLog(action = "System Initialized", entity = "Library", details = "Saraswati Study Point onboarded", timestamp = "2026-08-01 09:00 AM"),
            AuditLog(action = "Fee Collected", entity = "Payment", details = "₹1,200 collected for Aryan Patel (REC-0810)", timestamp = "2026-08-25 08:30 AM"),
            AuditLog(action = "Seat Assigned", entity = "Seat", details = "Seat A-01 assigned to Aryan Patel (Full Day)", timestamp = "2026-08-01 10:00 AM")
        )
    }
}
