package com.example.data.model

import java.util.UUID

enum class SaaSPlanType(val displayName: String, val monthlyPrice: Int, val sixMonthPrice: Int) {
    FREE("Vidyara Free", 0, 0),
    PREMIUM("Vidyara Pro", 99, 399),
    BUSINESS("Vidyara Business", 199, 999)
}

enum class BillingPeriod {
    MONTHLY,
    SIX_MONTH
}

enum class SeatType {
    FIXED,
    FLOATING
}

enum class SeatStatus {
    AVAILABLE,
    OCCUPIED,
    RESERVED,
    MAINTENANCE
}

enum class StudentStatus {
    ACTIVE,
    PENDING,
    EXPIRED,
    INACTIVE,
    SUSPENDED
}

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    LEAVE
}

enum class PaymentMethod {
    UPI,
    CASH,
    BANK_TRANSFER,
    CARD,
    OTHER
}

enum class ExpenseCategory(val displayName: String) {
    RENT("Rent"),
    ELECTRICITY("Electricity"),
    INTERNET("Internet & Wi-Fi"),
    SALARY("Staff Salary"),
    MAINTENANCE("Maintenance & Repairs"),
    CLEANING("Cleaning & Housekeeping"),
    FURNITURE("Furniture & Equipment"),
    MARKETING("Marketing & Ads"),
    SOFTWARE("Software & Tools"),
    OTHER("Other")
}

data class OwnerProfile(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val fullName: String = "Ratnesh Ankit",
    val phone: String = "+91 9876543210",
    val whatsapp: String = "+91 9876543210",
    val email: String = "ratneshankit123@gmail.com",
    val avatarUrl: String = "",
    val password: String = "admin123"
)

data class Library(
    val id: String = "lib_default_01",
    val ownerId: String = "owner_default",
    val name: String = "Saraswati Study Point & Library",
    val logoUrl: String = "",
    val phone: String = "+91 9876543210",
    val email: String = "contact@saraswatilibrary.com",
    val address: String = "Plot 42, Knowledge Park III, Near Metro Station",
    val location: String = "Knowledge Park III, Greater Noida",
    val city: String = "Greater Noida",
    val state: String = "Uttar Pradesh",
    val pincode: String = "201310",
    val totalSeats: Int = 60,
    val openingTime: String = "06:00 AM",
    val closingTime: String = "11:00 PM",
    val registrationToken: String = "reg_sp_2026_x89",
    val upiId: String = "saraswati.lib@okhdfcbank"
)

data class Branch(
    val id: String = "branch_01",
    val libraryId: String = "lib_default_01",
    val name: String = "Main Campus (Knowledge Park)",
    val code: String = "BR-01",
    val address: String = "Plot 42, Knowledge Park III",
    val phone: String = "+91 9876543210",
    val city: String = "Greater Noida",
    val state: String = "Uttar Pradesh",
    val pincode: String = "201310",
    val totalSeats: Int = 60,
    val openingTime: String = "06:00 AM",
    val closingTime: String = "11:00 PM",
    val upiId: String = "saraswati.lib@okhdfcbank",
    val isActive: Boolean = true,
    val isPrimary: Boolean = true
)

data class Floor(
    val id: String = "floor_01",
    val branchId: String = "branch_01",
    val name: String = "Ground Floor (Quiet Zone)",
    val floorNumber: Int = 0
)

data class Room(
    val id: String = "room_01",
    val floorId: String = "floor_01",
    val name: String = "Main Study Hall"
)

data class Shift(
    val id: String = UUID.randomUUID().toString(),
    val libraryId: String = "lib_default_01",
    val branchId: String = "branch_01",
    val name: String,
    val startTime: String,
    val endTime: String,
    val defaultPrice: Int,
    val capacity: Int = 60,
    val isActive: Boolean = true
)

data class Seat(
    val id: String = UUID.randomUUID().toString(),
    val branchId: String = "branch_01",
    val floorId: String = "floor_01",
    val roomId: String = "room_01",
    val seatNumber: String,
    val seatType: SeatType = SeatType.FIXED,
    val status: SeatStatus = SeatStatus.AVAILABLE,
    val assignedStudentId: String? = null,
    val assignedStudentName: String? = null,
    val assignedShiftId: String? = null,
    val assignedShiftName: String? = null,
    val expiryDate: String? = null
)

