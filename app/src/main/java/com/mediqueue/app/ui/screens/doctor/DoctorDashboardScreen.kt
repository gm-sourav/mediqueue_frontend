package com.mediqueue.app.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.DoctorBottomBar
import com.mediqueue.app.ui.components.DoctorTab
import com.mediqueue.app.ui.components.initialsOf
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle

private val DocIconBg = Color(0xFF1C2E2A)
private val DocStatBg = Color(0xFF1E322E)
private val DocGreenBg = Color(0xFF1D4034)
private val DocGreenText = Color(0xFF55D6A0)
private val DocRedBg = Color(0xFF4A2326)
private val DocRedText = Color(0xFFFF8A8A)
private val DocBlueBg = Color(0xFF1B3550)
private val DocBlueText = Color(0xFF7FB8F0)

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
            in 0..11 -> R.string.doc_good_morning
            in 12..16 -> R.string.doc_good_afternoon
            else -> R.string.doc_good_evening
        }
    )

    val pendingCount = appointments.count { it.status == "PENDING" }
    val confirmedCount = appointments.count { it.status == "CONFIRMED" }
    val doneCount = appointments.count { it.status == "COMPLETED" }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = selectedDate.format(fmt("EEEE, MMMM d")).uppercase(locale),
                        color = MediColors.Muted,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = greeting,
                        color = MediColors.Text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DocIconBg)
                        .clickable(onClick = onAvatarClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initialsOf(doctorName),
                        color = MediColors.Teal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Stats
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox(stringResource(R.string.status_pending), pendingCount, Modifier.weight(1f))
                StatBox(stringResource(R.string.status_confirmed), confirmedCount, Modifier.weight(1f))
                StatBox(stringResource(R.string.stat_done), doneCount, Modifier.weight(1f))
            }

            // Title + date dropdown
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
                var menuOpen by remember { mutableStateOf(false) }
                val todayWord = stringResource(R.string.today)
                val tomorrowWord = stringResource(R.string.tomorrow)
                fun labelOf(d: LocalDate) = when (d) {
                    today -> todayWord
                    today.plusDays(1) -> tomorrowWord
                    else -> d.format(fmt("EEE d"))
                }
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MediColors.Pill)
                            .clickable { menuOpen = true }
                            .padding(horizontal = 16.dp, vertical = 11.dp)
                    ) {
                        Text(
                            text = labelOf(selectedDate) + " ⌄",
                            color = MediColors.Teal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        containerColor = MediColors.Card
                    ) {
                        List(7) { today.plusDays(it.toLong()) }.forEach { d ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        labelOf(d),
                                        color = if (d == selectedDate) MediColors.Teal else MediColors.Text,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    menuOpen = false
                                    epochDay = d.toEpochDay()
                                    onDateChange(d)
                                }
                            )
                        }
                    }
                }
            }

            // List
            Spacer(Modifier.height(14.dp))
            if (appointments.isEmpty()) {
                Text(
                    stringResource(R.string.no_appointments_day),
                    color = MediColors.Muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp)
                )
            }
            appointments.forEachIndexed { index, a ->
                if (index > 0) Spacer(Modifier.height(14.dp))
                DoctorAppointmentCard(
                    item = a,
                    onConfirm = { onConfirm(a) },
                    onReject = { onReject(a) },
                    onComplete = { onComplete(a) },
                    onClick = { onAppointmentClick(a) }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatBox(label: String, value: Int, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DocStatBg)
            .padding(14.dp)
    ) {
        Text(
            label.uppercase(locale),
            color = MediColors.Muted,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )
        Spacer(Modifier.height(12.dp))
        Text(
            localizedNumber(value),
            color = MediColors.Text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DoctorAppointmentCard(
    item: DoctorAppointment,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable(
                enabled = item.status == "CONFIRMED" || item.status == "COMPLETED",
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.time + " · " + item.patientName,
                color = MediColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            DocBadge(item.status, item.token)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = item.reason + " · " + item.department,
            color = MediColors.Muted,
            fontSize = 13.sp
        )

        when (item.status) {
            "PENDING" -> {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlineAction(stringResource(R.string.reject_action), onReject)
                    OutlineAction(stringResource(R.string.confirm_action), onConfirm)
                }
            }
            "CONFIRMED" -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.mark_complete),
                    color = MediColors.Teal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable(onClick = onComplete)
                        .padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun OutlineAction(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .border(1.dp, MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Text(text, color = MediColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DocBadge(status: String, token: Int?) {
    val (bg, fg) = when (status) {
        "CONFIRMED" -> DocGreenBg to DocGreenText
        "COMPLETED" -> DocBlueBg to DocBlueText
        else -> DocRedBg to DocRedText   // pending / cancelled / rejected (pic: pending is red-ish)
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
            .padding(horizontal = 12.dp, vertical = 8.dp)
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
            DoctorAppointment(1, "10:00", "Nila Akter", "Follow-up", "Cardiology", "PENDING", null),
            DoctorAppointment(2, "10:30", "Rafi Ahmed", "New consultation", "Cardiology", "CONFIRMED", 28)
        ),
        onDateChange = {}, onConfirm = {}, onReject = {}, onComplete = {}, onAvatarClick = {}
    )
}