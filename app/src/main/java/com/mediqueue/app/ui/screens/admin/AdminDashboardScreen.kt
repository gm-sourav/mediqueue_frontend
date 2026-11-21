package com.mediqueue.app.ui.screens.admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.mediqueue.app.R
import com.mediqueue.app.data.AdminStatsDto
import com.mediqueue.app.data.AppointmentDto
import com.mediqueue.app.ui.screens.patient.formatTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.mediqueue.app.ui.components.AdminBottomBar
import com.mediqueue.app.ui.components.AdminTab
import com.mediqueue.app.ui.components.StatusBadge
import com.mediqueue.app.ui.components.IconKind
import com.mediqueue.app.ui.components.MediIcon
import com.mediqueue.app.ui.components.UserAvatar
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors

@Composable
fun AdminDashboardScreen(
    adminName: String,
    stats: AdminStatsDto?,
    onAddDoctor: () -> Unit,
    onAddDepartment: () -> Unit,
    onAvatarClick: () -> Unit,
    onTabSelected: (AdminTab) -> Unit
) {
    Scaffold(
        containerColor = MediColors.Bg,
        bottomBar = { AdminBottomBar(selected = AdminTab.Home, onSelect = onTabSelected) }
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
                        stringResource(R.string.admin_panel).uppercase(),
                        color = MediColors.Muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.clinic_management),
                        color = MediColors.Text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                UserAvatar(
                    name = adminName,
                    avatarSize = 46.dp,
                    modifier = Modifier.clickable(onClick = onAvatarClick)
                )
            }

            // ---------- Today ----------
            Spacer(Modifier.height(18.dp))
            TodayCard(stats)

            // ---------- Totals ----------
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TotalCard(
                    icon = IconKind.Stethoscope,
                    value = stats?.doctors,
                    label = stringResource(R.string.admin_stat_doctors),
                    tileColor = MediColors.Pill,
                    iconColor = MediColors.Teal,
                    modifier = Modifier.weight(1f)
                )
                TotalCard(
                    icon = IconKind.Users,
                    value = stats?.patients,
                    label = stringResource(R.string.admin_stat_patients),
                    tileColor = Color(0xFFEBE4FA),
                    iconColor = Color(0xFF7A4FC9),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TotalCard(
                    icon = IconKind.Building,
                    value = stats?.departments,
                    label = stringResource(R.string.admin_stat_departments),
                    tileColor = Color(0xFFDCE9FB),
                    iconColor = MediColors.Accent,
                    modifier = Modifier.weight(1f)
                )
                TotalCard(
                    icon = IconKind.Calendar,
                    value = stats?.appointmentsTotal,
                    label = stringResource(R.string.admin_stat_appointments),
                    tileColor = Color(0xFFFDE9D9),
                    iconColor = Color(0xFFD9822B),
                    modifier = Modifier.weight(1f)
                )
            }

            // ---------- Quick actions ----------
            Spacer(Modifier.height(22.dp))
            Text(
                stringResource(R.string.quick_actions),
                color = MediColors.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionTile(
                    icon = IconKind.Plus,
                    label = stringResource(R.string.add_doctor),
                    modifier = Modifier.weight(1f),
                    onClick = onAddDoctor
                )
                ActionTile(
                    icon = IconKind.Plus,
                    label = stringResource(R.string.add_department),
                    modifier = Modifier.weight(1f),
                    onClick = onAddDepartment
                )
            }

            // ---------- Recent appointments ----------
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.admin_recent),
                color = MediColors.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            val recent = stats?.recent.orEmpty()
            if (recent.isEmpty()) {
                Text(
                    stringResource(R.string.admin_no_recent),
                    color = MediColors.Muted,
                    fontSize = 13.sp
                )
            } else {
                recent.forEachIndexed { i, a ->
                    if (i > 0) Spacer(Modifier.height(10.dp))
                    RecentRow(a)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TodayCard(stats: AdminStatsDto?) {
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
            stringResource(R.string.admin_today_appts),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stats?.let { localizedNumber(it.appointmentsToday.toInt()) } ?: "–",
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStat(stringResource(R.string.status_pending), stats?.pendingToday, Modifier.weight(1f))
            MiniStat(stringResource(R.string.status_confirmed), stats?.confirmedToday, Modifier.weight(1f))
            MiniStat(stringResource(R.string.status_completed), stats?.completedToday, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniStat(label: String, value: Long?, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = value?.let { localizedNumber(it.toInt()) } ?: "–",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun TotalCard(
    icon: IconKind,
    value: Long?,
    label: String,
    tileColor: Color,
    iconColor: Color,
    modifier: Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(tileColor),
            contentAlignment = Alignment.Center
        ) {
            MediIcon(icon, tint = iconColor, iconSize = 22.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = value?.let { localizedNumber(it.toInt()) } ?: "–",
                color = MediColors.Text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(label, color = MediColors.Muted, fontSize = 11.5.sp, maxLines = 1)
        }
    }
}

@Composable
private fun ActionTile(icon: IconKind, label: String, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MediColors.Pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MediIcon(icon, tint = MediColors.Teal, iconSize = 20.dp, strokeWidth = 2.2f)
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            color = MediColors.TealDark,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
    }
}

@Composable
private fun RecentRow(a: AppointmentDto) {
    val locale = LocalConfiguration.current.locales[0]
    val whenText = runCatching {
        LocalDate.parse(a.slotDate).format(DateTimeFormatter.ofPattern("MMM d", locale))
    }.getOrDefault(a.slotDate) + " · " + formatTime(a.startTime, locale)
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                a.patientName,
                color = MediColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                a.doctorName + " · " + a.departmentName,
                color = MediColors.Muted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(whenText, color = MediColors.Muted, fontSize = 11.5.sp)
        }
        Spacer(Modifier.width(10.dp))
        StatusBadge(status = a.status)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AdminDashboardPreview() {
    AdminDashboardScreen(
        adminName = "MediQueue Admin",
        stats = AdminStatsDto(
            doctors = 4, departments = 6, patients = 12, appointmentsTotal = 31,
            appointmentsToday = 5, pendingToday = 2, confirmedToday = 2, completedToday = 1,
            recent = listOf(
                AppointmentDto(
                    1, 1, "Nila Akter", 1, "Dr. Karim Ahmed", "Cardiology",
                    "2026-11-22", "10:30:00", "10:45:00", "PENDING", null, null
                )
            )
        ),
        onAddDoctor = {},
        onAddDepartment = {},
        onAvatarClick = {},
        onTabSelected = {}
    )
}
