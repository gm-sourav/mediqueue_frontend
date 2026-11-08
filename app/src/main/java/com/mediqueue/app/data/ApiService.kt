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
}