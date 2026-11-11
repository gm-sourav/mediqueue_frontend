package com.mediqueue.app.ui.screens.patient

import com.mediqueue.app.data.AppointmentDto
import com.mediqueue.app.data.QueueDto
import java.time.Duration
import java.time.LocalTime
import java.util.Locale

/** Slot er dorghyo (minute). Backend e consult time nai, tai slot length ke average dhorchi */
private fun slotMinutes(a: AppointmentDto): Int =
    runCatching {
        Duration.between(LocalTime.parse(a.startTime), LocalTime.parse(a.endTime)).toMinutes().toInt()
    }.getOrDefault(10).coerceAtLeast(1)

/**
 * Backend queue e shudhu "currentToken" ar "waitingCount" ache.
 * Amar age koyjon = myToken - currentToken - 1 (shobai ase dhorle).
 * Wait = patientsAhead x slot length.
 */
private fun aheadOf(my: Int, q: QueueDto): Int = (my - q.currentToken - 1).coerceAtLeast(0)

private fun progressOf(my: Int, q: QueueDto): Float =
    if (my <= 0) 0f else (q.currentToken.toFloat() / my).coerceIn(0f, 1f)

/** Home screen er card */
fun AppointmentDto.toQueueInfo(q: QueueDto): QueueInfo? {
    val my = tokenNo ?: return null
    val ahead = aheadOf(my, q)
    return QueueInfo(
        myToken = my,
        nowServing = q.currentToken,
        patientsAhead = ahead,
        estimatedMinutes = ahead * slotMinutes(this),
        progress = progressOf(my, q),
        onTrack = true
    )
}

/** Live Queue screen */
fun AppointmentDto.toLiveQueueInfo(q: QueueDto, locale: Locale): LiveQueueInfo {
    val my = tokenNo ?: 0
    val ahead = aheadOf(my, q)
    return LiveQueueInfo(
        department = departmentName,
        doctorName = doctorName,
        timeText = formatTime(startTime, locale),
        myToken = my,
        nowServing = q.currentToken,
        waiting = ahead,
        progress = progressOf(my, q),
        minutesRemaining = ahead * slotMinutes(this)
    )
}