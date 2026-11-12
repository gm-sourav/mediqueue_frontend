package com.mediqueue.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mediqueue.app.ui.components.AdminBottomBar
import com.mediqueue.app.ui.components.AdminTab
import com.mediqueue.app.ui.components.DoctorBottomBar
import com.mediqueue.app.ui.components.DoctorTab
import com.mediqueue.app.ui.components.PatientBottomBar
import com.mediqueue.app.ui.components.PatientTab
import com.mediqueue.app.ui.theme.MediColors

@Composable
fun ProfileWithBottomBar(
    role: String?,
    onPatientTab: (PatientTab) -> Unit,
    onDoctorTab: (DoctorTab) -> Unit,
    onAdminTab: (AdminTab) -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediColors.Bg)
    ) {
        Box(modifier = Modifier.weight(1f)) { content() }
        when (role) {
            "PATIENT" -> PatientBottomBar(PatientTab.Profile, onPatientTab)
            "DOCTOR" -> DoctorBottomBar(DoctorTab.Profile, onDoctorTab)
            "ADMIN" -> AdminBottomBar(AdminTab.Profile, onAdminTab)
        }
    }
}