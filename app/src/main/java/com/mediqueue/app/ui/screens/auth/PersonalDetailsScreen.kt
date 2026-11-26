package com.mediqueue.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import com.mediqueue.app.ui.components.GenderSelector
import com.mediqueue.app.ui.components.PhotoPickerAvatar
import com.mediqueue.app.ui.components.avatarRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.data.ProfileDto
import com.mediqueue.app.ui.components.IconKind
import com.mediqueue.app.ui.components.MediFieldCard
import com.mediqueue.app.ui.components.MediIcon
import com.mediqueue.app.ui.components.MediInput
import com.mediqueue.app.ui.theme.MediColors

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(
    profile: ProfileDto,
    isSaving: Boolean,
    errorRes: Int?,
    onClearError: () -> Unit,
    onBack: () -> Unit,
    onSave: (name: String, phone: String, gender: String, bloodGroup: String, dateOfBirth: String) -> Unit,
    isUploading: Boolean = false,
    onPickPhoto: (Uri) -> Unit = {},
    onRemovePhoto: () -> Unit = {}
) {
    var name by rememberSaveable(profile.id) { mutableStateOf(profile.name) }
    var phone by rememberSaveable(profile.id) { mutableStateOf(profile.phone.orEmpty()) }
    var gender by rememberSaveable(profile.id) { mutableStateOf(profile.gender.orEmpty()) }
    var bloodGroup by rememberSaveable(profile.id) { mutableStateOf(profile.bloodGroup.orEmpty()) }
    var dob by rememberSaveable(profile.id) { mutableStateOf(profile.dateOfBirth.orEmpty()) }
    var showDobPicker by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPickPhoto(uri)
    }

    val snackbar = remember { SnackbarHostState() }
    val errorText = errorRes?.let { stringResource(it) }
    LaunchedEffect(errorText) {
        if (errorText != null) {
            snackbar.showSnackbar(errorText)
            onClearError()
        }
    }

    val nameOk = name.trim().isNotEmpty()
    val phoneOk = phone.isBlank() || Regex("^[0-9+\\-() ]{6,20}$").matches(phone.trim())
    val changed = name.trim() != profile.name || phone.trim() != profile.phone.orEmpty() ||
            gender != profile.gender.orEmpty() ||
            bloodGroup != profile.bloodGroup.orEmpty() || dob != profile.dateOfBirth.orEmpty()
    val isDoctor = profile.role.equals("DOCTOR", ignoreCase = true)

    val roleLabel = stringResource(
        when (profile.role.uppercase()) {
            "DOCTOR" -> R.string.role_doctor
            "ADMIN" -> R.string.role_admin
            else -> R.string.role_patient
        }
    )

    Scaffold(
        containerColor = MediColors.Bg,
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MediColors.Card,
                    contentColor = MediColors.Text
                )
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
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

            Spacer(Modifier.height(18.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PhotoPickerAvatar(
                    name = name.ifBlank { profile.name },
                    model = profile.photoUrl,
                    fallbackRes = avatarRes(isDoctor, gender.ifBlank { null }),
                    avatarSize = 96.dp,
                    busy = isUploading,
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.change_photo),
                        color = MediColors.Teal,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !isUploading) {
                                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            .padding(4.dp)
                    )
                    if (profile.photoUrl != null) {
                        Text(
                            text = stringResource(R.string.remove_photo),
                            color = MediColors.Danger,
                            fontSize = 12.5.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isUploading, onClick = onRemovePhoto)
                                .padding(4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.personal_details),
                    color = MediColors.Text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))
            MediFieldCard(
                label = stringResource(R.string.pd_name),
                error = submitted && !nameOk,
                errorText = stringResource(R.string.pd_err_name)
            ) {
                MediInput(
                    value = name,
                    onValueChange = { name = it },
                    hint = stringResource(R.string.pd_name),
                    keyboardType = KeyboardType.Text
                )
            }

            Spacer(Modifier.height(12.dp))
            MediFieldCard(
                label = stringResource(R.string.pd_phone),
                error = submitted && !phoneOk,
                errorText = stringResource(R.string.pd_err_phone)
            ) {
                MediInput(
                    value = phone,
                    onValueChange = { phone = it },
                    hint = stringResource(R.string.pd_phone_hint),
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
            }

            Spacer(Modifier.height(12.dp))
            MediFieldCard(label = stringResource(R.string.gender_label)) {
                GenderSelector(selected = gender, onSelect = { gender = it })
            }

            if (profile.role.equals("PATIENT", ignoreCase = true)) {
                Spacer(Modifier.height(12.dp))
                MediFieldCard(label = stringResource(R.string.dob_label)) {
                    val dobText = runCatching {
                        java.time.LocalDate.parse(dob).format(
                            java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.getDefault())
                        )
                    }.getOrNull()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showDobPicker = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dobText ?: stringResource(R.string.dob_hint),
                            color = if (dobText != null) MediColors.Text else MediColors.Muted,
                            fontSize = 14.sp
                        )
                        MediIcon(IconKind.Calendar, tint = MediColors.Teal, iconSize = 19.dp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                MediFieldCard(label = stringResource(R.string.blood_group_label)) {
                    BloodGroupSelector(selected = bloodGroup, onSelect = { bloodGroup = it })
                }
            }

            Spacer(Modifier.height(12.dp))
            ReadOnlyRow(
                icon = IconKind.Mail,
                label = stringResource(R.string.pd_email),
                value = profile.email,
                note = stringResource(R.string.pd_email_locked)
            )
            Spacer(Modifier.height(12.dp))
            ReadOnlyRow(
                icon = IconKind.Shield,
                label = stringResource(R.string.pd_role),
                value = roleLabel,
                note = null
            )

            Spacer(Modifier.height(22.dp))
            Button(
                onClick = {
                    submitted = true
                    if (nameOk && phoneOk) onSave(name, phone, gender, bloodGroup, dob)
                },
                enabled = changed && !isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediColors.Teal,
                    contentColor = MediColors.OnTeal,
                    disabledContainerColor = MediColors.Teal.copy(alpha = 0.35f),
                    disabledContentColor = MediColors.OnTeal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MediColors.OnTeal
                    )
                } else {
                    Text(text = stringResource(R.string.pd_save), fontSize = 14.sp)
                }
            }
        }
    }

    if (showDobPicker) {
        val nowMs = remember { System.currentTimeMillis() }
        val initialMs = remember(dob) {
            runCatching {
                java.time.LocalDate.parse(dob).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
            }.getOrNull()
        }
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = initialMs,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= nowMs
                override fun isSelectableYear(year: Int) = year <= java.time.LocalDate.now().year
            }
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDobPicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        dob = java.time.Instant.ofEpochMilli(ms)
                            .atZone(java.time.ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showDobPicker = false
                }) { Text(stringResource(R.string.ok_action), color = MediColors.Teal) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    if (dob.isNotEmpty()) dob = ""
                    showDobPicker = false
                }) { Text(stringResource(R.string.clear_action), color = MediColors.Muted) }
            }
        ) {
            androidx.compose.material3.DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun BloodGroupSelector(selected: String, onSelect: (String) -> Unit) {
    val groups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        groups.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { g ->
                    val on = g == selected
                    val shape = RoundedCornerShape(12.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(if (on) MediColors.Pill else MediColors.Card)
                            .border(1.5.dp, if (on) MediColors.Teal else MediColors.Border, shape)
                            .clickable { onSelect(if (on) "" else g) }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = g,
                            color = if (on) MediColors.TealDark else MediColors.Text,
                            fontSize = 13.sp,
                            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyRow(icon: IconKind, label: String, value: String, note: String?) {
    val shape = RoundedCornerShape(15.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Bg)
            .border(1.dp, MediColors.Border, shape)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MediColors.Pill),
            contentAlignment = Alignment.Center
        ) {
            MediIcon(icon, tint = MediColors.Teal, iconSize = 19.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = MediColors.Muted, fontSize = 11.sp)
            Spacer(Modifier.height(3.dp))
            Text(
                text = value,
                color = MediColors.Text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            if (note != null) {
                Spacer(Modifier.height(3.dp))
                Text(text = note, color = MediColors.Muted, fontSize = 10.5.sp)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PersonalDetailsPreview() {
    PersonalDetailsScreen(
        profile = ProfileDto(1, "Rahim Uddin", "rahim@test.com", "01700000000", "PATIENT"),
        isSaving = false,
        errorRes = null,
        onClearError = {},
        onBack = {},
        onSave = { _, _, _, _, _ -> }
    )
}
