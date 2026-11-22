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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.MediFieldCard
import com.mediqueue.app.ui.components.MediInput
import com.mediqueue.app.ui.theme.MediColors
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle

private val SlotAvailableBg = Color(0xFFE3F2F1)
private val SlotBookedBg = Color(0xFFE4EBEC)

/** hour = 0..23 of the slot start (used for Morning / Afternoon / Evening); -1 = unknown */
data class SlotItem(
    val id: Long,
    val time: String,
    val booked: Boolean,
    val hour: Int = -1
)

private enum class Period(val labelRes: Int) {
    Morning(R.string.period_morning),
    Afternoon(R.string.period_afternoon),
    Evening(R.string.period_evening)
}

private fun periodOf(hour: Int): Period = when {
    hour < 12 -> Period.Morning
    hour < 17 -> Period.Afternoon
    else -> Period.Evening
}

@Composable
fun PickSlotScreen(
    doctorName: String,
    slotsForDate: (LocalDate) -> List<SlotItem>,
    onConfirm: (date: LocalDate, slot: SlotItem, reason: String) -> Unit,
    isSubmitting: Boolean = false
) {
    val locale = LocalConfiguration.current.locales[0]
    val today = remember { LocalDate.now() }
    val days = remember { List(7) { today.plusDays(it.toLong()) } }

    var selectedEpochDay by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    var selectedSlotId by rememberSaveable { mutableStateOf<Long?>(null) }
    var reason by rememberSaveable { mutableStateOf("") }

    val selectedDate = LocalDate.ofEpochDay(selectedEpochDay)
    val slots = remember(selectedDate) { slotsForDate(selectedDate) }
    val freeCounts = remember { days.associateWith { d -> slotsForDate(d).count { !it.booked } } }
    val selectedSlot = slots.firstOrNull { it.id == selectedSlotId && !it.booked }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val selectFirstMsg = stringResource(R.string.select_slot_first)
    val todayWord = stringResource(R.string.today)

    fun fmt(pattern: String) =
        DateTimeFormatter.ofPattern(pattern, locale).withDecimalStyle(DecimalStyle.of(locale))

    Scaffold(
        containerColor = MediColors.Bg,
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MediColors.Card,
                    contentColor = MediColors.Text
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MediColors.Card)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (selectedSlot != null) {
                        selectedDate.format(fmt("EEE, MMM d")) + " · " + selectedSlot.time
                    } else {
                        stringResource(R.string.select_slot_first)
                    },
                    color = if (selectedSlot != null) MediColors.Text else MediColors.Muted,
                    fontSize = 13.sp,
                    fontWeight = if (selectedSlot != null) FontWeight.SemiBold else FontWeight.Normal
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (selectedSlot == null) {
                            scope.launch { snackbar.showSnackbar(selectFirstMsg) }
                        } else {
                            onConfirm(selectedDate, selectedSlot, reason.trim())
                        }
                    },
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSlot != null) MediColors.Teal else MediColors.Teal.copy(alpha = 0.45f),
                        contentColor = MediColors.OnTeal,
                        disabledContainerColor = MediColors.Teal.copy(alpha = 0.6f),
                        disabledContentColor = MediColors.OnTeal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MediColors.OnTeal
                        )
                    } else {
                        Text(text = stringResource(R.string.confirm_booking), fontSize = 14.sp)
                    }
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = doctorName.uppercase(locale),
                color = MediColors.Muted,
                fontSize = 11.sp,
                letterSpacing = 0.6.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.choose_time),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Step indicator: Date -> Time -> Confirm
            Spacer(Modifier.height(16.dp))
            StepIndicator(
                labels = listOf(
                    stringResource(R.string.step_date),
                    stringResource(R.string.step_time),
                    stringResource(R.string.step_confirm)
                ),
                current = if (selectedSlot == null) 1 else 2
            )

            // Date strip
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                days.forEach { day ->
                    DateCard(
                        weekday = if (day == today) todayWord else day.format(fmt("EEE")),
                        dayNumber = day.format(fmt("d")),
                        month = day.format(fmt("MMM")),
                        free = freeCounts[day] ?: 0,
                        selected = day == selectedDate,
                        onClick = {
                            selectedEpochDay = day.toEpochDay()
                            selectedSlotId = null
                        }
                    )
                }
            }

            // Slots grouped by part of day
            Spacer(Modifier.height(20.dp))
            Text(
                text = selectedDate.format(fmt("EEEE, MMMM d")),
                color = MediColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            if (slots.isEmpty() || slots.all { it.booked }) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_slots_day),
                    color = MediColors.Muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MediColors.Card)
                        .border(1.dp, MediColors.Border, RoundedCornerShape(14.dp))
                        .padding(vertical = 22.dp, horizontal = 16.dp)
                )
            } else {
                Period.values().forEach { period ->
                    val group = slots.filter { periodOf(it.hour) == period }
                    if (group.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(period.labelRes),
                                color = MediColors.Muted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.4.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "· " + group.count { !it.booked },
                                color = MediColors.Muted,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        group.chunked(3).forEachIndexed { rowIndex, rowSlots ->
                            if (rowIndex > 0) Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowSlots.forEach { slot ->
                                    SlotChip(
                                        slot = slot,
                                        selected = slot.id == selectedSlotId,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedSlotId = slot.id }
                                    )
                                }
                                repeat(3 - rowSlots.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Reason
            Spacer(Modifier.height(20.dp))
            MediFieldCard(label = stringResource(R.string.reason_for_visit)) {
                MediInput(
                    value = reason,
                    onValueChange = { reason = it },
                    hint = stringResource(R.string.reason_hint),
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                )
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.slot_taken_note),
                color = MediColors.Muted,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** current: 0-based index of the active step; earlier steps show a check mark */
@Composable
private fun StepIndicator(labels: List<String>, current: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { i, label ->
            val done = i < current
            val active = i == current
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (done || active) MediColors.Teal else MediColors.Pill),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (done) "✓" else (i + 1).toString(),
                    color = if (done || active) MediColors.OnTeal else MediColors.Teal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                color = if (active) MediColors.Text else MediColors.Muted,
                fontSize = 12.sp,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
            )
            if (i < labels.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(2.dp)
                        .background(if (i < current) MediColors.Teal else MediColors.Border)
                )
            }
        }
    }
}

