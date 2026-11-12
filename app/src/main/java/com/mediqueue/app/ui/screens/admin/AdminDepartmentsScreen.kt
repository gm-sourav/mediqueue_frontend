package com.mediqueue.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.AdminBottomBar
import com.mediqueue.app.ui.components.AdminTab
import com.mediqueue.app.ui.components.MediFieldCard
import com.mediqueue.app.ui.components.MediInput
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.screens.patient.departmentGlyph
import com.mediqueue.app.ui.theme.MediColors

data class AdminDepartment(
    val id: Long,
    val name: String,
    val icon: String,        // "heart", "brain", ...
    val doctorCount: Int
)

private val iconOptions = listOf("general", "heart", "brain", "tooth", "eye", "child", "bone")

@Composable
fun AdminDepartmentsScreen(
    departments: List<AdminDepartment>,
    isSubmitting: Boolean,
    clearSignal: Int,
    onAdd: (name: String, icon: String) -> Unit,
    onTabSelected: (AdminTab) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var icon by rememberSaveable { mutableStateOf("general") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    // Add hoye gele name field khali
    LaunchedEffect(clearSignal) {
        if (clearSignal > 0) {
            name = ""
            submitted = false
        }
    }

    Scaffold(
        containerColor = MediColors.Bg,
        bottomBar = { AdminBottomBar(selected = AdminTab.Departments, onSelect = onTabSelected) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                stringResource(R.string.admin_departments_title),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))
            MediFieldCard(
                label = stringResource(R.string.add_department),
                error = submitted && name.isBlank(),
                errorText = stringResource(R.string.err_required)
            ) {
                MediInput(
                    value = name,
                    onValueChange = { name = it.take(60) },
                    hint = stringResource(R.string.admin_dept_name_hint),
                    imeAction = ImeAction.Done
                )
                Text(
                    stringResource(R.string.admin_icon),
                    color = MediColors.Muted,
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.forEach { option ->
                        val selected = option == icon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) MediColors.Teal else MediColors.Pill)
                                .clickable { icon = option },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                departmentGlyph(option),
                                color = if (selected) MediColors.OnTeal else MediColors.Teal,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        submitted = true
                        if (name.isNotBlank()) onAdd(name.trim(), icon)
                    },
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediColors.Teal,
                        contentColor = MediColors.OnTeal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.admin_add_button), fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(20.dp))
            if (departments.isEmpty()) {
                Text(
                    stringResource(R.string.admin_no_departments),
                    color = MediColors.Muted,
                    fontSize = 13.sp
                )
            }
            departments.forEach { d ->
                DepartmentRow(d)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DepartmentRow(d: AdminDepartment) {
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
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MediColors.Pill),
            contentAlignment = Alignment.Center
        ) {
            Text(departmentGlyph(d.icon), color = MediColors.Teal, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                d.name,
                color = MediColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(3.dp))
            Text(
                stringResource(R.string.doctors_count, localizedNumber(d.doctorCount)),
                color = MediColors.Muted,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AdminDepartmentsPreview() {
    AdminDepartmentsScreen(
        departments = listOf(
            AdminDepartment(1, "Cardiology", "heart", 1),
            AdminDepartment(2, "Neurology", "brain", 1),
            AdminDepartment(3, "Dental", "tooth", 1)
        ),
        isSubmitting = false,
        clearSignal = 0,
        onAdd = { _, _ -> },
        onTabSelected = {}
    )
}