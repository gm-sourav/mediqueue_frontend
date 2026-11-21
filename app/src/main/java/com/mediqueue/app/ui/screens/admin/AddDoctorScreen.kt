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
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import com.mediqueue.app.ui.components.GenderSelector
import com.mediqueue.app.ui.components.PhotoPickerAvatar
import com.mediqueue.app.ui.components.avatarRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.mediqueue.app.ui.components.AvatarStore
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
    editing: AdminDoctor? = null,
    onDelete: () -> Unit = {},
    onCreate: (
        name: String,
        email: String,
        password: String,
        phone: String,
        departmentId: Long,
        experienceYears: Int?,
        fee: Double?,
        bio: String,
        gender: String,
        photo: Uri?
    ) -> Unit
) {
    val isEdit = editing != null
    val key = editing?.id ?: -1L
    var name by rememberSaveable(key) { mutableStateOf(editing?.name.orEmpty()) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable(key) { mutableStateOf(editing?.phone.orEmpty()) }
    var experience by rememberSaveable(key) {
        mutableStateOf(editing?.experienceYears?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var fee by rememberSaveable(key) {
        mutableStateOf(editing?.fee?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var bio by rememberSaveable(key) { mutableStateOf(editing?.bio.orEmpty()) }
    var departmentId by rememberSaveable(key) { mutableLongStateOf(editing?.departmentId ?: -1L) }
    var gender by rememberSaveable(key) { mutableStateOf(editing?.gender.orEmpty()) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var photo by rememberSaveable { mutableStateOf<Uri?>(null) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) photo = uri
    }

    val nameOk = name.isNotBlank()
    val emailOk = isEdit || email.trim().let { it.contains("@") && it.substringAfter("@").contains(".") }
    val passOk = isEdit || password.length >= 6
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
                stringResource(if (isEdit) R.string.admin_edit_doctor_title else R.string.admin_add_doctor_title),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            if (!isEdit) {
                Text(
                    stringResource(R.string.admin_add_doctor_sub),
                    color = MediColors.Muted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(18.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PhotoPickerAvatar(
                    name = name.ifBlank { "Dr" },
                    model = photo ?: editing?.let { AvatarStore.doctor(it.name)?.photoUrl },
                    fallbackRes = avatarRes(true, gender.ifBlank { null }),
                    avatarSize = 92.dp,
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.admin_doctor_photo_hint),
                    color = MediColors.Muted,
                    fontSize = 11.5.sp
                )
            }
            Spacer(Modifier.height(14.dp))
            MediFieldCard(label = stringResource(R.string.gender_label)) {
                GenderSelector(selected = gender, onSelect = { gender = it })
            }
            Spacer(Modifier.height(12.dp))
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

            if (isEdit) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = editing?.email.orEmpty(),
                    color = MediColors.Muted,
                    fontSize = 12.5.sp
                )
            } else {
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
                            bio.trim(),
                            gender,
                            photo
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
                Text(
                    stringResource(if (isEdit) R.string.save_changes else R.string.admin_create_doctor),
                    fontSize = 14.sp
                )
            }
            if (isEdit) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MediColors.Danger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MediColors.Danger),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(stringResource(R.string.delete_doctor), fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_doctor_title)) },
            text = { Text(stringResource(R.string.delete_doctor_msg, editing?.name.orEmpty())) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) {
                    Text(stringResource(R.string.delete_action), color = MediColors.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.cancel_action))
                }
            },
            containerColor = MediColors.Card
        )
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
        onCreate = { _, _, _, _, _, _, _, _, _, _ -> }
    )
}