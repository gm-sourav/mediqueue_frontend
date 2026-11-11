package com.mediqueue.app.ui.screens.doctor

import com.mediqueue.app.data.AppointmentDto
import com.mediqueue.app.data.QueueDto
import com.mediqueue.app.ui.screens.patient.formatTime
import java.util.Locale

/**
 * Backend e medicines ekta text. Protita medicine ek line:
 * "name | dosage | frequency | duration"
 * (porer step e patient er Prescriptions screen ei line gulo bhenge dekhabe)
 */
fun medicinesToText(list: List<MedicineEntry>): String =
    list.filter { it.name.isNotBlank() }.joinToString("\n") {
        listOf(it.name, it.dosage, it.frequency, it.duration).joinToString(" | ") { s -> s.trim() }
    }

fun List<AppointmentDto>.toDoctorAppointments(locale: Locale, noReason: String): List<DoctorAppointment> =
    sortedBy { it.startTime }.map {
        DoctorAppointment(
            id = it.id,
            time = formatTime(it.startTime, locale),
            patientName = it.patientName,
            reason = it.reason?.takeIf { r -> r.isNotBlank() } ?: noReason,
            department = it.departmentName,
            status = it.status,
            token = it.tokenNo
        )
    }

fun queueInfoOf(
    q: QueueDto,
    list: List<AppointmentDto>,
    department: String,
    locale: Locale
): QueueControlInfo {
    val current = q.currentToken
    val numbered = list.filter {
        it.tokenNo != null && (it.status == "CONFIRMED" || it.status == "COMPLETED")
    }
    val serving = if (current > 0) numbered.firstOrNull { it.tokenNo == current } else null
    val waiting = numbered
        .filter { it.status == "CONFIRMED" && (it.tokenNo ?: 0) > current }
        .sortedBy { it.tokenNo }
        .map { QueueEntry(it.tokenNo ?: 0, it.patientName, formatTime(it.startTime, locale)) }
    return QueueControlInfo(
        department = department,
        nowServingToken = if (current > 0) current else null,
        nowServingName = serving?.patientName,
        nowServingTime = serving?.let { formatTime(it.startTime, locale) },
        waiting = waiting
    )
}