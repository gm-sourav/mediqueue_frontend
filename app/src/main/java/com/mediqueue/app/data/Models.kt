package com.mediqueue.app.data

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val id: Long,
    val name: String,
    val email: String,
    val role: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String
)

// Register er response e token nai
data class RegisterResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: String
)

data class DepartmentDto(
    val id: Long,
    val name: String,
    val icon: String?
)

data class DoctorDto(
    val id: Long,
    val name: String,
    val email: String?,
    val phone: String?,
    val departmentId: Long,
    val departmentName: String,
    val bio: String?,
    val experienceYears: Int,
    val fee: Double,
    val photoUrl: String?
)

data class SlotDto(
    val id: Long,
    val doctorId: Long,
    val slotDate: String,      // "2026-11-09"
    val startTime: String,     // "09:00:00"
    val endTime: String,
    val status: String         // "AVAILABLE" / booked
)

data class BookRequest(
    val slotId: Long,
    val reason: String
)

data class AppointmentDto(
    val id: Long,
    val patientId: Long,
    val patientName: String,
    val doctorId: Long,
    val doctorName: String,
    val departmentName: String,
    val slotDate: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val reason: String?,
    val tokenNo: Int?
)
// ---------------- Doctor side ----------------

data class CreateSlotRequest(
    val slotDate: String,            // "2026-11-12"
    val startTime: String,           // "09:00"
    val endTime: String,             // "12:00"
    val slotDurationMinutes: Int
)

data class QueueDto(
    val doctorId: Long,
    val doctorName: String,
    val queueDate: String,
    val currentToken: Int,
    val waitingCount: Long
)

data class PrescriptionRequest(
    val diagnosis: String,
    val medicines: String,
    val advice: String?
)

// ---------------- Prescriptions ----------------

data class PrescriptionDto(
    val id: Long,
    val appointmentId: Long,
    val doctorName: String,
    val patientName: String,
    val visitDate: String,       // "2026-11-11"
    val diagnosis: String?,
    val medicines: String?,      // protita line: "name | dosage | frequency | duration"
    val advice: String?
)