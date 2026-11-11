package com.mediqueue.app.ui.screens.patient

import com.mediqueue.app.data.AppointmentDto
import com.mediqueue.app.data.PrescriptionDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Backend e medicines ekta text, protita line:
 * "name | dosage | frequency | duration"
 * (Postman e onno format e likhle: puro line ta name hishebe dekhabe)
 */
fun parseMedicines(text: String?): List<MedicineItem> =
    text.orEmpty().lines().map { it.trim() }.filter { it.isNotEmpty() }.map { line ->
        val parts = line.split("|").map { it.trim() }
        MedicineItem(
            name = parts.getOrElse(0) { line },
            dose = parts.getOrElse(1) { "" }.ifBlank { "—" },
            timing = listOf(parts.getOrElse(2) { "" }, parts.getOrElse(3) { "" })
                .filter { it.isNotBlank() }
                .joinToString(" · ")
        )
    }

/**
 * Department backend er prescription e nai, tai appointment theke nei.
 * "New" = last 7 din er visit (backend e read/unread flag nai).
 */
fun List<PrescriptionDto>.toPrescriptionItems(
    appointments: List<AppointmentDto>,
    today: LocalDate,
    locale: Locale
): List<PrescriptionItem> {
    val dateFmt = DateTimeFormatter.ofPattern("MMM dd", locale).withDecimalStyle(DecimalStyle.of(locale))
    val deptByAppt = appointments.associate { it.id to it.departmentName }
    return map { p ->
        val visit = runCatching { LocalDate.parse(p.visitDate) }.getOrNull()
        PrescriptionItem(
            id = p.id,
            dateText = visit?.format(dateFmt) ?: p.visitDate,
            department = deptByAppt[p.appointmentId].orEmpty(),
            doctorName = p.doctorName,
            summary = p.diagnosis.orEmpty(),
            isNew = visit != null && ChronoUnit.DAYS.between(visit, today) in 0..7,
            medicines = parseMedicines(p.medicines),
            note = p.advice?.takeIf { it.isNotBlank() }
        )
    }
}