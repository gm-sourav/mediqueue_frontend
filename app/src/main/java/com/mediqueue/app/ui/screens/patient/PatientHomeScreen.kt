package com.mediqueue.app.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.BadgeGreenBg
import com.mediqueue.app.ui.components.BadgeGreenText
import com.mediqueue.app.ui.components.DepartmentIcon
import com.mediqueue.app.ui.components.DoctorAvatar
import com.mediqueue.app.ui.components.IconKind
import com.mediqueue.app.ui.components.MediBadge
import com.mediqueue.app.ui.components.MediIcon
import com.mediqueue.app.ui.components.PatientBottomBar
import com.mediqueue.app.ui.components.PatientTab
import com.mediqueue.app.ui.components.StatusBadge
import com.mediqueue.app.ui.components.initialsOf
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val ProgressTrack = Color(0xFFD3E6E4)

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
    onPrescriptionsClick: () -> Unit = {},
    departments: List<Department> = emptyList(),
    onDepartmentClick: (Department) -> Unit = {}
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
            // ---------- Header ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(MediColors.TealLight, MediColors.TealDark)))
                        .clickable(onClick = onAvatarClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialsOf(patientName),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ---------- Hero ----------
            Spacer(Modifier.height(18.dp))
            HeroCard(onBookClick)

            // ---------- Live queue ----------
            if (queue != null) {
                Spacer(Modifier.height(18.dp))
                LiveQueueCard(queue)
            }

            // ---------- Departments ----------
            if (departments.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.departments_title),
                        color = MediColors.Text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.see_all),
                        color = MediColors.Teal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onBookClick)
                            .padding(4.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    departments.forEach { dept ->
                        Column(
                            modifier = Modifier
                                .width(78.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onDepartmentClick(dept) }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DepartmentIcon(icon = dept.icon, boxSize = 60.dp, corner = 18.dp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = dept.name,
                                color = MediColors.Text,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // ---------- Next appointment ----------
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
                if (nextAppointment != null) StatusBadge(status = nextAppointment.status)
            }
            Spacer(Modifier.height(12.dp))
            if (nextAppointment != null) {
                AppointmentCard(nextAppointment, onAppointmentClick)
            } else {
                EmptyAppointmentCard(onBookClick)
            }

            Spacer(Modifier.height(18.dp))
            PrescriptionsLink(onPrescriptionsClick)
            Spacer(Modifier.height(8.dp))
        }

        PatientBottomBar(selected = PatientTab.Home, onSelect = onTabSelected)
    }
}

@Composable
private fun HeroCard(onBookClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF12B5AC), Color(0xFF0A8A83), Color(0xFF066B6B))
                )
            )
            .padding(start = 20.dp, top = 20.dp, bottom = 20.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_hero_title),
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 25.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_hero_sub),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .clickable(onClick = onBookClick)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediIcon(IconKind.Calendar, tint = MediColors.Teal, iconSize = 17.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.book_appointment),
                    color = MediColors.Teal,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            DoctorAvatar(name = "Dr. Care", avatarSize = 104.dp, corner = 52.dp)
        }
    }
}

@Composable
private fun PrescriptionsLink(onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEFE8FB)),
            contentAlignment = Alignment.Center
        ) {
            MediIcon(IconKind.Pill, tint = Color(0xFF7A4FC9), iconSize = 22.dp)
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = stringResource(R.string.prescriptions_title),
            color = MediColors.Text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        MediIcon(IconKind.ChevronRight, tint = MediColors.Muted, iconSize = 20.dp)
    }
}

@Composable
private fun LiveQueueCard(queue: QueueInfo) {
    val shape = RoundedCornerShape(20.dp)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MediColors.Pill),
                    contentAlignment = Alignment.Center
                ) {
                    MediIcon(IconKind.Users, tint = MediColors.Teal, iconSize = 17.dp)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.live_queue),
                    color = MediColors.Teal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp
                )
            }
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
                    fontSize = 36.sp,
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
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(ProgressTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(queue.progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.horizontalGradient(listOf(MediColors.TealLight, MediColors.Teal)))
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.patients_ahead, localizedNumber(queue.patientsAhead)),
                color = MediColors.Text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                MediIcon(IconKind.Clock, tint = MediColors.Muted, iconSize = 15.dp)
                Spacer(Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.wait_minutes, localizedNumber(queue.estimatedMinutes)),
                    color = MediColors.Text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AppointmentCard(appointment: NextAppointment, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            DoctorAvatar(name = appointment.doctorName, avatarSize = 52.dp, corner = 16.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.doctorName,
                    color = MediColors.Text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = appointment.department,
                    color = MediColors.Muted,
                    fontSize = 12.sp
                )
            }
            MediIcon(IconKind.ChevronRight, tint = MediColors.Muted, iconSize = 20.dp)
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MediColors.Bg)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MediIcon(IconKind.Clock, tint = MediColors.Teal, iconSize = 16.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = appointment.whenText,
                    color = MediColors.Text,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(MediColors.Pill)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.token_chip, localizedNumber(appointment.token)),
                    color = MediColors.Teal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyAppointmentCard(onBookClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable(onClick = onBookClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MediColors.Pill),
            contentAlignment = Alignment.Center
        ) {
            MediIcon(IconKind.Calendar, tint = MediColors.Teal, iconSize = 22.dp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_no_appt),
                color = MediColors.Text,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_no_appt_sub),
                color = MediColors.Muted,
                fontSize = 12.sp,
                lineHeight = 17.sp
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
        onTabSelected = {},
        departments = listOf(
            Department(1, "Cardiology", 3, "heart"),
            Department(2, "Neurology", 2, "brain"),
            Department(3, "Dental", 4, "tooth")
        )
    )
}