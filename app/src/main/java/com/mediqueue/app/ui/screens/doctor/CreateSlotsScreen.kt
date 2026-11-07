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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DecimalStyle

private val SlotsPillBg = Color(0xFF1C3A35)
private val SlotsPreviewBg = Color(0xFF1E322E)
private val lengthOptions = listOf(15, 20, 30, 45, 60)

@Composable
fun CreateSlotsScreen(
    onCreate: (date: LocalDate, start: LocalTime, end: LocalTime, lengthMin: Int, slots: List<LocalTime>) -> Unit,
    isSubmitting: Boolean = false
) {
    val context = LocalContext.current
    val resources = context.resources
    val locale = resources.configuration.locales[0]
    val today = remember { LocalDate.now() }

    var epochDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    var startMin by rememberSaveable { mutableIntStateOf(9 * 60) }
    var endMin by rememberSaveable { mutableIntStateOf(12 * 60) }
    var lengthMin by rememberSaveable { mutableIntStateOf(30) }
    var lengthMenu by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Availability pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SlotsPillBg)
                    .padding(horizontal = 14.dp, vertical = 13.dp)
            ) {
                Text(
                    stringResource(R.string.availability_label),
                    color = MediColors.Teal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                stringResource(R.string.create_slots_title),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.create_slots_subtitle),
                color = MediColors.Muted,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(16.dp))
            PickerField(
                label = stringResource(R.string.field_date),
                value = date.format(fmt("EEEE, MMMM d, yyyy")),
                modifier = Modifier.fillMaxWidth(),
                onClick = { pickDate() }
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PickerField(
                    label = stringResource(R.string.start_time),
                    value = LocalTime.of(startMin / 60, startMin % 60).format(fmt("hh:mm a")),
                    modifier = Modifier.weight(1f),
                    onClick = { pickTime(startMin) { startMin = it } }
                )
                PickerField(
                    label = stringResource(R.string.end_time),
                    value = LocalTime.of(endMin / 60, endMin % 60).format(fmt("hh:mm a")),
                    modifier = Modifier.weight(1f),
                    onClick = { pickTime(endMin) { endMin = it } }
                )
            }

            Spacer(Modifier.height(10.dp))
            Box {
                PickerField(
                    label = stringResource(R.string.slot_length),
                    value = stringResource(R.string.minutes_format, localizedNumber(lengthMin)),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { lengthMenu = true }
                )
                DropdownMenu(
                    expanded = lengthMenu,
                    onDismissRequest = { lengthMenu = false },
                    containerColor = MediColors.Card
                ) {
                    lengthOptions.forEach { len ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.minutes_format, localizedNumber(len)),
                                    color = if (len == lengthMin) MediColors.Teal else MediColors.Text,
                                    fontSize = 13.sp
                                )
                            },
                            onClick = {
                                lengthMin = len
                                lengthMenu = false
                            }
                        )
                    }
                }
            }

            // Preview
            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SlotsPreviewBg)
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        resources.getQuantityString(R.plurals.slots_count, slots.size, localizedNumber(slots.size)),
                        color = MediColors.Muted,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                slots.chunked(3).forEachIndexed { rowIndex, rowSlots ->
                    if (rowIndex > 0) Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (i in 0 until 3) {
                            val t = rowSlots.getOrNull(i)
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = when (i) {
                                    0 -> Alignment.CenterStart
                                    1 -> Alignment.Center
                                    else -> Alignment.CenterEnd
                                }
                            ) {
                                if (t != null) {
                                    Text(
                                        t.format(fmt("h:mm")),
                                        color = MediColors.Text,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Create
            Spacer(Modifier.height(16.dp))
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
                    Text(
                        text = resources.getQuantityString(
                            R.plurals.create_slots_button, slots.size, localizedNumber(slots.size)
                        ),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PickerField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(15.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 14.dp, top = 15.dp, bottom = 16.dp)
    ) {
        Text(label, color = MediColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Text(value, color = MediColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CreateSlotsPreview() {
    CreateSlotsScreen(onCreate = { _, _, _, _, _ -> })
}