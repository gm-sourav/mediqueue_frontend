package com.mediqueue.app.ui.screens.patient

import com.mediqueue.app.data.AppointmentDto
import com.mediqueue.app.data.DepartmentDto
import com.mediqueue.app.data.DoctorDto
import com.mediqueue.app.data.SlotDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle
import java.util.Locale

private fun fmt(pattern: String, locale: Locale) =
    DateTimeFormatter.ofPattern(pattern, locale).withDecimalStyle(DecimalStyle.of(locale))

fun formatTime(raw: String, locale: Locale): String =
    runCatching { LocalTime.parse(raw).format(fmt("h:mm a", locale)) }.getOrDefault(raw)

fun formatTime(t: LocalTime, locale: Locale): String = t.format(fmt("h:mm a", locale))

/** Today / Tomorrow, na hole pattern onujayi (jemon "EEE" ba "MMM d") */
fun dayLabel(
    d: LocalDate,
    today: LocalDate,
    todayWord: String,
    tomorrowWord: String,
    locale: Locale,
    pattern: String
): String = when (d) {
    today -> todayWord
    today.plusDays(1) -> tomorrowWord
    else -> d.format(fmt(pattern, locale))
}

fun slotLabel(
    p: Pair<LocalDate, LocalTime>,
    today: LocalDate,
    todayWord: String,
    tomorrowWord: String,
    locale: Locale
): String = dayLabel(p.first, today, todayWord, tomorrowWord, locale, "EEE") +
        " · " + formatTime(p.second, locale)

fun departmentGlyph(icon: String?): String = when (icon?.lowercase()) {
    "heart" -> "♡"
    "brain" -> "✳"
    "tooth" -> "+"
    "eye" -> "◉"
    "child" -> "⌂"
    "bone" -> "+"
    else -> "+"
}

// NOTE: jodi "Type mismatch: required Long, found Int" error ashe,
// shei line theke `.toInt()` muche dao (Department, DoctorItem, DoctorDetail er id).

fun DepartmentDto.toUi(doctorCount: Int): Department =
    Department(id, name, doctorCount, departmentGlyph(icon))

fun DoctorDto.toItem(nextSlot: String?, availableToday: Boolean): DoctorItem =
    DoctorItem(id, name, experienceYears, departmentName, fee.toInt(), nextSlot, availableToday)

fun DoctorDto.toDetail(nextSlots: List<String>, availableToday: Boolean): DoctorDetail =
    DoctorDetail(
        id = id,
        name = name,
        specialty = departmentName,
        department = departmentName,
        experienceYears = experienceYears,
        fee = fee.toInt(),
        rating = 0.0,                 // backend e rating nai
        about = bio.orEmpty(),
        availableToday = availableToday,
        nextSlots = nextSlots
    )

fun List<SlotDto>.toSlotItems(
    date: LocalDate,
    today: LocalDate,
    now: LocalTime,
    locale: Locale
): List<SlotItem> = sortedBy { it.startTime }.map { s ->
    val start = runCatching { LocalTime.parse(s.startTime) }.getOrNull()
    val alreadyPassed = date == today && start != null && !start.isAfter(now)
    SlotItem(s.id, formatTime(s.startTime, locale), s.status != "AVAILABLE" || alreadyPassed)
}

private fun isUpcoming(status: String) = status == "PENDING" || status == "CONFIRMED"

/** Upcoming age (nikotom age), tarpor past (notun age) */
fun List<AppointmentDto>.toItems(
    fees: Map<Long, Int>,
    today: LocalDate,
    todayWord: String,
    tomorrowWord: String,
    locale: Locale
): List<AppointmentItem> {
    val up = filter { isUpcoming(it.status) }
        .sortedWith(compareBy({ it.slotDate }, { it.startTime }))
    val past = filter { !isUpcoming(it.status) }
        .sortedWith(compareByDescending<AppointmentDto> { it.slotDate }.thenByDescending { it.startTime })
    return (up + past).map { a ->
        val d = LocalDate.parse(a.slotDate)
        AppointmentItem(
            a.id,
            a.status,
            a.tokenNo,
            a.doctorName,
            a.departmentName,
            dayLabel(d, today, todayWord, tomorrowWord, locale, "MMM d"),
            formatTime(a.startTime, locale),
            d.format(fmt("MMM d", locale)),
            fees[a.doctorId] ?: 0
        )
    }
}

/** Home er "Next appointment": ajker ba porer sobcheye kachher pending/confirmed */
fun nextAppointmentOf(list: List<AppointmentDto>?, today: LocalDate): AppointmentDto? =
    list?.filter { isUpcoming(it.status) && !LocalDate.parse(it.slotDate).isBefore(today) }
        ?.minWithOrNull(compareBy({ it.slotDate }, { it.startTime }))

fun AppointmentDto.toNext(
    today: LocalDate,
    todayWord: String,
    tomorrowWord: String,
    locale: Locale
): NextAppointment {
    val d = LocalDate.parse(slotDate)
    return NextAppointment(
        doctorName = doctorName,
        department = departmentName,
        whenText = dayLabel(d, today, todayWord, tomorrowWord, locale, "EEE, MMM d") +
                ", " + formatTime(startTime, locale),
        token = tokenNo ?: 0,
        status = status

    )
}