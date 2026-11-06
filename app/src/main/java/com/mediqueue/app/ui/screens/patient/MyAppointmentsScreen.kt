package com.mediqueue.app.ui.screens.patient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.PatientBottomBar
import com.mediqueue.app.ui.components.PatientTab
import com.mediqueue.app.ui.components.initialsOf
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors

private val ApptPendingBg = Color(0xFF1D3530)
private val ApptGreenBg = Color(0xFF1D4034)
private val ApptGreenText = Color(0xFF55D6A0)
private val ApptBlueBg = Color(0xFF1B3550)
private val ApptBlueText = Color(0xFF7FB8F0)
private val ApptRedBg = Color(0xFF4A2326)
private val ApptRedText = Color(0xFFFF8A8A)
private val IconBoxBg = Color(0xFF1C2E2A)

/** status: PENDING / CONFIRMED / COMPLETED / CANCELLED / REJECTED */
data class AppointmentItem(
    val id: Long,
    val status: String,
    val token: Int?,
    val doctorName: String,
    val department: String,
    val whenLabel: String,   // "Today", "Nov 5"
    val time: String,        // "10:30 AM"
    val date: String,        // "Nov 2"
    val fee: Int
)

@Composable
fun MyAppointmentsScreen(
    appointments: List<AppointmentItem>,
    onCancel: (AppointmentItem) -> Unit,
    onDetailsClick: (AppointmentItem) -> Unit,
    onTabSelected: (PatientTab) -> Unit
) {
    var showPast by rememberSaveable { mutableStateOf(false) }

    val shown = appointments.filter {
        val upcoming = it.status == "PENDING" || it.status == "CONFIRMED"
        if (showPast) !upcoming else upcoming
    }

    Scaffold(
        containerColor = MediColors.Bg,
        bottomBar = { PatientBottomBar(selected = PatientTab.Bookings, onSelect = onTabSelected) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.my_appointments),
                    color = MediColors.Text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IconBoxBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text("▤", color = MediColors.Muted, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TabPill(stringResource(R.string.tab_upcoming), !showPast) { showPast = false }
                TabPill(stringResource(R.string.tab_past), showPast) { showPast = true }
            }

            Spacer(Modifier.height(16.dp))
            if (shown.isEmpty()) {
                Text(
                    text = stringResource(if (showPast) R.string.no_past else R.string.no_upcoming),
                    color = MediColors.Muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(shown, key = { it.id }) { item ->
                        AppointmentCard(
                            item = item,
                            onCancel = { onCancel(item) },
                            onDetails = { onDetailsClick(item) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TabPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) MediColors.Teal else MediColors.Pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp)
    ) {
        Text(
            text = text,
            color = if (selected) MediColors.OnTeal else MediColors.Teal,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AppointmentCard(
    item: AppointmentItem,
    onCancel: () -> Unit,
    onDetails: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val pending = item.status == "PENDING"
    val canCancel = item.status == "CONFIRMED"
    val showActions = item.status != "PENDING"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ApptBadge(status = item.status, token = item.token)
            Text(item.whenLabel, color = MediColors.Muted, fontSize = 13.sp)
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MediColors.Pill),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(item.doctorName),
                    color = MediColors.Teal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    item.doctorName,
                    color = MediColors.Text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(item.department, color = MediColors.Muted, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.time + " · " +
                        if (pending) stringResource(R.string.awaiting_confirmation) else item.date,
                color = MediColors.Muted,
                fontSize = 13.sp
            )
            if (!pending) {
                Text(
                    text = stringResource(R.string.fee_compact, localizedNumber(item.fee)),
                    color = MediColors.Muted,
                    fontSize = 13.sp
                )
            }
        }

        if (showActions) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (canCancel) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, MediColors.Border), RoundedCornerShape(10.dp))
                            .clickable(onClick = onCancel)
                            .padding(horizontal = 14.dp, vertical = 11.dp)
                    ) {
                        Text(
                            stringResource(R.string.cancel_booking),
                            color = MediColors.Text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Text(
                    text = stringResource(R.string.details_action) + " ›",
                    color = MediColors.Teal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable(onClick = onDetails)
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ApptBadge(status: String, token: Int?) {
    val (bg, fg) = when (status) {
        "CONFIRMED" -> ApptGreenBg to ApptGreenText
        "COMPLETED" -> ApptBlueBg to ApptBlueText
        "CANCELLED", "REJECTED" -> ApptRedBg to ApptRedText
        else -> ApptPendingBg to MediColors.Teal
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
    val text = if (status == "CONFIRMED" && token != null) {
        label + " · #" + localizedNumber(token)
    } else label

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
private fun MyAppointmentsPreview() {
    MyAppointmentsScreen(
        appointments = listOf(
            AppointmentItem(1, "CONFIRMED", 28, "Dr. Ayesha Rahman", "Cardiology", "Today", "10:30 AM", "Nov 2", 1000),
            AppointmentItem(2, "PENDING", null, "Dr. Samiul Haque", "Neurology", "Nov 5", "9:00 AM", "Nov 5", 800)
        ),
        onCancel = {},
        onDetailsClick = {},
        onTabSelected = {}
    )
}