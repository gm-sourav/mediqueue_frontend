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