package com.mediqueue.app.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediqueue.app.R
import com.mediqueue.app.data.ApiClient
import com.mediqueue.app.data.ProfileDto
import com.mediqueue.app.data.UpdateProfileRequest
import com.mediqueue.app.ui.components.AvatarStore
import com.mediqueue.app.ui.components.compressForUpload
import com.mediqueue.app.ui.components.photoPart
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.launch
import java.io.IOException

class ProfileViewModel : ViewModel() {

    var profile by mutableStateOf<ProfileDto?>(null)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var isUploading by mutableStateOf(false)
        private set

    var errorRes by mutableStateOf<Int?>(null)
        private set

    var unauthorized by mutableStateOf(false)
        private set

    fun clearError() {
        errorRes = null
    }

    fun load() {
        viewModelScope.launch {
            errorRes = null
            try {
                val res = ApiClient.api.getMe()
                when {
                    res.isSuccessful -> applyProfile(res.body())
                    res.code() == 401 -> unauthorized = true
                    else -> errorRes = R.string.error_generic
                }
            } catch (e: IOException) {
                errorRes = R.string.error_network
            } catch (e: Exception) {
                errorRes = R.string.error_generic
            }
        }
    }

    private fun applyProfile(p: ProfileDto?) {
        profile = p
        if (p != null) AvatarStore.setMe(p)
    }

    fun save(
        name: String,
        phone: String,
        gender: String,
        bloodGroup: String,
        dateOfBirth: String,
        onSaved: () -> Unit
    ) {
        if (isSaving) return
        isSaving = true
        errorRes = null
        viewModelScope.launch {
            try {
                val res = ApiClient.api.updateMe(UpdateProfileRequest(name.trim(), phone.trim(), gender, bloodGroup, dateOfBirth))
                when {
                    res.isSuccessful -> {
                        val body = res.body()
                        applyProfile(body)
                        if (body != null) ApiClient.session.updateName(body.name)
                        onSaved()
                    }
                    res.code() == 401 -> unauthorized = true
                    res.code() == 400 -> errorRes = R.string.pd_err_check
                    else -> errorRes = R.string.error_generic
                }
            } catch (e: IOException) {
                errorRes = R.string.error_network
            } catch (e: Exception) {
                errorRes = R.string.error_generic
            } finally {
                isSaving = false
            }
        }
    }

    fun uploadPhoto(context: Context, uri: Uri) {
        if (isUploading) return
        isUploading = true
        errorRes = null
        viewModelScope.launch {
            try {
                val bytes = compressForUpload(context, uri)
                if (bytes == null) {
                    errorRes = R.string.pd_photo_err
                    return@launch
                }
                val res = ApiClient.api.uploadMyPhoto(photoPart(bytes))
                when {
                    res.isSuccessful -> applyProfile(res.body())
                    res.code() == 401 -> unauthorized = true
                    else -> errorRes = R.string.pd_photo_err
                }
            } catch (e: IOException) {
                errorRes = R.string.error_network
            } catch (e: Exception) {
                errorRes = R.string.pd_photo_err
            } finally {
                isUploading = false
            }
        }
    }

    fun removePhoto() {
        if (isUploading) return
        isUploading = true
        errorRes = null
        viewModelScope.launch {
            try {
                val res = ApiClient.api.deleteMyPhoto()
                when {
                    res.isSuccessful -> applyProfile(res.body())
                    res.code() == 401 -> unauthorized = true
                    else -> errorRes = R.string.error_generic
                }
            } catch (e: IOException) {
                errorRes = R.string.error_network
            } catch (e: Exception) {
                errorRes = R.string.error_generic
            } finally {
                isUploading = false
            }
        }
    }

    fun reset() {
        AvatarStore.clear()
        isUploading = false
        profile = null
        isSaving = false
        errorRes = null
        unauthorized = false
    }
}
