package com.mediqueue.app.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.BadgeGreenBg
import com.mediqueue.app.ui.components.BadgeGreenText
import com.mediqueue.app.ui.components.MediBadge
import com.mediqueue.app.ui.components.PatientBottomBar
import com.mediqueue.app.ui.components.PatientTab
import com.mediqueue.app.ui.components.StatusBadge
import com.mediqueue.app.ui.components.initialsOf
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val AvatarBg = Color(0xFF1D3530)
private val ProgressTrack = Color(0xFF2A4A44)

data class QueueInfo(
    val myToken: Int,
    val nowServing: Int,
    val patientsAhead: Int,
    val estimatedMinutes: Int,
    val progress: Float,
    val onTrack: Boolean
)

data class NextAppointment(
    val doctorName: String,
    val department: String,
    val whenText: String,
    val token: Int,
    val status: String
)

@Composable
fun PatientHomeScreen(
    patientName: String,
    queue: QueueInfo?,
    nextAppointment: NextAppointment?,
    onBookClick: () -> Unit,
    onAppointmentClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onTabSelected: (PatientTab) -> Unit,
    onPrescriptionsClick: () -> Unit = {}
) {
    val locale = LocalConfiguration.current.locales[0]
    val dateText = remember(locale) {
        LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE, d MMM", locale))
            .uppercase(locale)
    }
    val hour = remember { LocalTime.now().hour }
    val greetingRes = when {
        hour < 12 -> R.string.good_morning
        hour < 17 -> R.string.good_afternoon
        else -> R.string.good_evening
    }
    val firstName = patientName.trim().split(" ").firstOrNull().orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediColors.Bg)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = dateText,
                        color = MediColors.Muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(greetingRes, firstName),
                        color = MediColors.Text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(AvatarBg)
                        .clickable(onClick = onAvatarClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialsOf(patientName),
                        color = MediColors.Teal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Live queue card
            if (queue != null) {
                Spacer(Modifier.height(18.dp))
                LiveQueueCard(queue)
            }

            // Next appointment
            if (nextAppointment != null) {
                Spacer(Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.next_appointment),
                        color = MediColors.Text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    StatusBadge(status = nextAppointment.status)
                }
                Spacer(Modifier.height(12.dp))
                AppointmentCard(nextAppointment, onAppointmentClick)
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onBookClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediColors.Teal,
                    contentColor = MediColors.OnTeal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = stringResource(R.string.book_appointment), fontSize = 14.sp)
            }

            Spacer(Modifier.height(12.dp))
            PrescriptionsLink(onPrescriptionsClick)
        }

        PatientBottomBar(selected = PatientTab.Home, onSelect = onTabSelected)
    }
}

@Composable
private fun PrescriptionsLink(onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MediColors.Pill),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.rx_badge),
                    color = MediColors.Teal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.prescriptions_title),
                color = MediColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(text = "›", color = MediColors.Muted, fontSize = 16.sp)
    }
}

@Composable
private fun LiveQueueCard(queue: QueueInfo) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.live_queue),
                color = MediColors.Teal,
                fontSize = 10.5.sp,
                letterSpacing = 0.8.sp
            )
            if (queue.onTrack) {
                MediBadge(
                    text = stringResource(R.string.on_track),
                    bg = BadgeGreenBg,
                    fg = BadgeGreenText
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = stringResource(R.string.your_token),
                    color = MediColors.Muted,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "#" + localizedNumber(queue.myToken),
                    color = MediColors.Teal,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.now_serving),
                    color = MediColors.Muted,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "#" + localizedNumber(queue.nowServing),
                    color = MediColors.Text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(ProgressTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(queue.progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(MediColors.Teal)
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.patients_ahead, localizedNumber(queue.patientsAhead)),
                color = MediColors.Text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.wait_minutes, localizedNumber(queue.estimatedMinutes)),
                color = MediColors.Text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AppointmentCard(appointment: NextAppointment, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = appointment.doctorName,
                color = MediColors.Text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "›", color = MediColors.Muted, fontSize = 16.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "${appointment.department} · ${appointment.whenText}",
            color = MediColors.Muted,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MediColors.Pill)
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = stringResource(R.string.token_chip, localizedNumber(appointment.token)),
                    color = MediColors.Teal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = stringResource(R.string.view_details),
                color = MediColors.Muted,
                fontSize = 12.5.sp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PatientHomePreview() {
    PatientHomeScreen(
        patientName = "Nila Akter",
        queue = QueueInfo(28, 21, 7, 25, 0.68f, true),
        nextAppointment = NextAppointment(
            doctorName = "Dr. Ayesha Rahman",
            department = "Cardiology",
            whenText = "Today, 10:30 AM",
            token = 28,
            status = "CONFIRMED"
        ),
        onBookClick = {},
        onAppointmentClick = {},
        onAvatarClick = {},
        onTabSelected = {}
    )
}