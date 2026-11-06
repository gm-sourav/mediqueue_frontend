package com.mediqueue.app.ui.screens.patient

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay

private val ProgressCardBg = Color(0xFF1E322E)
private val ProgressTrack = Color(0xFF2A3A36)

data class LiveQueueInfo(
    val department: String,
    val doctorName: String,
    val timeText: String,      // "10:30 AM"
    val myToken: Int,
    val nowServing: Int,
    val waiting: Int,
    val progress: Float,       // 0f..1f
    val minutesRemaining: Int
)

@Composable
fun LiveQueueScreen(
    info: LiveQueueInfo,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    refreshIntervalMs: Long = 10_000L
) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val resources = LocalContext.current.resources

    // Auto refresh
    val latestRefresh by rememberUpdatedState(onRefresh)
    LaunchedEffect(Unit) {
        while (true) {
            delay(refreshIntervalMs)
            latestRefresh()
        }
    }

    // "Updated X sec ago" counter, resets when new data arrives
    var secondsAgo by remember(info) { mutableIntStateOf(0) }
    LaunchedEffect(info) {
        while (true) {
            delay(1000)
            secondsAgo++
        }
    }

    Scaffold(containerColor = MediColors.Bg) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "‹ " + stringResource(R.string.back_label),
                    color = MediColors.Text,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(vertical = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MediColors.Pill)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        stringResource(R.string.live_badge),
                        color = MediColors.Teal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = (info.department + " · " + stringResource(R.string.today)).uppercase(locale),
                color = MediColors.Muted,
                fontSize = 11.sp,
                letterSpacing = 0.6.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.your_queue),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = info.doctorName + " · " + info.timeText,
                color = MediColors.Muted,
                fontSize = 12.sp
            )

            // Token card
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MediColors.Teal)
                    .padding(vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.your_token),
                    color = MediColors.OnTeal,
                    fontSize = 11.sp,
                    letterSpacing = 0.6.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "#" + localizedNumber(info.myToken),
                    color = MediColors.OnTeal,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    stringResource(R.string.keep_handy),
                    color = MediColors.OnTeal,
                    fontSize = 12.sp
                )
            }

            // Stats
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = stringResource(R.string.now_serving),
                    value = "#" + localizedNumber(info.nowServing),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.waiting_label),
                    value = resources.getQuantityString(
                        R.plurals.people_count,
                        info.waiting,
                        localizedNumber(info.waiting)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // Progress
            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ProgressCardBg)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.queue_progress),
                        color = MediColors.Text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (secondsAgo < 5) stringResource(R.string.updated_just_now)
                        else stringResource(R.string.updated_seconds_ago, localizedNumber(secondsAgo)),
                        color = MediColors.Muted,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ProgressTrack)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(info.progress.coerceIn(0f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MediColors.Teal)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.queue_minutes_remaining, localizedNumber(info.minutesRemaining)),
                    color = MediColors.Muted,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.refreshes_auto),
                color = MediColors.Muted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .padding(14.dp)
    ) {
        Text(label, color = MediColors.Muted, fontSize = 11.sp, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(10.dp))
        Text(value, color = MediColors.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LiveQueuePreview() {
    LiveQueueScreen(
        info = LiveQueueInfo("Cardiology", "Dr. Ayesha Rahman", "10:30 AM", 28, 21, 7, 0.68f, 25),
        onBack = {},
        onRefresh = {}
    )
}