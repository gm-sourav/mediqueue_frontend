package com.mediqueue.app.ui.screens.doctor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.mediqueue.app.ui.components.DoctorBottomBar
import com.mediqueue.app.ui.components.DoctorTab
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle

private val lengthOptions = listOf(15, 20, 30, 45, 60)

@Composable
fun CreateSlotsScreen(
    onCreate: (date: LocalDate, start: LocalTime, end: LocalTime, lengthMin: Int, slots: List<LocalTime>) -> Unit,
    isSubmitting: Boolean = false,
    onTabSelected: (DoctorTab) -> Unit = {}
) {
    val context = LocalContext.current
    val resources = context.resources
    val locale = resources.configuration.locales[0]
    val today = remember { LocalDate.now() }

    var epochDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    var startMin by rememberSaveable { mutableIntStateOf(9 * 60) }
    var endMin by rememberSaveable { mutableIntStateOf(12 * 60) }
    var lengthMin by rememberSaveable { mutableIntStateOf(30) }

    val date = LocalDate.ofEpochDay(epochDay)

    fun fmt(pattern: String) =
        DateTimeFormatter.ofPattern(pattern, locale).withDecimalStyle(DecimalStyle.of(locale))

    // Slots preview
    val slots = remember(startMin, endMin, lengthMin) {
        val list = mutableListOf<LocalTime>()
        var t = startMin
        while (t + lengthMin <= endMin) {
            list.add(LocalTime.of(t / 60, t % 60))
            t += lengthMin
        }
        list.toList()
    }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val invalidMsg = stringResource(R.string.invalid_time_window)

    val dialogTheme = android.R.style.Theme_DeviceDefault_Dialog_Alert

    fun pickTime(current: Int, onPicked: (Int) -> Unit) {
        TimePickerDialog(
            context, dialogTheme,
            { _, h, m -> onPicked(h * 60 + m) },
            current / 60, current % 60, false
        ).show()
    }

    fun pickDate() {
        DatePickerDialog(
            context, dialogTheme,
            { _, y, m, d -> epochDay = LocalDate.of(y, m + 1, d).toEpochDay() },
            date.year, date.monthValue - 1, date.dayOfMonth
        ).apply {
            datePicker.minDate = today.atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        }.show()
    }

    Scaffold(
        containerColor = MediColors.Bg,
        bottomBar = { DoctorBottomBar(DoctorTab.Slots, onTabSelected) },
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(snackbarData = data, containerColor = MediColors.Card, contentColor = MediColors.Text)
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // ---------- Header ----------
            Text(
                stringResource(R.string.availability_label).uppercase(locale),
                color = MediColors.Muted,
                fontSize = 11.sp,
                letterSpacing = 0.6.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.create_slots_title),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.create_slots_subtitle),
                color = MediColors.Muted,
                fontSize = 13.sp
            )

            // ---------- Date ----------
            Spacer(Modifier.height(20.dp))
            SectionLabel(stringResource(R.string.field_date))
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val days = List(7) { today.plusDays(it.toLong()) }
                days.forEach { d ->
                    DayChip(
                        weekday = d.format(fmt("EEE")),
                        dayNumber = d.format(fmt("d")),
                        selected = d == date
                    ) { epochDay = d.toEpochDay() }
                }
                // Any other day
                val customSelected = date !in days
                Column(
                    modifier = Modifier
                        .width(58.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (customSelected) MediColors.Teal else MediColors.Pill)
                        .clickable { pickDate() }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MediIcon(
                        IconKind.Calendar,
                        tint = if (customSelected) Color.White else MediColors.Teal,
                        iconSize = 22.dp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (customSelected) date.format(fmt("d MMM")) else stringResource(R.string.pick_date),
                        color = if (customSelected) Color.White else MediColors.Teal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                date.format(fmt("EEEE, MMMM d, yyyy")),
                color = MediColors.Muted,
                fontSize = 12.sp
            )

            // ---------- Time window ----------
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeCard(
                    label = stringResource(R.string.start_time),
                    value = LocalTime.of(startMin / 60, startMin % 60).format(fmt("hh:mm a")),
                    modifier = Modifier.weight(1f),
                    onClick = { pickTime(startMin) { startMin = it } }
                )
                TimeCard(
                    label = stringResource(R.string.end_time),
                    value = LocalTime.of(endMin / 60, endMin % 60).format(fmt("hh:mm a")),
                    modifier = Modifier.weight(1f),
                    onClick = { pickTime(endMin) { endMin = it } }
                )
            }

            // ---------- Slot length ----------
            Spacer(Modifier.height(20.dp))
            SectionLabel(stringResource(R.string.slot_length))
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                lengthOptions.forEach { len ->
                    val on = len == lengthMin
                    val shape = RoundedCornerShape(50)
                    Box(
                        modifier = Modifier
                            .clip(shape)
                            .background(if (on) MediColors.Teal else MediColors.Card)
                            .border(1.dp, if (on) MediColors.Teal else MediColors.Border, shape)
                            .clickable { lengthMin = len }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            stringResource(R.string.minutes_format, localizedNumber(len)),
                            color = if (on) Color.White else MediColors.Text,
                            fontSize = 12.5.sp,
                            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            // ---------- Preview ----------
            Spacer(Modifier.height(22.dp))
            val cardShape = RoundedCornerShape(20.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(MediColors.Card)
                    .border(1.dp, MediColors.Border, cardShape)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.preview_label),
                        color = MediColors.Text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        resources.getQuantityString(R.plurals.slots_count, slots.size, localizedNumber(slots.size)),
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
                if (slots.isEmpty()) {
                    Text(invalidMsg, color = MediColors.Danger, fontSize = 12.5.sp)
                }
                slots.chunked(3).forEachIndexed { rowIndex, rowSlots ->
                    if (rowIndex > 0) Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in 0 until 3) {
                            val t = rowSlots.getOrNull(i)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (t != null) MediColors.Pill else Color.Transparent)
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (t != null) {
                                    Text(
                                        t.format(fmt("h:mm a")),
                                        color = MediColors.TealDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ---------- Create ----------
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                    if (slots.isEmpty()) {
                        scope.launch { snackbar.showSnackbar(invalidMsg) }
                    } else {
                        onCreate(
                            date,
                            LocalTime.of(startMin / 60, startMin % 60),
                            LocalTime.of(endMin / 60, endMin % 60),
                            lengthMin,
                            slots
                        )
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediColors.Teal,
                    contentColor = MediColors.OnTeal,
                    disabledContainerColor = MediColors.Teal.copy(alpha = 0.6f),
                    disabledContentColor = MediColors.OnTeal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MediColors.OnTeal
                    )
                } else {
                    Text(
                        text = resources.getQuantityString(
                            R.plurals.create_slots_button, slots.size, localizedNumber(slots.size)
                        ),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = MediColors.Text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
private fun TimeCard(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable(onClick = onClick)
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
            MediIcon(IconKind.Clock, tint = MediColors.Teal, iconSize = 20.dp)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = MediColors.Muted, fontSize = 11.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, color = MediColors.Text, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CreateSlotsPreview() {
    CreateSlotsScreen(onCreate = { _, _, _, _, _ -> })
}
