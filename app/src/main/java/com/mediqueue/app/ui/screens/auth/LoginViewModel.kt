package com.mediqueue.app.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediqueue.app.R
import com.mediqueue.app.data.ApiClient
import com.mediqueue.app.data.AuthResponse
import com.mediqueue.app.data.LoginRequest
import kotlinx.coroutines.launch
import java.io.IOException

class LoginViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorRes by mutableStateOf<Int?>(null)
        private set

    fun clearError() {
        errorRes = null
    }

    fun login(email: String, password: String, onSuccess: (AuthResponse) -> Unit) {
        if (isLoading) return
        isLoading = true
        errorRes = null
        viewModelScope.launch {
            try {
                val res = ApiClient.api.login(LoginRequest(email.trim(), password))
                val body = res.body()
                if (res.isSuccessful && body != null) {
                    ApiClient.session.save(body)
                    onSuccess(body)
                } else {
                    errorRes = if (res.code() in 400..499) {
                        R.string.error_invalid_credentials
                    } else {
                        R.string.error_generic
                    }
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