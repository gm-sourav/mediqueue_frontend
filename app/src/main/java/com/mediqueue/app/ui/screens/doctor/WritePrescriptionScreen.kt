package com.mediqueue.app.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.MediFieldCard
import com.mediqueue.app.ui.components.MediInput
import com.mediqueue.app.ui.theme.MediColors
import kotlinx.coroutines.launch

private val WpInfoBg = Color(0xFF1E322E)
private val WpGreenBg = Color(0xFF1D4034)
private val WpGreenText = Color(0xFF55D6A0)

data class MedicineEntry(
    val id: Long,
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = ""
)

@Composable
fun WritePrescriptionScreen(
    patientName: String,
    department: String,
    time: String,                 // "10:00 AM"
    status: String,               // CONFIRMED / COMPLETED
    onBack: () -> Unit,
    onSave: (diagnosis: String, medicines: List<MedicineEntry>, notes: String) -> Unit,
    isSubmitting: Boolean = false
) {
    var nextId by rememberSaveable { mutableLongStateOf(2L) }
    val entries = remember { mutableStateListOf(MedicineEntry(1L)) }
    var notes by rememberSaveable { mutableStateOf("") }
    var diagnosis by rememberSaveable { mutableStateOf("") }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val needMedicineMsg = stringResource(R.string.add_medicine_first)
    val needDiagnosisMsg = stringResource(R.string.add_diagnosis_first)

    fun update(id: Long, change: (MedicineEntry) -> MedicineEntry) {
        val i = entries.indexOfFirst { it.id == id }
        if (i >= 0) entries[i] = change(entries[i])
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
            Text(
                text = "‹ " + stringResource(R.string.appointment_back),
                color = MediColors.Muted,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.write_prescription_title),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Patient card
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(WpInfoBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        patientName,
                        color = MediColors.Text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = department + " · " + stringResource(R.string.today) + ", " + time,
                        color = MediColors.Muted,
                        fontSize = 12.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(WpGreenBg)
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Text(
                        stringResource(
                            if (status == "COMPLETED") R.string.status_completed else R.string.status_confirmed
                        ),
                        color = WpGreenText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Diagnosis (backend e required)
            Spacer(Modifier.height(20.dp))
            MediFieldCard(label = stringResource(R.string.diagnosis_label)) {
                MediInput(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    hint = stringResource(R.string.diagnosis_hint),
                    imeAction = ImeAction.Next
                )
            }

            // Medicines header
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.medicines_title),
                    color = MediColors.Text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.add_action),
                    color = MediColors.Teal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            entries.add(MedicineEntry(nextId))
                            nextId++
                        }
                        .padding(vertical = 6.dp)
                )
            }

            // Medicine blocks
            entries.forEach { e ->
                Spacer(Modifier.height(12.dp))
                MedicineBlock(
                    entry = e,
                    canRemove = entries.size > 1,
                    onChange = { change -> update(e.id, change) },
                    onRemove = { entries.removeAll { it.id == e.id } }
                )
            }

            // Notes
            Spacer(Modifier.height(14.dp))
            MediFieldCard(
                label = stringResource(R.string.notes_for_patient),
                modifier = Modifier.heightIn(min = 92.dp)
            ) {
                MediInput(
                    value = notes,
                    onValueChange = { notes = it },
                    hint = stringResource(R.string.notes_for_patient_hint),
                    imeAction = ImeAction.Done
                )
            }

            // Save
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    val filled = entries.filter { it.name.isNotBlank() }
                    if (diagnosis.isBlank()) {
                        scope.launch { snackbar.showSnackbar(needDiagnosisMsg) }
                    } else if (filled.isEmpty()) {
                        scope.launch { snackbar.showSnackbar(needMedicineMsg) }
                    } else {
                        onSave(diagnosis.trim(), filled.toList(), notes.trim())
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
                    Text(stringResource(R.string.save_prescription), fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MedicineBlock(
    entry: MedicineEntry,
    canRemove: Boolean,
    onChange: ((MedicineEntry) -> MedicineEntry) -> Unit,
    onRemove: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .padding(14.dp)
    ) {
        // Name + remove
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (entry.name.isEmpty()) {
                    Text(
                        stringResource(R.string.medicine_name),
                        color = MediColors.Text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                BasicTextField(
                    value = entry.name,
                    onValueChange = { v -> onChange { it.copy(name = v) } },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MediColors.Text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(MediColors.Teal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (canRemove) {
                Text(
                    "×",
                    color = MediColors.Text,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable(onClick = onRemove)
                        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MediFieldCard(label = stringResource(R.string.rx_dosage), modifier = Modifier.weight(1f)) {
                MediInput(
                    value = entry.dosage,
                    onValueChange = { v -> onChange { it.copy(dosage = v) } },
                    hint = stringResource(R.string.rx_dosage_hint)
                )
            }
            MediFieldCard(label = stringResource(R.string.rx_frequency), modifier = Modifier.weight(1f)) {
                MediInput(
                    value = entry.frequency,
                    onValueChange = { v -> onChange { it.copy(frequency = v) } },
                    hint = stringResource(R.string.rx_frequency_hint)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        MediFieldCard(label = stringResource(R.string.rx_duration), modifier = Modifier.fillMaxWidth()) {
            MediInput(
                value = entry.duration,
                onValueChange = { v -> onChange { it.copy(duration = v) } },
                hint = stringResource(R.string.rx_duration_hint)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WritePrescriptionPreview() {
    WritePrescriptionScreen(
        patientName = "Nila Akter",
        department = "Cardiology",
        time = "10:00 AM",
        status = "COMPLETED",
        onBack = {},
        onSave = { _, _, _ -> }
    )
}