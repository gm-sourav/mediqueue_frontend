package com.mediqueue.app.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediqueue.app.R
import com.mediqueue.app.data.AdminStatsDto
import com.mediqueue.app.data.ApiClient
import com.mediqueue.app.data.CreateDoctorRequest
import com.mediqueue.app.data.DepartmentDto
import com.mediqueue.app.data.DepartmentRequest
import com.mediqueue.app.data.DoctorDto
import com.mediqueue.app.data.UpdateDoctorRequest
import com.mediqueue.app.ui.components.AvatarStore
import com.mediqueue.app.ui.components.compressForUpload
import com.mediqueue.app.ui.components.photoPart
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class AdminViewModel : ViewModel() {

    var departments by mutableStateOf<List<DepartmentDto>?>(null)
        private set
    var doctors by mutableStateOf<List<DoctorDto>?>(null)
        private set
    var stats by mutableStateOf<AdminStatsDto?>(null)
        private set
    var isSubmitting by mutableStateOf(false)
        private set
    var errorRes by mutableStateOf<Int?>(null)
        private set
    var unauthorized by mutableStateOf(false)

    private suspend fun <T> call(block: suspend () -> Response<T>): T? = try {
        val res = block()
        if (res.isSuccessful) {
            res.body()
        } else {
            if (res.code() == 401 || res.code() == 403) unauthorized = true
            errorRes = R.string.error_generic
            null
        }
    } catch (_: IOException) {
        errorRes = R.string.error_network
        null
    } catch (_: Exception) {
        errorRes = R.string.error_generic
        null
    }

    fun reset() {
        stats = null
        departments = null
        doctors = null
        isSubmitting = false
        errorRes = null
        unauthorized = false
    }

    /** Departments + doctors duitai ekshathe ana */
    fun load() {
        viewModelScope.launch { fetchAll() }
    }

    private suspend fun fetchAll() {
        errorRes = null
        val (deps, docs) = coroutineScope {
            val a = async { call { ApiClient.api.getDepartments() } }
            val b = async { call { ApiClient.api.getDoctors() } }
            // Stats are only decoration on the home screen. If they fail, the rest still works.
            val c = async {
                runCatching { ApiClient.api.getAdminStats().takeIf { it.isSuccessful }?.body() }.getOrNull()
            }
            c.await()?.let { stats = it }
            a.await() to b.await()
        }
        if (deps != null) departments = deps
        if (docs != null) {
            doctors = docs
            AvatarStore.putDoctors(docs)
        }
    }

    fun createDepartment(name: String, icon: String, onDone: (success: Boolean, errorRes: Int?) -> Unit) {
        if (isSubmitting) return
        isSubmitting = true
        viewModelScope.launch {
            try {
                val res = ApiClient.api.createDepartment(DepartmentRequest(name.trim(), icon))
                if (res.isSuccessful) {
                    fetchAll()
                    onDone(true, null)
                } else {
                    if (res.code() == 401 || res.code() == 403) unauthorized = true
                    onDone(
                        false,
                        if (res.code() in 400..499) R.string.error_department_create else R.string.error_generic
                    )
                }
            } catch (_: IOException) {
                onDone(false, R.string.error_network)
            } catch (_: Exception) {
                onDone(false, R.string.error_generic)
            } finally {
                isSubmitting = false
            }
        }
    }

    fun createDoctor(
        body: CreateDoctorRequest,
        photoUri: Uri?,
        context: Context,
        onDone: (success: Boolean, errorRes: Int?) -> Unit
    ) {
        if (isSubmitting) return
        isSubmitting = true
        viewModelScope.launch {
            try {
                val res = ApiClient.api.createDoctor(body)
                if (res.isSuccessful) {
                    // Photo is optional. If it fails the doctor is still created.
                    val created = res.body()
                    if (photoUri != null && created != null) {
                        runCatching {
                            compressForUpload(context, photoUri)?.let {
                                ApiClient.api.uploadDoctorPhoto(created.id, photoPart(it))
                            }
                        }
                    }
                    fetchAll()
                    onDone(true, null)
                } else {
                    if (res.code() == 401 || res.code() == 403) unauthorized = true
                    onDone(
                        false,
                        if (res.code() in 400..499) R.string.error_doctor_create else R.string.error_generic
                    )
                }
            } catch (_: IOException) {
                onDone(false, R.string.error_network)
            } catch (_: Exception) {
                onDone(false, R.string.error_generic)
            } finally {
                isSubmitting = false
            }
        }
    }

    fun updateDoctor(
        id: Long,
        body: UpdateDoctorRequest,
        photoUri: Uri?,
        context: Context,
        onDone: (success: Boolean, errorRes: Int?) -> Unit
    ) {
        if (isSubmitting) return
        isSubmitting = true
        viewModelScope.launch {
            try {
                val res = ApiClient.api.updateDoctor(id, body)
                if (res.isSuccessful) {
                    if (photoUri != null) {
                        runCatching {
                            compressForUpload(context, photoUri)?.let {
                                ApiClient.api.uploadDoctorPhoto(id, photoPart(it))
                            }
                        }
                    }
                    fetchAll()
                    onDone(true, null)
                } else {
                    if (res.code() == 401 || res.code() == 403) unauthorized = true
                    onDone(
                        false,
                        if (res.code() in 400..499) R.string.error_doctor_update else R.string.error_generic
                    )
                }
            } catch (_: IOException) {
                onDone(false, R.string.error_network)
            } catch (_: Exception) {
                onDone(false, R.string.error_generic)
            } finally {
                isSubmitting = false
            }
        }
    }

    fun deleteDoctor(id: Long, onDone: (success: Boolean, errorRes: Int?) -> Unit) {
        if (isSubmitting) return
        isSubmitting = true
        viewModelScope.launch {
            try {
                val res = ApiClient.api.deleteDoctor(id)
                if (res.isSuccessful) {
                    fetchAll()
                    onDone(true, null)
                } else {
                    if (res.code() == 401 || res.code() == 403) unauthorized = true
                    onDone(
                        false,
                        when (res.code()) {
                            409 -> R.string.error_doctor_has_appointments
                            in 400..499 -> R.string.error_doctor_delete
                            else -> R.string.error_generic
                        }
                    )
                }
            } catch (_: IOException) {
                onDone(false, R.string.error_network)
            } catch (_: Exception) {
                onDone(false, R.string.error_generic)
            } finally {
                isSubmitting = false
            }
        }
    }
}