data class Student(
    val id: String = UUID.randomUUID().toString(),
    val libraryId: String = "lib_default_01",
    val branchId: String = "branch_01",
    val studentCode: String,
    val fullName: String,
    val photoUrl: String = "",
    val mobile: String,
    val whatsapp: String = "",
    val email: String = "",
    val gender: String = "Male",
    val dateOfBirth: String = "2001-05-15",
    val address: String = "",
    val guardianName: String = "",
    val guardianPhone: String = "",
    val course: String = "UPSC Civil Services",
    val college: String = "",
    val joiningDate: String = "2026-08-01",
    val feeDueDate: String = "2026-08-28",
    val status: StudentStatus = StudentStatus.ACTIVE,
    val assignedSeatNumber: String = "",
    val assignedShiftName: String = "Full Day",
    val monthlyFee: Int = 1200,
    val dueAmount: Int = 0,
    val notes: String = ""
)

data class WhatsAppReminderLog(
    val id: String = UUID.randomUUID().toString(),
    val studentName: String,
    val targetPhone: String,
    val dueAmount: Int,
    val dueDate: String,
    val messageType: String = "OWNER_ALERT", // "OWNER_ALERT" or "STUDENT_REMINDER"
    val timestamp: String,
    val status: String = "Sent to WhatsApp"
)

data class LibraryPlan(
    val id: String = UUID.randomUUID().toString(),
    val libraryId: String = "lib_default_01",
    val branchId: String = "branch_01",
    val name: String,
    val durationMonths: Int = 1,
    val price: Int,
    val shiftName: String,
    val seatType: SeatType = SeatType.FIXED,
    val description: String = ""
)

data class StudentSubscription(
    val id: String = UUID.randomUUID().toString(),
    val libraryId: String = "lib_default_01",
    val branchId: String = "branch_01",
    val studentId: String,
    val studentName: String,
    val planName: String,
    val shiftName: String,
    val seatNumber: String,
    val startDate: String,
    val endDate: String,
    val amount: Int,
    val discount: Int = 0,
    val finalAmount: Int,
    val status: String = "active"
)

data class StudentPayment(
    val id: String = UUID.randomUUID().toString(),
    val libraryId: String = "lib_default_01",
    val branchId: String = "branch_01",
    val studentId: String,
    val studentName: String,
    val studentCode: String = "",
    val amount: Int,
    val discount: Int = 0,
    val paymentDate: String,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val transactionId: String = "TXN" + System.currentTimeMillis().toString().takeLast(6),
    val receiptNumber: String,
    val notes: String = "Monthly fee collection",
    val shiftName: String = "Full Day",
    val seatNumber: String = ""
)

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val libraryId: String = "lib_default_01",
    val branchId: String = "branch_01",
    val category: ExpenseCategory,
    val title: String,
    val amount: Int,
    val expenseDate: String,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val description: String = ""
)

data class AttendanceRecord(
    val id: String = UUID.randomUUID().toString(),
    val libraryId: String = "lib_default_01",
    val branchId: String = "branch_01",
    val studentId: String,
    val studentName: String,
    val studentCode: String,
    val shiftName: String,
    val seatNumber: String,
    val attendanceDate: String,
    val checkInTime: String? = "08:15 AM",
    val checkOutTime: String? = null,
    val status: AttendanceStatus = AttendanceStatus.PRESENT
)

data class RegistrationRequest(
    val id: String = UUID.randomUUID().toString(),
    val studentName: String,
    val mobile: String,
    val email: String,
    val course: String,
    val requestedShift: String,
    val preferredSeat: String = "Floating",
    val requestDate: String,
    val status: String = "pending" // pending, approved, rejected
)

data class AuditLog(
    val id: String = UUID.randomUUID().toString(),
    val action: String,
    val entity: String,
    val details: String,
    val timestamp: String = "2026-08-28 09:00 AM",
    val user: String = "Owner (Ratnesh)"
)

data class SaaSSubscription(
    val planType: SaaSPlanType = SaaSPlanType.FREE,
    val billingPeriod: BillingPeriod = BillingPeriod.MONTHLY,
    val startDate: String = "2026-08-01",
    val endDate: String = "2099-12-31",
    val isActive: Boolean = true,
    val allowedBranchesCount: Int = 1
)
