package com.mediqueue.app.ui.screens.auth

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.MediFieldCard
import com.mediqueue.app.ui.components.MediInput
import com.mediqueue.app.ui.components.MediPill
import com.mediqueue.app.ui.theme.MediColors
import kotlinx.coroutines.launch

/**
 * Two steps on one screen:
 *  step 0 -> enter email, server sends a 6-digit code
 *  step 1 -> enter code + new password
 */
@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    onResetDone: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    var step by rememberSaveable { mutableStateOf(0) }
    var email by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val loading = viewModel.isLoading
    val errorText = viewModel.errorRes?.let { stringResource(it) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val codeSentMsg = stringResource(R.string.fp_code_sent)

    // Back: step 1 -> step 0, step 0 -> login
    BackHandler {
        if (step == 1) {
            step = 0
            code = ""; password = ""; confirm = ""; submitted = false
        } else onBackToLogin()
    }

    LaunchedEffect(errorText) {
        if (errorText != null) {
            snackbar.showSnackbar(errorText)
            viewModel.clearError()
        }
    }

    val emailOk = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val codeOk = code.length == 6
    val passOk = password.length >= 6
    val matchOk = password == confirm

    fun sendCode(resend: Boolean) {
        submitted = true
        if (!emailOk) return
        viewModel.requestCode(email) {
            submitted = false
            step = 1
            if (resend) scope.launch { snackbar.showSnackbar(codeSentMsg) }
        }
    }

    fun reset() {
        submitted = true
        if (!codeOk || !passOk || !matchOk) return
        viewModel.resetPassword(email, code, password) { onResetDone() }
    }

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
                .padding(pad)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
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
                        .clickable {
                            if (step == 1) {
                                step = 0
                                code = ""; password = ""; confirm = ""; submitted = false
                            } else onBackToLogin()
                        }
                        .padding(vertical = 6.dp, horizontal = 2.dp)
                )
                MediPill(text = stringResource(R.string.brand_pill), letterSpacing = 0.6.sp)
            }

            Spacer(Modifier.height(38.dp))
            Text(
                text = stringResource(R.string.fp_title),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (step == 0) stringResource(R.string.fp_subtitle_email)
                else stringResource(R.string.fp_subtitle_code),
                color = MediColors.Muted,
                fontSize = 13.sp
            )
            if (step == 1) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.fp_sent_to, email.trim()),
                    color = MediColors.Teal,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(22.dp))

            if (step == 0) {
                MediFieldCard(
                    label = stringResource(R.string.email),
                    error = submitted && !emailOk,
                    errorText = stringResource(R.string.err_invalid_email)
                ) {
                    MediInput(
                        value = email,
                        onValueChange = { email = it },
                        hint = stringResource(R.string.email_hint),
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    )
                }
            } else {
                MediFieldCard(
                    label = stringResource(R.string.fp_code),
                    error = submitted && !codeOk,
                    errorText = stringResource(R.string.fp_err_code)
                ) {
                    MediInput(
                        value = code,
                        onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
                        hint = stringResource(R.string.fp_code_hint),
                        keyboardType = KeyboardType.NumberPassword
                    )
                }
                Spacer(Modifier.height(12.dp))
                MediFieldCard(
                    label = stringResource(R.string.fp_new_password),
                    error = submitted && !passOk,
                    errorText = stringResource(R.string.err_password_short)
                ) {
                    MediInput(
                        value = password,
                        onValueChange = { password = it },
                        hint = stringResource(R.string.password),
                        keyboardType = KeyboardType.Password,
                        password = true
                    )
                }
                Spacer(Modifier.height(12.dp))
                MediFieldCard(
                    label = stringResource(R.string.confirm_password),
                    error = submitted && passOk && !matchOk,
                    errorText = stringResource(R.string.err_password_mismatch)
                ) {
                    MediInput(
                        value = confirm,
                        onValueChange = { confirm = it },
                        hint = stringResource(R.string.confirm_password_hint),
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        password = true
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { if (step == 0) sendCode(false) else reset() },
                enabled = !loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediColors.Teal,
                    contentColor = MediColors.OnTeal,
                    disabledContainerColor = MediColors.Teal.copy(alpha = 0.6f),
                    disabledContentColor = MediColors.OnTeal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MediColors.OnTeal
                    )
                } else {
                    Text(
                        text = stringResource(if (step == 0) R.string.fp_send_code else R.string.fp_reset_button),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            if (step == 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = stringResource(R.string.fp_resend),
                        color = MediColors.Teal,
                        fontSize = 12.5.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = !loading) { sendCode(true) }
                            .padding(8.dp)
                    )
                    Text(
                        text = stringResource(R.string.fp_change_email),
                        color = MediColors.Muted,
                        fontSize = 12.5.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                step = 0
                                code = ""; password = ""; confirm = ""; submitted = false
                            }
                            .padding(8.dp)
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.fp_back_to_login),
                    color = MediColors.Muted,
                    fontSize = 12.5.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onBackToLogin() }
                        .padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ForgotPasswordPreview() {
    ForgotPasswordScreen(onBackToLogin = {}, onResetDone = {})
}