@Composable
private fun DateCard(
    weekday: String,
    dayNumber: String,
    month: String,
    free: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val empty = free == 0
    Column(
        modifier = Modifier
            .width(64.dp)
            .clip(shape)
            .background(if (selected) MediColors.Teal else MediColors.Card)
            .border(1.dp, if (selected) MediColors.Teal else MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weekday,
            color = if (selected) MediColors.OnTeal else MediColors.Muted,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = dayNumber,
            color = if (selected) MediColors.OnTeal else if (empty) MediColors.Muted else MediColors.Text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = month,
            color = if (selected) MediColors.OnTeal else MediColors.Muted,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(
                    when {
                        empty -> Color.Transparent
                        selected -> MediColors.OnTeal
                        else -> MediColors.Teal
                    }
                )
        )
    }
}

@Composable
private fun SlotChip(
    slot: SlotItem,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = when {
        slot.booked -> SlotBookedBg
        selected -> MediColors.Teal
        else -> SlotAvailableBg
    }
    val fg = when {
        slot.booked -> MediColors.Muted
        selected -> MediColors.OnTeal
        else -> MediColors.Teal
    }
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .then(if (slot.booked) Modifier else Modifier.clickable(onClick = onClick)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = slot.time,
            color = fg,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = if (slot.booked) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PickSlotPreview() {
    PickSlotScreen(
        doctorName = "Dr. Ayesha Rahman",
        slotsForDate = {
            listOf(
                SlotItem(1, "9:00 AM", false, 9), SlotItem(2, "9:30 AM", true, 9), SlotItem(3, "10:00 AM", false, 10),
                SlotItem(4, "10:30 AM", false, 10), SlotItem(5, "11:00 AM", true, 11), SlotItem(6, "11:30 AM", false, 11),
                SlotItem(7, "12:00 PM", false, 12), SlotItem(8, "12:30 PM", false, 12), SlotItem(9, "1:00 PM", false, 13),
                SlotItem(10, "5:00 PM", false, 17), SlotItem(11, "5:30 PM", false, 17)
            )
        },
        onConfirm = { _, _, _ -> }
    )
}
