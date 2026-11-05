package com.mediqueue.app.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.BadgeGreenBg
import com.mediqueue.app.ui.components.BadgeGreenText
import com.mediqueue.app.ui.components.MediBadge
import com.mediqueue.app.ui.components.initialsOf
import com.mediqueue.app.ui.components.localizedDecimal
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors

private val AvatarBg = Color(0xFF1D3530)

data class DoctorDetail(
    val id: Long,
    val name: String,
    val specialty: String,
    val department: String,
    val experienceYears: Int,
    val fee: Int,
    val rating: Double,
    val about: String,
    val availableToday: Boolean,
    val nextSlots: List<String>
)

@Composable
fun DoctorDetailScreen(
    doctor: DoctorDetail,
    onBack: () -> Unit,
    onBookClick: () -> Unit,
    onSeeAllClick: () -> Unit
) {
    var favorite by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediColors.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Back + favorite
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹ " + stringResource(R.string.back),
                color = MediColors.Text.copy(alpha = 0.85f),
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack)
                    .padding(vertical = 6.dp, horizontal = 2.dp)
            )
            Text(
                text = if (favorite) "♥" else "♡",
                color = if (favorite) MediColors.Teal else MediColors.Text,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { favorite = !favorite }
                    .padding(6.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Avatar, name, specialty, availability
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(AvatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(doctor.name.removePrefix("Dr. ")),
                    color = MediColors.Teal,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = doctor.name,
                color = MediColors.Text,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = doctor.specialty + " · " + doctor.department,
                color = MediColors.Muted,
                fontSize = 12.5.sp
            )
            if (doctor.availableToday) {
                Spacer(Modifier.height(12.dp))
                MediBadge(
                    text = stringResource(R.string.available_today),
                    bg = BadgeGreenBg,
                    fg = BadgeGreenText
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                value = stringResource(R.string.years_short, localizedNumber(doctor.experienceYears)),
                label = stringResource(R.string.experience),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = stringResource(R.string.fee_compact, localizedNumber(doctor.fee)),
                label = stringResource(R.string.consultation),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = localizedDecimal(doctor.rating),
                label = stringResource(R.string.patient_rating),
                modifier = Modifier.weight(1f)
            )
        }

        // About
        Spacer(Modifier.height(22.dp))
        Text(
            text = stringResource(R.string.about),
            color = MediColors.Text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = doctor.about,
            color = MediColors.Text.copy(alpha = 0.8f),
            fontSize = 12.5.sp,
            lineHeight = 19.sp
        )

        // Next available
        Spacer(Modifier.height(16.dp))
        val cardShape = RoundedCornerShape(16.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(MediColors.Card)
                .border(1.dp, MediColors.Border, cardShape)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.next_available),
                    color = MediColors.Text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.see_all),
                    color = MediColors.Muted,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onSeeAllClick)
                        .padding(2.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            if (doctor.nextSlots.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_slots),
                    color = MediColors.Muted,
                    fontSize = 12.sp
                )
            } else {
                doctor.nextSlots.take(3).forEachIndexed { index, slot ->
                    if (index > 0) Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MediColors.Pill)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = slot,
                            color = MediColors.Teal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onBookClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MediColors.Teal,
                contentColor = MediColors.OnTeal
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = stringResource(R.string.book_now), fontSize = 14.sp)
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            color = MediColors.Text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            color = MediColors.Muted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DoctorDetailPreview() {
    DoctorDetailScreen(
        doctor = DoctorDetail(
            id = 1,
            name = "Dr. Ayesha Rahman",
            specialty = "Cardiologist",
            department = "Cardiology",
            experienceYears = 12,
            fee = 1000,
            rating = 4.9,
            about = "Experienced cardiologist focused on clear, patient-centred care.",
            availableToday = true,
            nextSlots = listOf("Today · 10:30 AM")
        ),
        onBack = {},
        onBookClick = {},
        onSeeAllClick = {}
    )
}