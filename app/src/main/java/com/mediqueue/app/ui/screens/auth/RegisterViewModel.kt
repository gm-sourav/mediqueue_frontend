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
import com.mediqueue.app.data.RegisterRequest
import kotlinx.coroutines.launch
import java.io.IOException

class RegisterViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorRes by mutableStateOf<Int?>(null)
        private set

    /** Register korar por shathe shathe login kore session save kore (register e token ashe na). */
    fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        onSuccess: (AuthResponse) -> Unit
    ) {
        if (isLoading) return
        isLoading = true
        errorRes = null
        viewModelScope.launch {
            try {
                val reg = ApiClient.api.register(
                    RegisterRequest(name.trim(), email.trim(), password, phone)
                )
                if (!reg.isSuccessful) {
                    errorRes = if (reg.code() in 400..499) {
                        R.string.error_register_failed
                    } else {
                        R.string.error_generic
                    }
                    return@launch
                }
                val login = ApiClient.api.login(LoginRequest(email.trim(), password))
                val auth = login.body()
                if (login.isSuccessful && auth != null) {
                    ApiClient.session.save(auth)
                    onSuccess(auth)
                } else {
                    errorRes = R.string.error_account_created
                }
            } catch (_: IOException) {
                errorRes = R.string.error_network
            } catch (_: Exception) {
                errorRes = R.string.error_generic
            } finally {
                isLoading = false
            }
        }
    }
}