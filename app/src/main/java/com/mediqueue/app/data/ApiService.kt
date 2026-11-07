package com.mediqueue.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    // Register, departments, doctors, appointments... porer step e ekta ekta kore jog hobe
}