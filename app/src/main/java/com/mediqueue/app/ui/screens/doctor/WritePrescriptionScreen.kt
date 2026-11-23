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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import com.mediqueue.app.ui.components.IconKind
import com.mediqueue.app.ui.components.MediIcon
import com.mediqueue.app.ui.components.PatientAvatar
import com.mediqueue.app.ui.components.initialsOf
import com.mediqueue.app.ui.components.localizedNumber
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
import androidx.compose.ui.res.stringResource
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

private val WpInfoBg = Color(0xFFEAF5F4)
private val WpGreenBg = Color(0xFFDDF3E7)
private val WpGreenText = Color(0xFF1B8A57)

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
    patientGender: String? = null,
    patientPhotoUrl: String? = null,
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
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MediColors.Card)
                    .navigationBarsPadding()
            ) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(MediColors.Border))
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
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediColors.Teal,
                        contentColor = MediColors.OnTeal,
                        disabledContainerColor = MediColors.Teal.copy(alpha = 0.6f),
                        disabledContentColor = MediColors.OnTeal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(54.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MediColors.OnTeal
                        )
                    } else {
                        Text(
                            stringResource(R.string.save_prescription),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
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
            // ---------- Top bar ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MediColors.Card)
                        .border(1.dp, MediColors.Border, CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    MediIcon(IconKind.ChevronLeft, tint = MediColors.Text, iconSize = 20.dp)
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    stringResource(R.string.write_prescription_title),
                    color = MediColors.Text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ---------- Patient card ----------
            Spacer(Modifier.height(18.dp))
            val cardShape = RoundedCornerShape(20.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF12B5AC), Color(0xFF0A8A83), Color(0xFF066B6B))
                        )
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PatientAvatar(
                    name = patientName,
                    gender = patientGender,
                    photoUrl = patientPhotoUrl,
                    avatarSize = 48.dp
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        patientName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = department + " · " + time,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Text(
                    stringResource(
                        if (status == "COMPLETED") R.string.status_completed else R.string.status_confirmed
                    ),
                    color = WpGreenText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 11.dp, vertical = 6.dp)
                )
            }

            // ---------- Diagnosis ----------
            Spacer(Modifier.height(22.dp))
            MediFieldCard(label = stringResource(R.string.diagnosis_label)) {
                MediInput(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    hint = stringResource(R.string.diagnosis_hint),
                    imeAction = ImeAction.Next
                )
            }

            // ---------- Medicines ----------
            Spacer(Modifier.height(22.dp))
            Text(
                stringResource(R.string.medicines_title),
                color = MediColors.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            entries.forEachIndexed { index, e ->
                Spacer(Modifier.height(12.dp))
                MedicineBlock(
                    number = index + 1,
                    entry = e,
                    canRemove = entries.size > 1,
                    onChange = { change -> update(e.id, change) },
                    onRemove = { entries.removeAll { it.id == e.id } }
                )
            }

            Spacer(Modifier.height(12.dp))
            val addShape = RoundedCornerShape(16.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(addShape)
                    .background(MediColors.Pill)
                    .clickable {
                        entries.add(MedicineEntry(nextId))
                        nextId++
                    }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                MediIcon(IconKind.Plus, tint = MediColors.Teal, iconSize = 18.dp, strokeWidth = 2.2f)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.add_medicine),
                    color = MediColors.TealDark,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // ---------- Notes ----------
            Spacer(Modifier.height(22.dp))
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
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MedicineBlock(
    number: Int,
    entry: MedicineEntry,
    canRemove: Boolean,
    onChange: ((MedicineEntry) -> MedicineEntry) -> Unit,
    onRemove: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .padding(14.dp)
    ) {
        // Header: pill icon, number, remove
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFFEFE8FB)),
                contentAlignment = Alignment.Center
            ) {
                MediIcon(IconKind.Pill, tint = Color(0xFF7A4FC9), iconSize = 18.dp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(R.string.medicine_n, localizedNumber(number)),
                color = MediColors.Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (canRemove) {
                Text(
                    stringResource(R.string.remove_action),
                    color = MediColors.Danger,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onRemove)
                        .padding(6.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        MediFieldCard(label = stringResource(R.string.medicine_name)) {
            MediInput(
                value = entry.name,
                onValueChange = { v -> onChange { it.copy(name = v) } },
                hint = stringResource(R.string.medicine_name_hint)
            )
        }
        Spacer(Modifier.height(10.dp))
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