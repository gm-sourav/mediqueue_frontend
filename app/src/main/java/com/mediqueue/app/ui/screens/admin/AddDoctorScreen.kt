package com.mediqueue.app.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.MediFieldCard
import com.mediqueue.app.ui.components.MediInput
import com.mediqueue.app.ui.theme.MediColors

@Composable
fun AddDoctorScreen(
    departments: List<AdminDepartment>,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onCreate: (
        name: String,
        email: String,
        password: String,
        phone: String,
        departmentId: Long,
        experienceYears: Int?,
        fee: Double?,
        bio: String
    ) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var experience by rememberSaveable { mutableStateOf("") }
    var fee by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var departmentId by rememberSaveable { mutableLongStateOf(-1L) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val nameOk = name.isNotBlank()
    val emailOk = email.trim().let { it.contains("@") && it.substringAfter("@").contains(".") }
    val passOk = password.length >= 6
    val deptOk = departmentId >= 0

    val required = stringResource(R.string.err_required)

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
                stringResource(R.string.admin_add_doctor_title),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.admin_add_doctor_sub),
                color = MediColors.Muted,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(18.dp))
            MediFieldCard(
                label = stringResource(R.string.full_name),
                error = submitted && !nameOk,
                errorText = required
            ) {
                MediInput(
                    value = name,
                    onValueChange = { name = it },
                    hint = stringResource(R.string.admin_doctor_name_hint)
                )
            }

            Spacer(Modifier.height(12.dp))
            MediFieldCard(
                label = stringResource(R.string.email),
                error = submitted && !emailOk,
                errorText = stringResource(R.string.err_invalid_email)
            ) {
                MediInput(
                    value = email,
                    onValueChange = { email = it },
                    hint = stringResource(R.string.email_hint),
                    keyboardType = KeyboardType.Email
                )
            }

            Spacer(Modifier.height(12.dp))
            MediFieldCard(
                label = stringResource(R.string.password),
                error = submitted && !passOk,
                errorText = stringResource(R.string.err_password_short)
            ) {
                MediInput(
                    value = password,
                    onValueChange = { password = it },
                    hint = stringResource(R.string.admin_password_hint),
                    password = true
                )
            }

            Spacer(Modifier.height(12.dp))
            MediFieldCard(label = stringResource(R.string.phone)) {
                MediInput(
                    value = phone,
                    onValueChange = { phone = it },
                    hint = stringResource(R.string.phone_hint),
                    keyboardType = KeyboardType.Phone
                )
            }

            Spacer(Modifier.height(12.dp))
            MediFieldCard(
                label = stringResource(R.string.admin_department),
                error = submitted && !deptOk,
                errorText = stringResource(R.string.err_select_department)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    departments.forEach { d ->
                        val selected = d.id == departmentId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) MediColors.Teal else MediColors.Pill)
                                .clickable { departmentId = d.id }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                d.name,
                                color = if (selected) MediColors.OnTeal else MediColors.Teal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MediFieldCard(
                    label = stringResource(R.string.admin_experience_label),
                    modifier = Modifier.weight(1f)
                ) {
                    MediInput(
                        value = experience,
                        onValueChange = { experience = it.filter { c -> c.isDigit() }.take(2) },
                        hint = "5",
                        keyboardType = KeyboardType.Number
                    )
                }
                MediFieldCard(
                    label = stringResource(R.string.admin_fee_label),
                    modifier = Modifier.weight(1f)
                ) {
                    MediInput(
                        value = fee,
                        onValueChange = { fee = it.filter { c -> c.isDigit() || c == '.' }.take(7) },
                        hint = "800",
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            MediFieldCard(label = stringResource(R.string.admin_bio_label)) {
                MediInput(
                    value = bio,
                    onValueChange = { bio = it.take(500) },
                    hint = stringResource(R.string.admin_bio_hint),
                    imeAction = ImeAction.Done
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    submitted = true
                    if (nameOk && emailOk && passOk && deptOk) {
                        onCreate(
                            name.trim(),
                            email.trim(),
                            password,
                            phone,
                            departmentId,
                            experience.toIntOrNull(),
                            fee.toDoubleOrNull(),
                            bio.trim()
                        )
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediColors.Teal,
                    contentColor = MediColors.OnTeal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.admin_create_doctor), fontSize = 14.sp)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun AddDoctorPreview() {
    AddDoctorScreen(
        departments = listOf(
            AdminDepartment(1, "Cardiology", "heart", 1),
            AdminDepartment(2, "Neurology", "brain", 1)
        ),
        isSubmitting = false,
        onBack = {},
        onCreate = { _, _, _, _, _, _, _, _ -> }
    )
}