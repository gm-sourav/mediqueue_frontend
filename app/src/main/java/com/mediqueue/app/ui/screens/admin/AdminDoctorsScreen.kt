package com.mediqueue.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.AdminBottomBar
import com.mediqueue.app.ui.components.AdminTab
import com.mediqueue.app.ui.components.initialsOf
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors

data class AdminDoctor(
    val id: Long,
    val name: String,
    val email: String,
    val department: String,
    val experienceYears: Int,
    val fee: Int
)

@Composable
fun AdminDoctorsScreen(
    doctors: List<AdminDoctor>,
    onAddDoctor: () -> Unit,
    onTabSelected: (AdminTab) -> Unit
) {
    Scaffold(
        containerColor = MediColors.Bg,
        bottomBar = { AdminBottomBar(selected = AdminTab.Doctors, onSelect = onTabSelected) }
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
                Column {
                    Text(
                        stringResource(R.string.admin_doctors_title),
                        color = MediColors.Text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.doctors_count, localizedNumber(doctors.size)),
                        color = MediColors.Muted,
                        fontSize = 12.sp
                    )
                }
                Button(
                    onClick = onAddDoctor,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediColors.Teal,
                        contentColor = MediColors.OnTeal
                    )
                ) {
                    Text(stringResource(R.string.add_action), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(18.dp))
            if (doctors.isEmpty()) {
                Text(
                    stringResource(R.string.no_doctors),
                    color = MediColors.Muted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
            doctors.forEach { d ->
                DoctorRow(d)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DoctorRow(d: AdminDoctor) {
    val shape = RoundedCornerShape(16.dp)
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
                .size(44.dp)
                .clip(CircleShape)
                .background(MediColors.Pill),
            contentAlignment = Alignment.Center
        ) {
            Text(
                initialsOf(d.name),
                color = MediColors.Teal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                d.name,
                color = MediColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                d.department + " · " + stringResource(R.string.years_short, localizedNumber(d.experienceYears)),
                color = MediColors.Muted,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                d.email,
                color = MediColors.Muted,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(R.string.fee_compact, localizedNumber(d.fee)),
            color = MediColors.Teal,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AdminDoctorsPreview() {
    AdminDoctorsScreen(
        doctors = listOf(
            AdminDoctor(1, "Dr. Karim Ahmed", "karim@mediqueue.com", "Cardiology", 12, 800),
            AdminDoctor(2, "Dr. Nusrat Jahan", "nusrat@mediqueue.com", "Neurology", 8, 700)
        ),
        onAddDoctor = {},
        onTabSelected = {}
    )
}