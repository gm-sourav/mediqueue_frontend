package com.mediqueue.app.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediqueue.app.R
import com.mediqueue.app.data.ApiClient
import com.mediqueue.app.data.CreateDoctorRequest
import com.mediqueue.app.data.DepartmentDto
import com.mediqueue.app.data.DepartmentRequest
import com.mediqueue.app.data.DoctorDto
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
            a.await() to b.await()
        }
        if (deps != null) departments = deps
        if (docs != null) doctors = docs
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

    fun createDoctor(body: CreateDoctorRequest, onDone: (success: Boolean, errorRes: Int?) -> Unit) {
        if (isSubmitting) return
        isSubmitting = true
        viewModelScope.launch {
            try {
                val res = ApiClient.api.createDoctor(body)
                if (res.isSuccessful) {
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
}