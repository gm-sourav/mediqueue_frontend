package com.mediqueue.app.ui.screens.patient

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediqueue.app.R
import com.mediqueue.app.data.ApiClient
import com.mediqueue.app.data.AppointmentDto
import com.mediqueue.app.data.BookRequest
import com.mediqueue.app.data.DepartmentDto
import com.mediqueue.app.data.DoctorDto
import com.mediqueue.app.data.PrescriptionDto
import com.mediqueue.app.data.QueueDto
import com.mediqueue.app.data.SlotDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime

class PatientViewModel : ViewModel() {

    var departments by mutableStateOf<List<DepartmentDto>?>(null)
        private set
    var allDoctors by mutableStateOf<List<DoctorDto>?>(null)
        private set
    var deptDoctors by mutableStateOf<List<DoctorDto>?>(null)
        private set
    var upcoming by mutableStateOf<Map<Long, List<Pair<LocalDate, LocalTime>>>>(emptyMap())
        private set
    var week by mutableStateOf<Map<LocalDate, List<SlotDto>>?>(null)
        private set
    var appointments by mutableStateOf<List<AppointmentDto>?>(null)
        private set
    var prescriptions by mutableStateOf<List<PrescriptionDto>?>(null)
        private set
    var queue by mutableStateOf<QueueDto?>(null)
        private set
    var isBooking by mutableStateOf(false)
        private set
    var errorRes by mutableStateOf<Int?>(null)
        private set
    var unauthorized by mutableStateOf(false)

    private var deptDoctorsFor = -1L

    // ---------- helpers ----------
    private fun handleHttp(code: Int) {
        if (code == 401 || code == 403) unauthorized = true
        errorRes = R.string.error_generic
    }

    private suspend fun <T> call(block: suspend () -> Response<T>): T? = try {
        val res = block()
        if (res.isSuccessful) {
            res.body()
        } else {
            handleHttp(res.code())
            null
        }
    } catch (_: IOException) {
        errorRes = R.string.error_network
        null
    } catch (_: Exception) {
        errorRes = R.string.error_generic
        null
    }

    private suspend fun fetchSlots(doctorId: Long, date: LocalDate): List<SlotDto>? = try {
        val res = ApiClient.api.getDoctorSlots(doctorId, date.toString())
        if (res.isSuccessful) res.body().orEmpty() else null
    } catch (_: Exception) {
        null
    }

    fun reset() {
        departments = null
        allDoctors = null
        deptDoctors = null
        deptDoctorsFor = -1L
        upcoming = emptyMap()
        week = null
        appointments = null
        prescriptions = null
        queue = null
        isBooking = false
        errorRes = null
        unauthorized = false
    }

    fun clearDoctors() {
        deptDoctors = null
    }

    fun clearWeek() {
        week = null
    }

    // ---------- Departments ----------
    fun loadDepartments() {
        viewModelScope.launch {
            errorRes = null
            val deps = call { ApiClient.api.getDepartments() }
            val docs = call { ApiClient.api.getDoctors() }
            if (deps != null) departments = deps
            if (docs != null) allDoctors = docs
        }
    }

    // ---------- Doctors of a department (+ next slots, 3 din) ----------
    fun loadDoctors(departmentId: Long) {
        viewModelScope.launch {
            errorRes = null
            if (deptDoctorsFor != departmentId) {
                deptDoctors = null
                upcoming = emptyMap()
            }
            val docs = call { ApiClient.api.getDoctors(departmentId = departmentId) } ?: return@launch

            val today = LocalDate.now()
            val now = LocalTime.now()
            val pairs = coroutineScope {
                docs.map { d ->
                    async {
                        val result = mutableListOf<Pair<LocalDate, LocalTime>>()
                        for (i in 0..2) {
                            val date = today.plusDays(i.toLong())
                            val slots = fetchSlots(d.id, date).orEmpty()
                            slots.filter { it.status == "AVAILABLE" }
                                .mapNotNull { runCatching { LocalTime.parse(it.startTime) }.getOrNull() }
                                .filter { i > 0 || it.isAfter(now) }
                                .sorted()
                                .forEach { result.add(date to it) }
                            if (result.size >= 3) break
                        }
                        d.id to result.take(3).toList()
                    }
                }.awaitAll()
            }
            upcoming = pairs.toMap()
            deptDoctors = docs
            deptDoctorsFor = departmentId
        }
    }

