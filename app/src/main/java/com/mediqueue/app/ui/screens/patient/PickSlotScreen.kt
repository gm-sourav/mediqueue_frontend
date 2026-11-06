package com.mediqueue.app.ui.screens.patient

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
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

private val SlotAvailableBg = Color(0xFF1D3530)
private val SlotBookedBg = Color(0xFF2A3330)

data class SlotItem(
    val id: Long,
    val time: String,
    val booked: Boolean
)

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

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val selectFirstMsg = stringResource(R.string.select_slot_first)
    val todayWord = stringResource(R.string.today)

    fun dayFormatter(pattern: String) =
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
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.choose_time),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Date chips
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { day ->
                    val selected = day == selectedDate
                    val label = if (day == today) {
                        todayWord + " " + day.format(dayFormatter("d"))
                    } else {
                        day.format(dayFormatter("EEE d"))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) MediColors.Teal else MediColors.Pill)
                            .clickable {
                                selectedEpochDay = day.toEpochDay()
                                selectedSlotId = null
                            }
                            .padding(horizontal = 16.dp, vertical = 11.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (selected) MediColors.OnTeal else MediColors.Teal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Selected date label
            Spacer(Modifier.height(18.dp))
            Text(
                text = selectedDate.format(dayFormatter("EEEE, MMMM d")),
                color = MediColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            // Slot grid (3 column)
            Spacer(Modifier.height(12.dp))
            slots.chunked(3).forEachIndexed { rowIndex, rowSlots ->
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

            // Reason
            Spacer(Modifier.height(16.dp))
            MediFieldCard(label = stringResource(R.string.reason_for_visit)) {
                MediInput(
                    value = reason,
                    onValueChange = { reason = it },
                    hint = stringResource(R.string.reason_hint),
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                )
            }

            // Confirm
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val slot = slots.firstOrNull { it.id == selectedSlotId && !it.booked }
                    if (slot == null) {
                        scope.launch { snackbar.showSnackbar(selectFirstMsg) }
                    } else {
                        onConfirm(selectedDate, slot, reason.trim())
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediColors.Teal,
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

            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.slot_taken_note),
                color = MediColors.Muted,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
                SlotItem(1, "9:00 AM", false), SlotItem(2, "9:30 AM", true), SlotItem(3, "10:00 AM", false),
                SlotItem(4, "10:30 AM", false), SlotItem(5, "11:00 AM", true), SlotItem(6, "11:30 AM", false),
                SlotItem(7, "12:00 PM", false), SlotItem(8, "12:30 PM", false), SlotItem(9, "1:00 PM", false)
            )
        },
        onConfirm = { _, _, _ -> }
    )
}