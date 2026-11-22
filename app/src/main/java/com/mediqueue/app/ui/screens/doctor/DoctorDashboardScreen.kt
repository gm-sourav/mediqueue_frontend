package com.mediqueue.app.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.DoctorAvatar
import com.mediqueue.app.ui.components.DoctorBottomBar
import com.mediqueue.app.ui.components.DoctorTab
import com.mediqueue.app.ui.components.IconKind
import com.mediqueue.app.ui.components.MediIcon
import com.mediqueue.app.ui.components.initialsOf
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle

private val DocGreenBg = Color(0xFFDDF3E7)
private val DocGreenText = Color(0xFF1B8A57)
private val DocRedBg = Color(0xFFFDE5E2)
private val DocRedText = Color(0xFFC73B2E)
private val DocBlueBg = Color(0xFFE1EDFB)
private val DocBlueText = Color(0xFF2468B5)
private val DocAmberBg = Color(0xFFFFF1D6)
private val DocAmberText = Color(0xFFB36B00)
private val PatientTints = listOf(
    Color(0xFFD8F0EE), Color(0xFFDCE9FB), Color(0xFFFDE9D9), Color(0xFFEBE4FA), Color(0xFFFCE4EA)
)

/** status: PENDING / CONFIRMED / COMPLETED / CANCELLED / REJECTED */
data class DoctorAppointment(
    val id: Long,
    val time: String,          // "10:00"
    val patientName: String,
    val reason: String,        // "Follow-up"
    val department: String,    // "Cardiology"
    val status: String,
    val token: Int?
)

