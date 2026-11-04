package com.mediqueue.app.ui.screens.auth

import android.util.Patterns
import androidx.compose.ui.platform.LocalContext
import com.mediqueue.app.LocaleHelper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.mediqueue.app.R
import com.mediqueue.app.ui.theme.MediColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val LinkLight = Color(0xFFC9D8D4)

@Composable
fun LoginScreen(

    onLoginSuccess: () -> Unit,
    onCreateAccount: () -> Unit
) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currentLang = LocalConfiguration.current.locales[0].language
    val checkFieldsMsg = stringResource(R.string.err_check_fields)

    fun signIn() {
        if (loading) return
        val emailOk = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        val passOk = password.length >= 6
        emailError = !emailOk
        passwordError = !passOk
        if (!emailOk || !passOk) {
            scope.launch { snackbar.showSnackbar(checkFieldsMsg) }
            return
        }
        // DUMMY: pore ekhane backend login call boshbe
        scope.launch {
            loading = true
            delay(1200)
            loading = false
            onLoginSuccess()
        }
    }

    Scaffold(
        containerColor = MediColors.Bg,
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                androidx.compose.material3.Snackbar(
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
                Pill(text = stringResource(R.string.brand_pill), letterSpacing = 0.6.sp)
                Pill(
                    text = stringResource(R.string.lang_toggle),
                    modifier = Modifier.clickable {
                        val next = if (currentLang == "bn") "en" else "bn"
                        LocaleHelper.apply(context, next)
                    }
                )
            }

            Spacer(Modifier.height(38.dp))
            Text(
                text = stringResource(R.string.login_title),
                color = MediColors.Text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                color = MediColors.Muted,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(22.dp))
            FieldCard(label = stringResource(R.string.email), error = emailError) {
                BasicTextField(
                    value = email,
                    onValueChange = { email = it; emailError = false },
                    singleLine = true,
                    textStyle = TextStyle(color = MediColors.Text, fontSize = 13.sp),
                    cursorBrush = SolidColor(MediColors.Teal),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    decorationBox = { inner ->
                        Box {
                            if (email.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.email_hint),
                                    color = MediColors.Muted,
                                    fontSize = 13.sp
                                )
                            }
                            inner()
                        }
                    }
                )
            }

            Spacer(Modifier.height(12.dp))
            FieldCard(label = stringResource(R.string.password), error = passwordError) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = false },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = MediColors.Muted, fontSize = 13.sp),
                        cursorBrush = SolidColor(MediColors.Teal),
                        visualTransformation = if (showPassword) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )
                    Text(
                        text = stringResource(if (showPassword) R.string.hide else R.string.show),
                        color = MediColors.Muted,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { showPassword = !showPassword }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.forgot_password),
                color = MediColors.Muted,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { signIn() },
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
                    Text(text = stringResource(R.string.sign_in), fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(26.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.new_to),
                    color = LinkLight,
                    fontSize = 13.sp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.create_account),
                    color = MediColors.Teal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onCreateAccount() }
                )
            }
        }
    }
}

@Composable
private fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    letterSpacing: TextUnit = TextUnit.Unspecified
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MediColors.Pill)
            .padding(horizontal = 15.dp, vertical = 11.dp)
    ) {
        Text(
            text = text,
            color = MediColors.Teal,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = letterSpacing
        )
    }
}

@Composable
private fun FieldCard(
    label: String,
    error: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(15.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, if (error) MediColors.Danger else MediColors.Border, shape)
            .padding(horizontal = 14.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = label,
            color = MediColors.Text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = {}, onCreateAccount = {})
}