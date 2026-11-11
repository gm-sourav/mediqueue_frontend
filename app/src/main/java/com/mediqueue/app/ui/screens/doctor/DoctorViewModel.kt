package com.mediqueue.app.ui.screens.doctor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediqueue.app.R
import com.mediqueue.app.data.ApiClient
import com.mediqueue.app.data.AppointmentDto
import com.mediqueue.app.data.CreateSlotRequest
import com.mediqueue.app.data.PrescriptionRequest
import com.mediqueue.app.data.QueueDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

enum class DoctorAction { CONFIRM, REJECT, COMPLETE }

class DoctorViewModel : ViewModel() {

    /** Dashboard: oi dine er appointment. null = ekhono load hocche ba error */
    var appointments by mutableStateOf<List<AppointmentDto>?>(null)
        private set

    /** Queue screen */
    var queue by mutableStateOf<QueueDto?>(null)
        private set
    var queueAppointments by mutableStateOf<List<AppointmentDto>?>(null)
        private set
    var myDepartment by mutableStateOf("")
        private set

    var isSubmitting by mutableStateOf(false)
        private set
    var isCalling by mutableStateOf(false)
        private set
    var errorRes by mutableStateOf<Int?>(null)
        private set
    var unauthorized by mutableStateOf(false)

    private var currentDate: LocalDate = LocalDate.now()

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

    fun reset() {
        appointments = null
        queue = null
        queueAppointments = null
        myDepartment = ""
        isSubmitting = false
        isCalling = false
        errorRes = null
        unauthorized = false
        currentDate = LocalDate.now()
    }

    // ---------- Dashboard ----------
    /**
     * clear = true  : screen e prothom dhukle, loading dekhabe
     * clear = false : date chip e tap korle, screen er date thik thakbe, list khali hoye notun ta ashbe
     */
    fun loadAppointments(date: LocalDate, clear: Boolean) {
        currentDate = date
        errorRes = null
        appointments = if (clear) null else emptyList()
        viewModelScope.launch {
            val list = call { ApiClient.api.getDoctorAppointments(date.toString()) }
            if (date != currentDate) return@launch      // porer date e chole gechi, purono response felo
            appointments = list                          // fail hole null -> error + Retry
        }
    }

    fun act(id: Long, action: DoctorAction, onDone: (success: Boolean, errorRes: Int?) -> Unit) {
        viewModelScope.launch {
            try {
                val res = when (action) {
                    DoctorAction.CONFIRM -> ApiClient.api.confirmAppointment(id)
                    DoctorAction.REJECT -> ApiClient.api.rejectAppointment(id)
                    DoctorAction.COMPLETE -> ApiClient.api.completeAppointment(id)
                }
                val updated = res.body()
                if (res.isSuccessful && updated != null) {
                    appointments = appointments?.map { if (it.id == updated.id) updated else it }
                    onDone(true, null)
                } else {
                    if (res.code() == 401 || res.code() == 403) unauthorized = true
                    // status bodle gele (409) list ta notun kore anbo
                    val date = currentDate
                    call { ApiClient.api.getDoctorAppointments(date.toString()) }?.let {
                        if (date == currentDate) appointments = it
                    }
                    onDone(false, R.string.error_generic)
                }
            } catch (_: IOException) {
                onDone(false, R.string.error_network)
            } catch (_: Exception) {
                onDone(false, R.string.error_generic)
            }
        }
    }

    // ---------- Create slots ----------
    fun createSlots(
        date: LocalDate,
        start: LocalTime,
        end: LocalTime,
        lengthMin: Int,
        onDone: (success: Boolean, createdCount: Int, errorRes: Int?) -> Unit
    ) {
        if (isSubmitting) return
        isSubmitting = true
        viewModelScope.launch {
            try {
                val body = CreateSlotRequest(
                    slotDate = date.toString(),
                    startTime = hhmm(start),
                    endTime = hhmm(end),
                    slotDurationMinutes = lengthMin
                )
                val res = ApiClient.api.createSlots(body)
                if (res.isSuccessful) {
                    onDone(true, res.body()?.size ?: 0, null)
                } else {
                    if (res.code() == 401 || res.code() == 403) unauthorized = true
                    onDone(
                        false, 0,
                        if (res.code() == 409) R.string.error_slots_exist else R.string.error_generic
                    )
                }
            } catch (_: IOException) {
                onDone(false, 0, R.string.error_network)
            } catch (_: Exception) {
                onDone(false, 0, R.string.error_generic)
            } finally {
                isSubmitting = false
            }
        }
    }

    private fun hhmm(t: LocalTime): String =
        String.format(Locale.US, "%02d:%02d", t.hour, t.minute)

    // ---------- Queue ----------
    fun loadQueue(clear: Boolean = true) {
        errorRes = null
        if (clear) {
            queue = null
            queueAppointments = null
        }
        viewModelScope.launch { fetchQueue() }
    }

    private suspend fun fetchQueue() {
        val today = LocalDate.now().toString()
        val (q, list) = coroutineScope {
            val a = async { call { ApiClient.api.getMyQueue(today) } }
            val b = async { call { ApiClient.api.getDoctorAppointments(today) } }
            a.await() to b.await()
        }
        if (myDepartment.isEmpty()) {
            val email = ApiClient.session.email
            call { ApiClient.api.getDoctors() }
                ?.firstOrNull { it.email.equals(email, ignoreCase = true) }
                ?.let { myDepartment = it.departmentName }
        }
        if (q != null && list != null) {
            queue = q
            queueAppointments = list
        }
    }

    fun callNext(onDone: (success: Boolean, errorRes: Int?) -> Unit) {
        if (isCalling) return
        isCalling = true
        viewModelScope.launch {
            try {
                val res = ApiClient.api.callNextToken(LocalDate.now().toString())
                if (res.isSuccessful) {
                    fetchQueue()
                    onDone(true, null)
                } else {
                    if (res.code() == 401 || res.code() == 403) unauthorized = true
                    onDone(
                        false,
                        if (res.code() == 409) R.string.queue_no_more else R.string.error_generic
                    )
                }
            } catch (_: IOException) {
                onDone(false, R.string.error_network)
            } catch (_: Exception) {
                onDone(false, R.string.error_generic)
            } finally {
                isCalling = false
            }
        }
    }

    // ---------- Prescription ----------
    fun savePrescription(
        appointmentId: Long,
        diagnosis: String,
        medicines: List<MedicineEntry>,
        notes: String,
        onDone: (success: Boolean, errorRes: Int?) -> Unit
    ) {
        if (isSubmitting) return
        isSubmitting = true
        viewModelScope.launch {
            try {
                val res = ApiClient.api.writePrescription(
                    appointmentId,
                    PrescriptionRequest(
                        diagnosis = diagnosis.trim(),
                        medicines = medicinesToText(medicines),
                        advice = notes.trim().ifBlank { null }
                    )
                )
                if (res.isSuccessful) {
                    onDone(true, null)
                } else {
                    if (res.code() == 401 || res.code() == 403) unauthorized = true
                    onDone(
                        false,
                        if (res.code() in 400..499) R.string.error_rx_failed else R.string.error_generic
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