@Composable
fun DoctorDashboardScreen(
    doctorName: String,
    appointments: List<DoctorAppointment>,
    onDateChange: (LocalDate) -> Unit,
    onConfirm: (DoctorAppointment) -> Unit,
    onReject: (DoctorAppointment) -> Unit,
    onComplete: (DoctorAppointment) -> Unit,
    onAvatarClick: () -> Unit,
    onAppointmentClick: (DoctorAppointment) -> Unit = {},
    onTabSelected: (DoctorTab) -> Unit = {}
) {
    val locale = LocalConfiguration.current.locales[0]
    val today = remember { LocalDate.now() }
    var epochDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    val selectedDate = LocalDate.ofEpochDay(epochDay)

    fun fmt(pattern: String) =
        DateTimeFormatter.ofPattern(pattern, locale).withDecimalStyle(DecimalStyle.of(locale))

    val greeting = stringResource(
        when (LocalTime.now().hour) {
            in 0..11 -> R.string.greet_morning
            in 12..16 -> R.string.greet_afternoon
            else -> R.string.greet_evening
        }
    )
    val todayWord = stringResource(R.string.today)
    val tomorrowWord = stringResource(R.string.tomorrow)
    val dayLabel = when (selectedDate) {
        today -> todayWord
        today.plusDays(1) -> tomorrowWord
        else -> selectedDate.format(fmt("EEE, MMM d"))
    }

    val pendingCount = appointments.count { it.status == "PENDING" }
    val confirmedCount = appointments.count { it.status == "CONFIRMED" }
    val doneCount = appointments.count { it.status == "COMPLETED" }
    val next = appointments.firstOrNull { it.status == "CONFIRMED" || it.status == "PENDING" }

    Scaffold(
        containerColor = MediColors.Bg,
        bottomBar = { DoctorBottomBar(DoctorTab.Today, onTabSelected) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // ---------- Header ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = today.format(fmt("EEEE, MMMM d")).uppercase(locale),
                        color = MediColors.Muted,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = greeting,
                        color = MediColors.Muted,
                        fontSize = 14.sp
                    )
                    Text(
                        text = doctorName,
                        color = MediColors.Text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DoctorAvatar(
                    name = doctorName,
                    avatarSize = 50.dp,
                    corner = 25.dp,
                    modifier = Modifier.clickable(onClick = onAvatarClick)
                )
            }

            // ---------- Schedule card ----------
            Spacer(Modifier.height(18.dp))
            ScheduleCard(
                dayLabel = dayLabel,
                total = appointments.size,
                pending = pendingCount,
                confirmed = confirmedCount,
                done = doneCount,
                next = next
            )

            // ---------- Date strip ----------
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                List(7) { today.plusDays(it.toLong()) }.forEach { d ->
                    DayChip(
                        weekday = d.format(fmt("EEE")),
                        dayNumber = d.format(fmt("d")),
                        selected = d == selectedDate
                    ) {
                        epochDay = d.toEpochDay()
                        onDateChange(d)
                    }
                }
            }

            // ---------- List ----------
            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.appointments_title),
                    color = MediColors.Text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = localizedNumber(appointments.size),
                    color = MediColors.Teal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MediColors.Pill)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            if (appointments.isEmpty()) {
                EmptyDay()
            }
            appointments.forEachIndexed { index, a ->
                if (index > 0) Spacer(Modifier.height(12.dp))
                DoctorAppointmentCard(
                    item = a,
                    onConfirm = { onConfirm(a) },
                    onReject = { onReject(a) },
                    onComplete = { onComplete(a) },
                    onPrescription = { onAppointmentClick(a) }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ScheduleCard(
    dayLabel: String,
    total: Int,
    pending: Int,
    confirmed: Int,
    done: Int,
    next: DoctorAppointment?
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF12B5AC), Color(0xFF0A8A83), Color(0xFF066B6B))
                )
            )
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.doc_schedule_for, dayLabel).uppercase(),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = localizedNumber(total),
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.appointments_title),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStat(stringResource(R.string.status_pending), pending, Modifier.weight(1f))
            MiniStat(stringResource(R.string.status_confirmed), confirmed, Modifier.weight(1f))
            MiniStat(stringResource(R.string.stat_done), done, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MediColors.Pill),
                contentAlignment = Alignment.Center
            ) {
                MediIcon(IconKind.Clock, tint = MediColors.Teal, iconSize = 20.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.doc_next_patient),
                    color = MediColors.Muted,
                    fontSize = 10.5.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (next != null) next.patientName else stringResource(R.string.doc_no_next),
                    color = MediColors.Text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (next != null) {
                Text(
                    next.time,
                    color = MediColors.Teal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: Int, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            localizedNumber(value),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun DayChip(weekday: String, dayNumber: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .width(58.dp)
            .clip(shape)
            .background(if (selected) MediColors.Teal else MediColors.Card)
            .border(1.dp, if (selected) MediColors.Teal else MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            weekday,
            color = if (selected) Color.White.copy(alpha = 0.9f) else MediColors.Muted,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            dayNumber,
            color = if (selected) Color.White else MediColors.Text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyDay() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MediColors.Pill),
            contentAlignment = Alignment.Center
        ) {
            MediIcon(IconKind.Calendar, tint = MediColors.Teal, iconSize = 30.dp)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.no_appointments_day),
            color = MediColors.Muted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DoctorAppointmentCard(
    item: DoctorAppointment,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    onPrescription: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val tint = PatientTints[((item.patientName.hashCode() % PatientTints.size) + PatientTints.size) % PatientTints.size]
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(tint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initialsOf(item.patientName),
                    color = MediColors.TealDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.patientName,
                    color = MediColors.Text,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MediIcon(IconKind.Clock, tint = MediColors.Muted, iconSize = 13.dp)
                    Spacer(Modifier.width(4.dp))
                    Text(item.time, color = MediColors.Muted, fontSize = 12.sp)
                }
            }
            DocBadge(item.status, item.token)
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = item.reason + " · " + item.department,
            color = MediColors.Muted,
            fontSize = 12.5.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        when (item.status) {
            "PENDING" -> {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton(
                        text = stringResource(R.string.reject_action),
                        fg = DocRedText,
                        bg = Color.Transparent,
                        borderColor = DocRedText.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f),
                        onClick = onReject
                    )
                    ActionButton(
                        text = stringResource(R.string.confirm_action),
                        fg = Color.White,
                        bg = MediColors.Teal,
                        borderColor = MediColors.Teal,
                        modifier = Modifier.weight(1f),
                        onClick = onConfirm
                    )
                }
            }
            "CONFIRMED" -> {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton(
                        text = stringResource(R.string.doc_open_rx),
                        fg = MediColors.TealDark,
                        bg = MediColors.Pill,
                        borderColor = MediColors.Pill,
                        modifier = Modifier.weight(1f),
                        onClick = onPrescription
                    )
                    ActionButton(
                        text = stringResource(R.string.mark_complete),
                        fg = Color.White,
                        bg = MediColors.Teal,
                        borderColor = MediColors.Teal,
                        modifier = Modifier.weight(1f),
                        onClick = onComplete
                    )
                }
            }
            "COMPLETED" -> {
                Spacer(Modifier.height(14.dp))
                ActionButton(
                    text = stringResource(R.string.doc_open_rx),
                    fg = MediColors.TealDark,
                    bg = MediColors.Pill,
                    borderColor = MediColors.Pill,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPrescription
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    fg: Color,
    bg: Color,
    borderColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = fg,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DocBadge(status: String, token: Int?) {
    val (bg, fg) = when (status) {
        "CONFIRMED" -> DocGreenBg to DocGreenText
        "COMPLETED" -> DocBlueBg to DocBlueText
        "PENDING" -> DocAmberBg to DocAmberText
        else -> DocRedBg to DocRedText   // cancelled / rejected
    }
    val label = stringResource(
        when (status) {
            "CONFIRMED" -> R.string.status_confirmed
            "COMPLETED" -> R.string.status_completed
            "CANCELLED" -> R.string.status_cancelled
            "REJECTED" -> R.string.status_rejected
            else -> R.string.status_pending
        }
    )
    val text = if (status == "CONFIRMED" && token != null) label + " · #" + localizedNumber(token) else label
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Text(text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DoctorDashboardPreview() {
    DoctorDashboardScreen(
        doctorName = "Dr. Ayesha Rahman",
        appointments = listOf(
            DoctorAppointment(1, "10:00 AM", "Nila Akter", "Follow-up", "Cardiology", "PENDING", null),
            DoctorAppointment(2, "10:30 AM", "Rafi Ahmed", "New consultation", "Cardiology", "CONFIRMED", 28)
        ),
        onDateChange = {}, onConfirm = {}, onReject = {}, onComplete = {}, onAvatarClick = {}
    )
}
