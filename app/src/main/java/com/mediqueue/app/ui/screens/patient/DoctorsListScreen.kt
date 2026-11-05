package com.mediqueue.app.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.mediqueue.app.ui.components.BadgeGreenBg
import com.mediqueue.app.ui.components.BadgeGreenText
import com.mediqueue.app.ui.components.MediInput
import com.mediqueue.app.ui.components.PatientBottomBar
import com.mediqueue.app.ui.components.PatientTab
import com.mediqueue.app.ui.components.initialsOf
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors

private val AvatarBg = Color(0xFF1D3530)

data class DoctorItem(
    val id: Long,
    val name: String,
    val experienceYears: Int,
    val specialty: String,
    val fee: Int,
    val nextSlot: String?,        // jemon "Today · 10:30 AM", slot na thakle null
    val availableToday: Boolean
)

@Composable
fun DoctorsListScreen(
    departmentName: String,
    doctors: List<DoctorItem>,
    onDoctorClick: (DoctorItem) -> Unit,
    onTabSelected: (PatientTab) -> Unit,
    searchHint: String = stringResource(R.string.search_doctors),
    selectedTab: PatientTab? = null
) {
    var query by rememberSaveable { mutableStateOf("") }

    val filtered = remember(query, doctors) {
        val q = query.trim()
        if (q.isEmpty()) doctors
        else doctors.filter {
            it.name.contains(q, ignoreCase = true) || it.specialty.contains(q, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediColors.Bg)
    ) {
        // Header
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.department_label),
                color = MediColors.Muted,
                fontSize = 11.sp,
                letterSpacing = 0.6.sp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = departmentName,
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(14.dp))

            // Search bar
            val searchShape = RoundedCornerShape(14.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(searchShape)
                    .background(MediColors.Card)
                    .border(1.dp, MediColors.Border, searchShape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⌕", color = MediColors.Text, fontSize = 13.sp)
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    MediInput(
                        value = query,
                        onValueChange = { query = it },
                        hint = searchHint,
                        imeAction = ImeAction.Search
                    )
                }
            }
        }

        // Doctor list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_doctors),
                        color = MediColors.Muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
            items(filtered, key = { it.id }) { doctor ->
                DoctorCard(doctor) { onDoctorClick(doctor) }
            }
        }

        PatientBottomBar(selected = selectedTab, onSelect = onTabSelected)
    }
}

@Composable
private fun DoctorCard(doctor: DoctorItem, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        // Avatar + name
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AvatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(doctor.name.removePrefix("Dr. ")),
                    color = MediColors.Teal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = doctor.name,
                    color = MediColors.Text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.years_experience, localizedNumber(doctor.experienceYears)) +
                            " · " + doctor.specialty,
                    color = MediColors.Muted,
                    fontSize = 12.sp
                )
            }
        }

        // Fee row
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.consultation),
                color = MediColors.Text.copy(alpha = 0.85f),
                fontSize = 12.5.sp
            )
            Text(
                text = stringResource(R.string.fee_format, localizedNumber(doctor.fee)),
                color = MediColors.Text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Next slot + View
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val chipBg = if (doctor.availableToday) BadgeGreenBg else MediColors.Pill
            val chipFg = if (doctor.availableToday) BadgeGreenText else MediColors.Teal
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(chipBg)
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = doctor.nextSlot ?: stringResource(R.string.no_slots),
                    color = chipFg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = stringResource(R.string.view_action) + " ›",
                color = MediColors.Teal,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DoctorsListPreview() {
    DoctorsListScreen(
        departmentName = "Cardiology",
        searchHint = "Search cardiologists",
        doctors = listOf(
            DoctorItem(1, "Dr. Ayesha Rahman", 12, "Cardiologist", 1000, "Today · 10:30 AM", true),
            DoctorItem(2, "Dr. Samiul Haque", 8, "Cardiologist", 800, "Tomorrow · 9:00 AM", false),
            DoctorItem(3, "Dr. Tasnim Noor", 6, "Cardiologist", 700, "Wed · 11:00 AM", false)
        ),
        onDoctorClick = {},
        onTabSelected = {}
    )
}