    // ---------- Slots (7 din) ----------
    fun loadWeek(doctorId: Long) {
        viewModelScope.launch {
            errorRes = null
            week = null
            val today = LocalDate.now()
            val days = coroutineScope {
                (0..6).map { i ->
                    val d = today.plusDays(i.toLong())
                    async { d to fetchSlots(doctorId, d) }
                }.awaitAll()
            }
            if (days.all { it.second == null }) {
                errorRes = R.string.error_network
            } else {
                week = days.associate { it.first to it.second.orEmpty() }
            }
        }
    }

    // ---------- Booking ----------
    fun book(slotId: Long, reason: String, onDone: (success: Boolean, errorRes: Int?) -> Unit) {
        if (isBooking) return
        isBooking = true
        viewModelScope.launch {
            try {
                val res = ApiClient.api.bookAppointment(BookRequest(slotId, reason))
                if (res.isSuccessful) {
                    onDone(true, null)
                } else {
                    if (res.code() == 401 || res.code() == 403) unauthorized = true
                    onDone(
                        false,
                        if (res.code() in 400..499) R.string.error_booking_failed else R.string.error_generic
                    )
                }
            } catch (_: IOException) {
                onDone(false, R.string.error_network)
            } catch (_: Exception) {
                onDone(false, R.string.error_generic)
            } finally {
                isBooking = false
            }
        }
    }

    // ---------- My appointments ----------
    private suspend fun fetchAppointments() {
        if (allDoctors == null) {
            call { ApiClient.api.getDoctors() }?.let { allDoctors = it }
        }
        val list = call { ApiClient.api.getMyAppointments() }
        if (list != null) appointments = list
    }

    fun loadPrescriptions() {
        viewModelScope.launch {
            val list = call { ApiClient.api.getMyPrescriptions() }
            if (list != null) prescriptions = list
        }
    }

    /** Ajker CONFIRMED (token paoa) appointment: live queue er jonno */
    val activeAppointment: AppointmentDto?
        get() {
            val today = LocalDate.now().toString()
            return appointments
                ?.filter { it.slotDate == today && it.status == "CONFIRMED" && it.tokenNo != null }
                ?.minByOrNull { it.startTime }
        }

    private suspend fun fetchQueue() {
        val a = activeAppointment
        if (a == null) {
            queue = null
            return
        }
        val q = call { ApiClient.api.getDoctorQueue(a.doctorId, LocalDate.now().toString()) }
        if (q != null) queue = q
    }

    /** Home / Live Queue screen: appointment + queue duitai ana */
    fun loadHome() {
        viewModelScope.launch {
            errorRes = null
            fetchAppointments()
            fetchQueue()
        }
    }

    /** Live Queue screen e 10 sec por por */
    fun refreshQueue() {
        viewModelScope.launch { fetchQueue() }
    }

    fun loadAppointments() {
        viewModelScope.launch {
            errorRes = null
            fetchAppointments()
        }
    }

    fun cancel(id: Long, onDone: (success: Boolean, errorRes: Int?) -> Unit) {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.cancelAppointment(id)
                if (res.isSuccessful) {
                    fetchAppointments()
                    onDone(true, null)
                } else {
                    if (res.code() == 401 || res.code() == 403) unauthorized = true
                    onDone(false, R.string.error_generic)
                }
            } catch (_: IOException) {
                onDone(false, R.string.error_network)
            } catch (_: Exception) {
                onDone(false, R.string.error_generic)
            }
        }
    }
}