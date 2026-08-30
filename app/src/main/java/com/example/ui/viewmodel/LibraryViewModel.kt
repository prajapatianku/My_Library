package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.LibraryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DashboardMetrics(
    val activeStudentsCount: Int,
    val presentTodayCount: Int,
    val absentTodayCount: Int,
    val occupiedSeatsCount: Int,
    val totalSeatsCount: Int,
    val availableSeatsCount: Int,
    val todayCollection: Int,
    val monthlyCollection: Int,
    val totalExpenses: Int,
    val netRevenue: Int,
    val pendingDues: Int,
    val expiringCount: Int,
    val pendingRequestsCount: Int
)

class LibraryViewModel(
    private val repository: LibraryRepository = LibraryRepository()
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Base Repository State
    val ownerProfile = repository.ownerProfile
    val branches = repository.branches
    val activeBranchId = repository.activeBranchId
    val saasSubscription = repository.saasSubscription
    val registrationRequests = repository.registrationRequests
    val auditLogs = repository.auditLogs
    val isLoggedIn = repository.isLoggedIn
    val isOnboardingCompleted = repository.isOnboardingCompleted

    init {
        repository.checkSubscriptionStatus()
    }

    // Separated & Filtered Branch-Specific States
    val library = combine(repository.library, repository.activeBranchId, repository.branches) { mainLib, activeId, branchList ->
        val activeBranch = branchList.find { it.id == activeId }
        if (activeBranch != null) {
            Library(
                id = activeBranch.id,
                ownerId = mainLib.ownerId,
                name = activeBranch.name,
                phone = activeBranch.phone,
                email = mainLib.email,
                address = activeBranch.address,
                location = activeBranch.address,
                city = activeBranch.city,
                state = activeBranch.state,
                pincode = activeBranch.pincode,
                totalSeats = activeBranch.totalSeats,
                openingTime = activeBranch.openingTime,
                closingTime = activeBranch.closingTime,
                upiId = activeBranch.upiId,
                registrationToken = mainLib.registrationToken
            )
        } else {
            mainLib
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.library.value)

    val shifts = combine(repository.shifts, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val libraryPlans = combine(repository.libraryPlans, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seats = combine(repository.seats, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students = combine(repository.students, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance = combine(repository.attendance, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments = combine(repository.payments, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses = combine(repository.expenses, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lastGeneratedOtp = repository.lastGeneratedOtp

    // UI Search & Filter States
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery = _studentSearchQuery.asStateFlow()

    private val _studentShiftFilter = MutableStateFlow("All")
    val studentShiftFilter = _studentShiftFilter.asStateFlow()

    private val _studentStatusFilter = MutableStateFlow("All")
    val studentStatusFilter = _studentStatusFilter.asStateFlow()

    private val _seatFloorFilter = MutableStateFlow("All")
    val seatFloorFilter = _seatFloorFilter.asStateFlow()

    private val _attendanceShiftFilter = MutableStateFlow("All")
    val attendanceShiftFilter = _attendanceShiftFilter.asStateFlow()

    private val _selectedAttendanceDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val selectedAttendanceDate = _selectedAttendanceDate.asStateFlow()

    private val _reportType = MutableStateFlow("Revenue")
    val reportType = _reportType.asStateFlow()

    private val _financeSubTab = MutableStateFlow(0) // 0 = Fees, 1 = Expenses
    val financeSubTab = _financeSubTab.asStateFlow()

    // Dialog & Modal Triggers
    private val _selectedStudentForDetail = MutableStateFlow<Student?>(null)
    val selectedStudentForDetail = _selectedStudentForDetail.asStateFlow()

    private val _selectedSeatForAction = MutableStateFlow<Seat?>(null)
    val selectedSeatForAction = _selectedSeatForAction.asStateFlow()

    private val _activeReceipt = MutableStateFlow<StudentPayment?>(null)
    val activeReceipt = _activeReceipt.asStateFlow()

    private val _showUpgradeModal = MutableStateFlow(false)
    val showUpgradeModal = _showUpgradeModal.asStateFlow()

    private val _upgradeTargetFeature = MutableStateFlow<String?>(null)
    val upgradeTargetFeature = _upgradeTargetFeature.asStateFlow()

    private val _showAddStudentDialog = MutableStateFlow(false)
    val showAddStudentDialog = _showAddStudentDialog.asStateFlow()

    private val _showCollectFeeDialog = MutableStateFlow<Student?>(null)
    val showCollectFeeDialog = _showCollectFeeDialog.asStateFlow()

    private val _showAddExpenseDialog = MutableStateFlow(false)
    val showAddExpenseDialog = _showAddExpenseDialog.asStateFlow()

    private val _showQrDialog = MutableStateFlow(false)
    val showQrDialog = _showQrDialog.asStateFlow()

    private val _showBranchManagerDialog = MutableStateFlow(false)
    val showBranchManagerDialog = _showBranchManagerDialog.asStateFlow()

    private val _showWhatsAppReminderDialog = MutableStateFlow(false)
    val showWhatsAppReminderDialog = _showWhatsAppReminderDialog.asStateFlow()

    private val _selectedStudentForWhatsAppReminder = MutableStateFlow<Student?>(null)
    val selectedStudentForWhatsAppReminder = _selectedStudentForWhatsAppReminder.asStateFlow()

    private val _uiToastMessage = MutableStateFlow<String?>(null)
    val uiToastMessage = _uiToastMessage.asStateFlow()

    // Computed Dashboard KPI Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        combine(students, seats) { stu, seat -> Pair(stu, seat) },
        combine(attendance, payments) { att, pay -> Pair(att, pay) },
        combine(expenses, registrationRequests) { exp, req -> Pair(exp, req) }
    ) { (stuList, seatList), (attList, payList), (expList, reqList) ->
        val today = dateFormat.format(Date())
        val activeStu = stuList.count { it.status == StudentStatus.ACTIVE }
        val presentToday = attList.count { it.attendanceDate == today && it.status == AttendanceStatus.PRESENT }
        val absentToday = attList.count { it.attendanceDate == today && it.status == AttendanceStatus.ABSENT }
        val occupied = seatList.count { it.status == SeatStatus.OCCUPIED }
        val totalSeats = seatList.size
        val availableSeats = totalSeats - occupied

        val todayCol = payList.filter { it.paymentDate == today }.sumOf { it.amount }
        val monthCol = payList.sumOf { it.amount }
        val totalExp = expList.sumOf { it.amount }
        val netRev = monthCol - totalExp
        val pendingDues = stuList.sumOf { it.dueAmount }
        val expiring = stuList.count { it.dueAmount > 0 || it.status == StudentStatus.EXPIRED }
        val pendingReqs = reqList.count { it.status == "pending" }

        DashboardMetrics(
            activeStudentsCount = activeStu,
            presentTodayCount = presentToday,
            absentTodayCount = absentToday,
            occupiedSeatsCount = occupied,
            totalSeatsCount = totalSeats,
            availableSeatsCount = availableSeats,
            todayCollection = todayCol,
            monthlyCollection = monthCol,
            totalExpenses = totalExp,
            netRevenue = netRev,
            pendingDues = pendingDues,
            expiringCount = expiring,
            pendingRequestsCount = pendingReqs
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardMetrics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    )

    // Filtered Students List
    val filteredStudents: StateFlow<List<Student>> = combine(
        students, studentSearchQuery, studentShiftFilter, studentStatusFilter
    ) { list, query, shift, status ->
        list.filter { student ->
            val matchesQuery = query.isBlank() ||
                student.fullName.contains(query, ignoreCase = true) ||
                student.studentCode.contains(query, ignoreCase = true) ||
                student.mobile.contains(query) ||
                student.assignedSeatNumber.contains(query, ignoreCase = true)

            val matchesShift = shift == "All" || student.assignedShiftName.contains(shift, ignoreCase = true)
            val matchesStatus = when (status) {
                "Active" -> student.status == StudentStatus.ACTIVE
                "Has Due" -> student.dueAmount > 0
                "Expired" -> student.status == StudentStatus.EXPIRED
                else -> true
            }
            matchesQuery && matchesShift && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Setters & Actions
    fun setStudentSearchQuery(q: String) { _studentSearchQuery.value = q }
    fun setStudentShiftFilter(f: String) { _studentShiftFilter.value = f }
    fun setStudentStatusFilter(s: String) { _studentStatusFilter.value = s }
    fun setSeatFloorFilter(floor: String) { _seatFloorFilter.value = floor }
    fun setAttendanceShiftFilter(shift: String) { _attendanceShiftFilter.value = shift }
    fun setSelectedAttendanceDate(date: String) { _selectedAttendanceDate.value = date }

    fun goToPreviousAttendanceDay() {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(_selectedAttendanceDate.value) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            _selectedAttendanceDate.value = dateFormat.format(cal.time)
        } catch (e: Exception) {
            // keep current
        }
    }

    fun goToNextAttendanceDay() {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(_selectedAttendanceDate.value) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val nextDate = cal.time
            // Do not advance past today if desired, or allow viewing up to today
            _selectedAttendanceDate.value = dateFormat.format(nextDate)
        } catch (e: Exception) {
            // keep current
        }
    }

    fun isAttendanceDateEditable(dateStr: String): Boolean {
        return try {
            val target = dateFormat.parse(dateStr) ?: return false
            val today = dateFormat.parse(dateFormat.format(Date())) ?: return false
            val diffMillis = today.time - target.time
            val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
            // Editable for Today (0), Yesterday (1), Day before yesterday (2)
            diffDays in 0..2
        } catch (e: Exception) {
            false
        }
    }

    fun getAttendanceDaysAgo(dateStr: String): Int {
        return try {
            val target = dateFormat.parse(dateStr) ?: return 0
            val today = dateFormat.parse(dateFormat.format(Date())) ?: return 0
            val diffMillis = today.time - target.time
            (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun setReportType(type: String) { _reportType.value = type }
    fun setFinanceSubTab(tab: Int) { _financeSubTab.value = tab }

    fun selectStudentForDetail(student: Student?) { _selectedStudentForDetail.value = student }
    fun selectSeatForAction(seat: Seat?) { _selectedSeatForAction.value = seat }
    fun showAddStudentDialog(show: Boolean) { _showAddStudentDialog.value = show }
    fun showCollectFeeDialog(student: Student?) { _showCollectFeeDialog.value = student }
    fun showAddExpenseDialog(show: Boolean) { _showAddExpenseDialog.value = show }
    fun showQrDialog(show: Boolean) { _showQrDialog.value = show }
    fun showBranchManagerDialog(show: Boolean) {
        if (!hasFeature("multi_branch")) {
            requestUpgrade("multi_branch")
        } else {
            _showBranchManagerDialog.value = show
        }
    }

    fun showWhatsAppReminderDialog(show: Boolean) {
        if (!hasFeature("whatsapp_fee_reminders")) {
            requestUpgrade("whatsapp_fee_reminders")
        } else {
            _showWhatsAppReminderDialog.value = show
        }
    }

    fun selectStudentForWhatsAppReminder(student: Student?) {
        _selectedStudentForWhatsAppReminder.value = student
        if (student != null) {
            _showWhatsAppReminderDialog.value = true
        }
    }

    fun showActiveReceipt(payment: StudentPayment?) { _activeReceipt.value = payment }
    fun clearToast() { _uiToastMessage.value = null }
    fun showToast(message: String) { _uiToastMessage.value = message }

    fun requestUpgrade(featureKey: String) {
        _upgradeTargetFeature.value = featureKey
        _showUpgradeModal.value = true
    }

    fun dismissUpgradeModal() {
        _showUpgradeModal.value = false
        _upgradeTargetFeature.value = null
    }

    fun hasFeature(featureKey: String): Boolean = repository.hasFeature(featureKey)
    fun requiredPlan(featureKey: String): SaaSPlanType = repository.requiredPlan(featureKey)

    fun completeOnboarding(
        name: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pincode: String,
        openingTime: String,
        closingTime: String
    ) {
        repository.completeOnboarding(name, phone, address, city, state, pincode, openingTime, closingTime)
        _uiToastMessage.value = "Welcome to My Library! Your workspace is ready."
    }

    fun switchBranch(branchId: String) {
        repository.switchBranch(branchId)
        _uiToastMessage.value = "Switched branch"
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
    ) {
        val sub = saasSubscription.value
        val currentBranchCount = branches.value.size
        if (currentBranchCount >= sub.allowedBranchesCount) {
            requestUpgrade("add_branch")
            return
        }
        val success = repository.createBranch(
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
            upiId = upiId
        )
        if (success) {
            _uiToastMessage.value = "Branch $name created successfully!"
            _showBranchManagerDialog.value = false
        }
    }

    fun upgradeSaaS(planType: SaaSPlanType, period: BillingPeriod, allowedBranches: Int = 1) {
        repository.upgradeSaaSPlan(planType, period, allowedBranches)
        _showUpgradeModal.value = false
        _upgradeTargetFeature.value = null
        _uiToastMessage.value = "Upgraded to ${planType.displayName}! All features unlocked."
    }

    fun purchaseAdditionalBranch() {
        repository.addSaaSSubscriptionBranch()
        _showUpgradeModal.value = false
        _uiToastMessage.value = "Additional branch added to your subscription successfully!"
    }

    fun calculateProratedBranchPrice(): Int {
        val sub = saasSubscription.value
        if (sub.planType != SaaSPlanType.BUSINESS) return 0
        
        val daysRemaining = getSubscriptionDaysRemaining()
        if (daysRemaining <= 0) return 0
        
        val totalDays = if (sub.billingPeriod == BillingPeriod.MONTHLY) 28 else 168
        
        val baseAdditionalPrice = if (sub.billingPeriod == BillingPeriod.MONTHLY) {
            99
        } else {
            499
        }
        
        val proratedPrice = (baseAdditionalPrice * daysRemaining) / totalDays
        return proratedPrice.coerceAtLeast(1)
    }

    fun renewSaaS() {
        repository.renewSaaSPlan()
        _showUpgradeModal.value = false
        _uiToastMessage.value = "Subscription renewed successfully!"
    }

    fun getSubscriptionDaysRemaining(): Int {
        val sub = saasSubscription.value
        if (sub.planType == SaaSPlanType.FREE) return -1
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val endDate = sdf.parse(sub.endDate) ?: return -1
            val today = sdf.parse(sdf.format(Date())) ?: return -1
            val diff = endDate.time - today.time
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            -1
        }
    }

    fun updateLibraryDetails(
        name: String,
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
        repository.updateLibraryDetails(
            libraryName = name,
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
        _uiToastMessage.value = "Library profile updated successfully!"
    }

    fun createStudent(
        fullName: String,
        mobile: String,
        whatsapp: String,
        email: String,
        course: String,
        assignedSeat: String,
        assignedShift: String,
        monthlyFee: Int,
        initialDue: Int
    ): Boolean {
        val count = students.value.size + 1
        val code = "STU-" + String.format(Locale.US, "%03d", count)
        val student = Student(
            studentCode = code,
            fullName = fullName,
            mobile = mobile,
            whatsapp = if (whatsapp.isBlank()) mobile else whatsapp,
            email = email,
            course = course,
            assignedSeatNumber = assignedSeat,
            assignedShiftName = assignedShift,
            monthlyFee = monthlyFee,
            dueAmount = initialDue,
            branchId = repository.activeBranchId.value
        )
        val success = repository.addStudent(student)
        if (success) {
            _showAddStudentDialog.value = false
            _uiToastMessage.value = "Student $fullName ($code) added successfully!"
        } else {
            _uiToastMessage.value = "Seat $assignedSeat is already occupied in $assignedShift. Please choose another seat."
        }
        return success
    }

    fun deleteStudent(studentId: String) {
        repository.deleteStudent(studentId)
        _selectedStudentForDetail.value = null
        _uiToastMessage.value = "Student removed"
    }

    fun assignSeatToStudent(seatNumber: String, studentId: String, studentName: String, shiftName: String) {
        repository.assignSeat(seatNumber, studentId, studentName, shiftName)
        _selectedSeatForAction.value = null
        _uiToastMessage.value = "Seat $seatNumber assigned to $studentName"
    }

    fun releaseSeat(seatNumber: String) {
        repository.releaseSeat(seatNumber)
        _selectedSeatForAction.value = null
        _uiToastMessage.value = "Seat $seatNumber is now available"
    }

    fun toggleSeatMaintenance(seatNumber: String) {
        repository.toggleSeatMaintenance(seatNumber)
        _selectedSeatForAction.value = null
        _uiToastMessage.value = "Seat $seatNumber status updated"
    }

    fun markAttendance(studentId: String, status: AttendanceStatus, targetDate: String = _selectedAttendanceDate.value) {
        if (!isAttendanceDateEditable(targetDate)) {
            _uiToastMessage.value = "Attendance for $targetDate is locked. Historical records older than 2 days cannot be modified."
            return
        }
        repository.markAttendance(studentId, status, targetDate)
    }

    fun markAllPresent(targetDate: String = _selectedAttendanceDate.value) {
        if (!isAttendanceDateEditable(targetDate)) {
            _uiToastMessage.value = "Attendance for $targetDate is locked (older than 2 days)."
            return
        }
        repository.markAllPresent(targetDate)
        val daysAgo = getAttendanceDaysAgo(targetDate)
        val label = when (daysAgo) {
            0 -> "today"
            1 -> "yesterday"
            2 -> "day before yesterday"
            else -> targetDate
        }
        _uiToastMessage.value = "All active students marked Present for $label!"
    }

    fun collectFee(
        studentId: String,
        amount: Int,
        discount: Int,
        paymentMethod: PaymentMethod,
        notes: String
    ) {
        val payment = repository.collectFee(studentId, amount, discount, paymentMethod, notes)
        _showCollectFeeDialog.value = null
        if (payment != null) {
            _activeReceipt.value = payment
            _uiToastMessage.value = "₹$amount collected successfully! Receipt generated."
        }
    }

    fun addExpense(
        category: ExpenseCategory,
        title: String,
        amount: Int,
        paymentMethod: PaymentMethod,
        description: String
    ) {
        repository.addExpense(category, title, amount, paymentMethod, description)
        _showAddExpenseDialog.value = false
        _uiToastMessage.value = "₹$amount expense logged for $title"
    }

    fun approveRequest(requestId: String) {
        repository.approveRegistrationRequest(requestId)
        _uiToastMessage.value = "Registration request approved!"
    }

    fun rejectRequest(requestId: String) {
        repository.rejectRegistrationRequest(requestId)
        _uiToastMessage.value = "Registration request rejected"
    }

    fun updateShift(shift: Shift) {
        repository.updateShift(shift)
        _uiToastMessage.value = "Shift ${shift.name} updated successfully!"
    }

    fun createShift(name: String, startTime: String, endTime: String, defaultPrice: Int) {
        repository.createShift(name, startTime, endTime, defaultPrice)
        _uiToastMessage.value = "Custom shift $name created successfully!"
    }

    // Authentication actions
    fun login(emailOrPhone: String, name: String = "Ratnesh Ankit"): Boolean {
        val success = repository.login(emailOrPhone, name)
        if (success) {
            _uiToastMessage.value = "Welcome back, ${repository.ownerProfile.value.fullName}!"
        }
        return success
    }

    fun sendOtp(phone: String): String {
        val otp = repository.sendOtp(phone)
        _uiToastMessage.value = "OTP sent to $phone: $otp"
        return otp
    }

    fun verifyOtpAndLogin(phone: String, otp: String): Boolean {
        val success = repository.verifyOtpAndLogin(phone, otp)
        if (success) {
            _uiToastMessage.value = "OTP Verified! Welcome to your Library portal."
        } else {
            _uiToastMessage.value = "Invalid OTP code. Please enter the 4-digit OTP."
        }
        return success
    }

    fun loginWithPassword(phoneOrEmail: String, password: String): Boolean {
        val success = repository.loginWithPassword(phoneOrEmail, password)
        if (success) {
            _uiToastMessage.value = "Welcome back, ${repository.ownerProfile.value.fullName}!"
        } else {
            _uiToastMessage.value = "Account not found or password incorrect. Please check your credentials or register a new library."
        }
        return success
    }

    fun logout() {
        repository.logout()
        _uiToastMessage.value = "Logged out successfully"
    }

    fun registerOwner(fullName: String, phone: String, email: String, libraryName: String) {
        repository.registerOwner(fullName, phone, email, libraryName)
        _uiToastMessage.value = "Welcome, $fullName! Your library is ready."
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
        shifts: List<Shift>? = null
    ) {
        repository.registerFullLibrary(
            ownerName = ownerName,
            phone = phone,
            whatsapp = whatsapp,
            email = email,
            password = password,
            libraryName = libraryName,
            contactNumber = contactNumber,
            libraryEmail = libraryEmail,
            address = address,
            location = location,
            city = city,
            state = state,
            pincode = pincode,
            seatCapacity = seatCapacity,
            customShifts = shifts
        )
        _uiToastMessage.value = "🎉 Registration complete! Welcome $ownerName to $libraryName."
    }

    // ==========================================
    // WHATSAPP FEE DUE REMINDER SYSTEM
    // (Available on 2nd / Premium & 3rd / Business Plans)
    // ==========================================

    fun generateOwnerWhatsAppDueAlertText(dueStudents: List<Student>, lib: Library, own: OwnerProfile): String {
        val totalDue = dueStudents.sumOf { it.dueAmount }
        val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val sb = StringBuilder()
        sb.append("🔔 *LIBRARY FEE DUE NOTIFICATION*\n")
        sb.append("🏛️ *${lib.name}*\n")
        sb.append("📅 Date: $todayStr\n\n")
        sb.append("Hello *${own.fullName}*, the following students have fee due dates today / pending dues:\n\n")

        dueStudents.forEachIndexed { index, student ->
            sb.append("${index + 1}. *${student.fullName}* (${student.studentCode})\n")
            sb.append("   • 💰 Due: *₹${student.dueAmount}* (Monthly: ₹${student.monthlyFee})\n")
            sb.append("   • 📅 Due Date: ${student.feeDueDate}\n")
            sb.append("   • 🪑 Seat: ${student.assignedSeatNumber.ifBlank { "Floating" }} | ${student.assignedShiftName}\n")
            sb.append("   • 📞 Contact: ${student.mobile}\n\n")
        }

        sb.append("📊 *Total Pending Collection:* ₹$totalDue across ${dueStudents.size} student(s)\n")
        sb.append("💳 *Library UPI ID:* ${lib.upiId}\n\n")
        sb.append("💡 _Powered by My Library Management System_")
        return sb.toString()
    }

    fun generateStudentWhatsAppReminderText(student: Student, lib: Library): String {
        val sb = StringBuilder()
        sb.append("📢 *FEE PAYMENT REMINDER*\n")
        sb.append("🏛️ *${lib.name}*\n\n")
        sb.append("Dear *${student.fullName}*,\n\n")
        sb.append("This is a friendly reminder that your library subscription fee of *₹${student.dueAmount}* is due for payment on *${student.feeDueDate}*.\n\n")
        sb.append("📌 *Student Pass Details:*\n")
        sb.append("• Student ID: *${student.studentCode}*\n")
        sb.append("• Course: ${student.course}\n")
        sb.append("• Shift: ${student.assignedShiftName}\n")
        sb.append("• Assigned Seat: ${if (student.assignedSeatNumber.isNotBlank()) "Seat " + student.assignedSeatNumber else "Floating Area"}\n")
        sb.append("• Due Amount: *₹${student.dueAmount}*\n\n")
        sb.append("💳 *Payment Options:*\n")
        sb.append("• Pay via UPI: *${lib.upiId}*\n")
        sb.append("• Or visit the library front desk.\n\n")
        sb.append("Kindly share the transaction screenshot after payment to receive your digital receipt.\n\n")
        sb.append("Best regards,\n")
        sb.append("*${lib.name} Management*\n")
        sb.append("📞 ${lib.phone}")
        return sb.toString()
    }

    fun launchWhatsApp(context: android.content.Context, rawPhone: String, message: String) {
        try {
            var cleanPhone = rawPhone.replace("+", "").replace(" ", "").replace("-", "").trim()
            if (cleanPhone.length == 10) {
                cleanPhone = "91$cleanPhone"
            }
            val encodedMessage = java.net.URLEncoder.encode(message, "UTF-8")
            val url = if (cleanPhone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, message)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Send WhatsApp Reminder"))
            } catch (e2: Exception) {
                _uiToastMessage.value = "WhatsApp message prepared"
            }
        }
    }

    fun sendOwnerWhatsAppAlert(context: android.content.Context) {
        if (!hasFeature("whatsapp_fee_reminders")) {
            requestUpgrade("whatsapp_fee_reminders")
            return
        }
        val dueStudents = students.value.filter { it.dueAmount > 0 }
        if (dueStudents.isEmpty()) {
            _uiToastMessage.value = "No students have pending fee dues! 🎉"
            return
        }
        val own = ownerProfile.value
        val lib = library.value
        val msg = generateOwnerWhatsAppDueAlertText(dueStudents, lib, own)
        val targetPhone = own.whatsapp.ifBlank { own.phone }

        repository.logWhatsAppReminder(
            studentName = "${dueStudents.size} Due Students",
            targetPhone = targetPhone,
            dueAmount = dueStudents.sumOf { it.dueAmount },
            dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            messageType = "OWNER_ALERT"
        )

        launchWhatsApp(context, targetPhone, msg)
        _uiToastMessage.value = "Opening WhatsApp to notify owner (${own.fullName}) of ${dueStudents.size} fee dues!"
    }

    fun sendStudentWhatsAppReminder(context: android.content.Context, student: Student) {
        if (!hasFeature("whatsapp_fee_reminders")) {
            requestUpgrade("whatsapp_fee_reminders")
            return
        }
        val lib = library.value
        val msg = generateStudentWhatsAppReminderText(student, lib)
        val targetPhone = student.whatsapp.ifBlank { student.mobile }

        repository.logWhatsAppReminder(
            studentName = student.fullName,
            targetPhone = targetPhone,
            dueAmount = student.dueAmount,
            dueDate = student.feeDueDate,
            messageType = "STUDENT_REMINDER"
        )

        launchWhatsApp(context, targetPhone, msg)
        _uiToastMessage.value = "Opening WhatsApp fee reminder for ${student.fullName} (₹${student.dueAmount} due)"
    }

    fun lookupPincode(pincode: String, onResult: (city: String, state: String) -> Unit) {
        viewModelScope.launch {
            val result = repository.lookupPincode(pincode)
            if (result != null) {
                onResult(result.first, result.second)
            }
        }
    }
}
