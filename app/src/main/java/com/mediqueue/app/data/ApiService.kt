package com.mediqueue.app.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ---- Auth ----
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    // ---- Common ----
    @GET("api/departments")
    suspend fun getDepartments(): Response<List<DepartmentDto>>

    @GET("api/doctors")
    suspend fun getDoctors(
        @Query("departmentId") departmentId: Long? = null,
        @Query("name") name: String? = null
    ): Response<List<DoctorDto>>

    @GET("api/doctors/{id}/slots")
    suspend fun getDoctorSlots(
        @Path("id") doctorId: Long,
        @Query("date") date: String
    ): Response<List<SlotDto>>

    // ---- Patient ----
    @POST("api/patient/appointments")
    suspend fun bookAppointment(@Body body: BookRequest): Response<AppointmentDto>

    @GET("api/patient/appointments")
    suspend fun getMyAppointments(): Response<List<AppointmentDto>>

    @PUT("api/patient/appointments/{id}/cancel")
    suspend fun cancelAppointment(@Path("id") id: Long): Response<ResponseBody>

    @GET("api/doctors/{id}/queue")
    suspend fun getDoctorQueue(
        @Path("id") doctorId: Long,
        @Query("date") date: String
    ): Response<QueueDto>

    @GET("api/patient/prescriptions")
    suspend fun getMyPrescriptions(): Response<List<PrescriptionDto>>

    // ---- Doctor ----
    @GET("api/doctor/appointments")
    suspend fun getDoctorAppointments(@Query("date") date: String): Response<List<AppointmentDto>>

    @PUT("api/doctor/appointments/{id}/confirm")
    suspend fun confirmAppointment(@Path("id") id: Long): Response<AppointmentDto>

    @PUT("api/doctor/appointments/{id}/reject")
    suspend fun rejectAppointment(@Path("id") id: Long): Response<AppointmentDto>

    @PUT("api/doctor/appointments/{id}/complete")
    suspend fun completeAppointment(@Path("id") id: Long): Response<AppointmentDto>

    @POST("api/doctor/slots")
    suspend fun createSlots(@Body body: CreateSlotRequest): Response<List<SlotDto>>

    @GET("api/doctor/queue")
    suspend fun getMyQueue(@Query("date") date: String): Response<QueueDto>

    @PUT("api/doctor/queue/next")
    suspend fun callNextToken(@Query("date") date: String): Response<QueueDto>

    @POST("api/doctor/appointments/{id}/prescription")
    suspend fun writePrescription(
        @Path("id") id: Long,
        @Body body: PrescriptionRequest
    ): Response<ResponseBody>

    // ---- Admin ----
    @POST("api/admin/departments")
    suspend fun createDepartment(@Body body: DepartmentRequest): Response<DepartmentDto>

    @POST("api/admin/doctors")
    suspend fun createDoctor(@Body body: CreateDoctorRequest): Response<DoctorDto>
}