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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.PatientBottomBar
import com.mediqueue.app.ui.components.PatientTab
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors

private val RxIconBg = Color(0xFF1C2E2A)
private val RxDetailBg = Color(0xFF1E322E)
private val RxNewBg = Color(0xFF1D4034)
private val RxNewText = Color(0xFF55D6A0)

data class MedicineItem(
    val name: String,     // "Medicine A"
    val dose: String,     // "1 tablet"
    val timing: String    // "After food"
)

data class PrescriptionItem(
    val id: Long,
    val dateText: String,     // "Nov 02"
    val department: String,   // "Cardiology"
    val doctorName: String,
    val summary: String,
    val isNew: Boolean,
    val medicines: List<MedicineItem>,
    val note: String?
)

@Composable
fun PrescriptionsScreen(
    prescriptions: List<PrescriptionItem>,
    onTabSelected: (PatientTab) -> Unit
) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val resources = LocalContext.current.resources
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selected = prescriptions.firstOrNull { it.id == selectedId } ?: prescriptions.firstOrNull()

    Scaffold(
        containerColor = MediColors.Bg,
        bottomBar = { PatientBottomBar(selected = PatientTab.Home, onSelect = onTabSelected) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.prescriptions_title),
                    color = MediColors.Text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RxIconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.rx_badge),
                        color = MediColors.Teal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.prescriptions_subtitle),
                color = MediColors.Muted,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(16.dp))
            if (prescriptions.isEmpty()) {
                Text(
                    stringResource(R.string.no_prescriptions),
                    color = MediColors.Muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 30.dp)
                )
            }

            prescriptions.forEach { p ->
                val medText = resources.getQuantityString(
                    R.plurals.medicines_count, p.medicines.size, localizedNumber(p.medicines.size)
                )
                val countText = if (p.note != null) {
                    medText + " · " + resources.getQuantityString(R.plurals.notes_count, 1, localizedNumber(1))
                } else medText

                val shape = RoundedCornerShape(16.dp)
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
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = (p.dateText + " · " + p.department).uppercase(locale),
                            color = MediColors.Muted,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (p.isNew) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(RxNewBg)
                                    .padding(horizontal = 11.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    stringResource(R.string.new_badge),
                                    color = RxNewText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(if (p.isNew) 2.dp else 12.dp))
                    Text(
                        p.doctorName,
                        color = MediColors.Text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(p.summary, color = MediColors.Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(countText, color = MediColors.Muted, fontSize = 13.sp)
                        Text(
                            text = stringResource(R.string.open_action) + " ›",
                            color = MediColors.Teal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { selectedId = p.id }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            // Detail card
            if (selected != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(RxDetailBg)
                        .padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.prescription_detail),
                        color = MediColors.Text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    selected.medicines.forEachIndexed { index, m ->
                        if (index > 0) Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                m.name + " · " + m.dose,
                                color = MediColors.Text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(m.timing, color = MediColors.Muted, fontSize = 13.sp)
                        }
                    }
                    if (selected.note != null) {
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MediColors.Border)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            selected.note,
                            color = MediColors.Muted,
                            fontSize = 12.5.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PrescriptionsPreview() {
    PrescriptionsScreen(
        prescriptions = listOf(
            PrescriptionItem(
                1, "Nov 02", "Cardiology", "Dr. Ayesha Rahman", "Follow-up and medication plan", true,
                listOf(MedicineItem("Medicine A", "1 tablet", "After food"), MedicineItem("Medicine B", "1 tablet", "Night")),
                "Take as directed by your doctor. Contact the clinic if you have questions."
            ),
            PrescriptionItem(
                2, "Oct 11", "General Medicine", "Dr. Farhan Kabir", "Prescription and visit notes", false,
                listOf(MedicineItem("Medicine C", "1 capsule", "Before food")), null
            )
        ),
        onTabSelected = {}
    )
}