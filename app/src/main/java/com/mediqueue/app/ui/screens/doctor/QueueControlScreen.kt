package com.mediqueue.app.ui.screens.doctor
import com.mediqueue.app.ui.components.DoctorBottomBar
import com.mediqueue.app.ui.components.DoctorTab
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import com.mediqueue.app.ui.components.IconKind
import com.mediqueue.app.ui.components.MediIcon
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle

private val QcLiveBg = Color(0xFFDDF3E7)
private val QcLiveText = Color(0xFF1B8A57)

data class QueueEntry(
    val token: Int,
    val patientName: String,
    val time: String          // "10:15"
)

data class QueueControlInfo(
    val department: String,
    val nowServingToken: Int?,       // null = nobody called yet
    val nowServingName: String?,
    val nowServingTime: String?,
    val waiting: List<QueueEntry>
)

@Composable
fun QueueControlScreen(
    info: QueueControlInfo,
    onCallNext: () -> Unit,
    date: LocalDate = LocalDate.now(),
    isCalling: Boolean = false,
    onTabSelected: (DoctorTab) -> Unit = {}
) {
    val resources = LocalContext.current.resources
    val locale = resources.configuration.locales[0]
    val dateText = date.format(
        DateTimeFormatter.ofPattern("EEEE, MMM d", locale).withDecimalStyle(DecimalStyle.of(locale))
    )
    val next = info.waiting.firstOrNull()

    Scaffold(
        containerColor = MediColors.Bg,
        bottomBar = { DoctorBottomBar(DoctorTab.Queue, onTabSelected) }
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
                        stringResource(R.string.queue_control_badge),
                        color = MediColors.Muted,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.todays_queue),
                        color = MediColors.Text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = info.department + " · " + dateText,
                        color = MediColors.Muted,
                        fontSize = 12.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(QcLiveBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(QcLiveText)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.live_status),
                        color = QcLiveText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ---------- Now serving ----------
            Spacer(Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF12B5AC), Color(0xFF0A8A83), Color(0xFF066B6B))
                        )
                    )
                    .padding(vertical = 26.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.now_serving),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.5.sp,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (info.nowServingToken != null) "#" + localizedNumber(info.nowServingToken) else "—",
                            color = MediColors.TealDark,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (info.nowServingName != null) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = info.nowServingName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (info.nowServingTime != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = info.nowServingTime,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ---------- Stats ----------
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QcStat(
                    icon = IconKind.Users,
                    label = stringResource(R.string.waiting_label),
                    value = resources.getQuantityString(
                        R.plurals.patients_count, info.waiting.size, localizedNumber(info.waiting.size)
                    ),
                    modifier = Modifier.weight(1f)
                )
                QcStat(
                    icon = IconKind.Clock,
                    label = stringResource(R.string.next_token),
                    value = if (next != null) "#" + localizedNumber(next.token) else "—",
                    modifier = Modifier.weight(1f)
                )
            }

            // ---------- Call next ----------
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onCallNext,
                enabled = next != null && !isCalling,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediColors.Teal,
                    contentColor = MediColors.OnTeal,
                    disabledContainerColor = MediColors.Teal.copy(alpha = 0.35f),
                    disabledContentColor = MediColors.OnTeal.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isCalling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MediColors.OnTeal
                    )
                } else {
                    Text(
                        stringResource(R.string.call_next),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ---------- Waiting list ----------
            Spacer(Modifier.height(22.dp))
            Text(
                stringResource(R.string.waiting_next),
                color = MediColors.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            if (info.waiting.isEmpty()) {
                Text(
                    stringResource(R.string.nobody_waiting),
                    color = MediColors.Muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
            info.waiting.forEachIndexed { index, e ->
                if (index > 0) Spacer(Modifier.height(10.dp))
                WaitingRow(e, isNext = index == 0)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun WaitingRow(e: QueueEntry, isNext: Boolean) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, if (isNext) MediColors.Teal else MediColors.Border, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isNext) MediColors.Teal else MediColors.Pill),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "#" + localizedNumber(e.token),
                color = if (isNext) Color.White else MediColors.TealDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                e.patientName,
                color = MediColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(e.time, color = MediColors.Muted, fontSize = 12.sp)
        }
        if (isNext) {
            Text(
                stringResource(R.string.doc_up_next),
                color = MediColors.TealDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MediColors.Pill)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun QcStat(icon: IconKind, label: String, value: String, modifier: Modifier = Modifier) {
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
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MediColors.Pill),
            contentAlignment = Alignment.Center
        ) {
            MediIcon(icon, tint = MediColors.Teal, iconSize = 20.dp)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = MediColors.Muted, fontSize = 10.5.sp, letterSpacing = 0.4.sp, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(value, color = MediColors.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun QueueControlPreview() {
    QueueControlScreen(
        info = QueueControlInfo(
            "Cardiology", 21, "Nila Akter", "10:00 AM",
            listOf(
                QueueEntry(22, "Rafi Ahmed", "10:15"),
                QueueEntry(23, "Tania Noor", "10:30")
            )
        ),
        onCallNext = {}
    )
}