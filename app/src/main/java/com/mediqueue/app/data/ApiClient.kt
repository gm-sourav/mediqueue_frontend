package com.mediqueue.app.data

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // Emulator theke PC er localhost
    // BuildConfig.SERVER_URL (app/build.gradle.kts) set hole oi deployed server, na hole local PC
    val BASE_URL: String =
        when {
            com.mediqueue.app.BuildConfig.SERVER_URL.isNotBlank() ->
                com.mediqueue.app.BuildConfig.SERVER_URL.trimEnd('/') + "/"
            isEmulator() -> "http://10.0.2.2:8080/"
            else -> "http://localhost:8080/"
        }

    private fun isEmulator(): Boolean =
        android.os.Build.FINGERPRINT.contains("generic") ||
                android.os.Build.FINGERPRINT.contains("emulator") ||
                android.os.Build.HARDWARE.contains("ranchu") ||
                android.os.Build.HARDWARE.contains("goldfish") ||
                android.os.Build.PRODUCT.contains("sdk")

    /** Server sends "/uploads/x.jpg". Make it a full URL for Coil. */
    fun absoluteUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("http")) path else BASE_URL.trimEnd('/') + path
    }

    lateinit var session: SessionManager
        private set

    fun init(context: Context) {
        session = SessionManager(context)
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                session.token?.let { builder.addHeader("Authorization", "Bearer $it") }
                chain.proceed(builder.build())
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}