package com.mediqueue.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Patterns
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.MediFieldCard
import com.mediqueue.app.ui.components.MediInput
import com.mediqueue.app.ui.components.MediPill
import com.mediqueue.app.ui.theme.MediColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val LinkLight = Color(0xFFC9D8D4)
private val PhoneRegex = Regex("^(\\+?88)?01[3-9]\\d{8}$")

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onSignInClick: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    var submitted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val nameError = submitted && name.isBlank()
    val emailError = submitted && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val phoneError = submitted && !PhoneRegex.matches(phone.replace(Regex("[\\s-]"), ""))
    val passError = submitted && password.length < 6
    // Mismatch: confirm e kichu lekha shuru korlei dekhabe
    val confirmError = (submitted || confirm.isNotEmpty()) && confirm != password

    fun submit() {
        if (loading) return
        submitted = true
        val ok = name.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() &&
                PhoneRegex.matches(phone.replace(Regex("[\\s-]"), "")) &&
                password.length >= 6 &&
                confirm == password
        if (!ok) return
        // DUMMY: pore ekhane backend register call boshbe
        scope.launch {
            loading = true
            delay(1200)
            loading = false
            onRegistered()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediColors.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediPill(text = stringResource(R.string.create_account), fontSize = 11.sp)
            Text(
                text = stringResource(R.string.register_step),
                color = MediColors.Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.register_title),
            color = MediColors.Text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.register_subtitle),
            color = MediColors.Muted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(20.dp))
        MediFieldCard(
            label = stringResource(R.string.full_name),
            error = nameError,
            errorText = stringResource(R.string.err_name)
        ) {
            MediInput(
                value = name,
                onValueChange = { name = it },
                hint = stringResource(R.string.full_name_hint)
            )
        }

        Spacer(Modifier.height(12.dp))
        MediFieldCard(
            label = stringResource(R.string.email),
            error = emailError,
            errorText = stringResource(R.string.err_email)
        ) {
            MediInput(
                value = email,
                onValueChange = { email = it },
                hint = stringResource(R.string.reg_email_hint),
                keyboardType = KeyboardType.Email
            )
        }

        Spacer(Modifier.height(12.dp))
        MediFieldCard(
            label = stringResource(R.string.phone),
            error = phoneError,
            errorText = stringResource(R.string.err_phone)
        ) {
            MediInput(
                value = phone,
                onValueChange = { phone = it },
                hint = stringResource(R.string.phone_hint),
                keyboardType = KeyboardType.Phone
            )
        }

        Spacer(Modifier.height(12.dp))
        MediFieldCard(
            label = stringResource(R.string.password),
            error = passError,
            errorText = stringResource(R.string.err_password_short)
        ) {
            MediInput(
                value = password,
                onValueChange = { password = it },
                hint = stringResource(R.string.reg_password_hint),
                keyboardType = KeyboardType.Password,
                password = true
            )
        }

        Spacer(Modifier.height(12.dp))
        MediFieldCard(
            label = stringResource(R.string.confirm_password),
            error = confirmError,
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

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { submit() },
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
                Text(text = stringResource(R.string.create_account), fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.already_registered),
                color = LinkLight,
                fontSize = 13.sp
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.sign_in_link),
                color = MediColors.Teal,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onSignInClick() }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RegisterScreenPreview() {
    RegisterScreen(onRegistered = {}, onSignInClick = {})
}