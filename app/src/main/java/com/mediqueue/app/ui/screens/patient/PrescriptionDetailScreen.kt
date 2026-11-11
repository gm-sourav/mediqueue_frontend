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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.initialsOf
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors

private val DxHeaderBg = Color(0xFF1E322E)
private val DxNewBg = Color(0xFF1D4034)
private val DxNewText = Color(0xFF55D6A0)

/** Ekta prescription er puro page: doctor, diagnosis, medicines, advice */
@Composable
fun PrescriptionDetailScreen(
    prescription: PrescriptionItem,
    onBack: () -> Unit
) {
    Scaffold(containerColor = MediColors.Bg) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "‹ " + stringResource(R.string.back),
                color = MediColors.Muted,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.prescription_title),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // Doctor card
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DxHeaderBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MediColors.Pill),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initialsOf(prescription.doctorName),
                        color = MediColors.Teal,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        prescription.doctorName,
                        color = MediColors.Text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = listOf(prescription.department, prescription.dateText)
                            .filter { it.isNotBlank() }
                            .joinToString(" · "),
                        color = MediColors.Muted,
                        fontSize = 12.5.sp
                    )
                }
                if (prescription.isNew) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DxNewBg)
                            .padding(horizontal = 11.dp, vertical = 9.dp)
                    ) {
                        Text(
                            stringResource(R.string.new_badge),
                            color = DxNewText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Diagnosis
            if (prescription.summary.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                SectionLabel(stringResource(R.string.diagnosis_label))
                Spacer(Modifier.height(8.dp))
                InfoCard {
                    Text(
                        prescription.summary,
                        color = MediColors.Text,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            }

            // Medicines
            Spacer(Modifier.height(20.dp))
            SectionLabel(
                stringResource(R.string.medicines_title) + " · " + localizedNumber(prescription.medicines.size)
            )
            prescription.medicines.forEachIndexed { index, m ->
                Spacer(Modifier.height(10.dp))
                MedicineCard(number = index + 1, item = m)
            }

            // Advice
            if (prescription.note != null) {
                Spacer(Modifier.height(20.dp))
                SectionLabel(stringResource(R.string.rx_advice_title))
                Spacer(Modifier.height(8.dp))
                InfoCard {
                    Text(
                        prescription.note,
                        color = MediColors.Text,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = MediColors.Text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun MedicineCard(number: Int, item: MedicineItem) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MediColors.Pill),
            contentAlignment = Alignment.Center
        ) {
            Text(
                localizedNumber(number),
                color = MediColors.Teal,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                color = MediColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            if (item.timing.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(item.timing, color = MediColors.Muted, fontSize = 12.5.sp)
            }
        }
        if (item.dose.isNotBlank() && item.dose != "—") {
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MediColors.Pill)
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text(
                    item.dose,
                    color = MediColors.Teal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PrescriptionDetailPreview() {
    PrescriptionDetailScreen(
        prescription = PrescriptionItem(
            id = 1,
            dateText = "Nov 11",
            department = "Cardiology",
            doctorName = "Dr. Karim Ahmed",
            summary = "Viral fever",
            isNew = true,
            medicines = listOf(
                MedicineItem("Medicine A", "1 tablet", "Twice daily · 7 days"),
                MedicineItem("Medicine B", "5 ml", "Night")
            ),
            note = "Drink plenty of water and rest. Come back if fever lasts more than 3 days."
        ),
        onBack = {}
    )
}