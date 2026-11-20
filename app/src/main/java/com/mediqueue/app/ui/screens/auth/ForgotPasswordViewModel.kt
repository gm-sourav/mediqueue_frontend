package com.mediqueue.app.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediqueue.app.R
import com.mediqueue.app.data.ApiClient
import com.mediqueue.app.data.ForgotPasswordRequest
import com.mediqueue.app.data.ResetPasswordRequest
import kotlinx.coroutines.launch
import java.io.IOException

class ForgotPasswordViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorRes by mutableStateOf<Int?>(null)
        private set

    fun clearError() {
        errorRes = null
    }

    /** Step 1: ask the server to send a 6-digit code. Server always says OK (no email leak). */
    fun requestCode(email: String, onSent: () -> Unit) {
        if (isLoading) return
        isLoading = true
        errorRes = null
        viewModelScope.launch {
            try {
                val res = ApiClient.api.forgotPassword(ForgotPasswordRequest(email.trim()))
                if (res.isSuccessful) {
                    onSent()
                } else {
                    errorRes = if (res.code() == 400) R.string.err_invalid_email else R.string.error_generic
                }
            } catch (e: IOException) {
                errorRes = R.string.error_network
            } catch (e: Exception) {
                errorRes = R.string.error_generic
            } finally {
                isLoading = false
            }
        }
    }

    /** Step 2: send code + new password. */
    fun resetPassword(email: String, code: String, newPassword: String, onDone: () -> Unit) {
        if (isLoading) return
        isLoading = true
        errorRes = null
        viewModelScope.launch {
            try {
                val res = ApiClient.api.resetPassword(
                    ResetPasswordRequest(email.trim(), code.trim(), newPassword)
                )
                if (res.isSuccessful) {
                    onDone()
                } else {
                    errorRes = if (res.code() == 400) R.string.fp_invalid_code else R.string.error_generic
                }
            } catch (e: IOException) {
                errorRes = R.string.error_network
            } catch (e: Exception) {
                errorRes = R.string.error_generic
            } finally {
                isLoading = false
            }
        }
    }
}