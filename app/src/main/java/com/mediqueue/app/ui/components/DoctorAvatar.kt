package com.mediqueue.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.mediqueue.app.R
import com.mediqueue.app.data.ApiClient
import com.mediqueue.app.data.DoctorDto
import com.mediqueue.app.data.ProfileDto
import com.mediqueue.app.ui.theme.MediColors

/**
 * Gender + photo of people, looked up by name.
 * Many screens only know a doctor's name (appointments, prescriptions...), so the avatar
 * looks the details up here. The lists are filled whenever doctors are loaded.
 */
object AvatarStore {
    data class Entry(val gender: String?, val photoUrl: String?)

    private val doctors = mutableStateMapOf<String, Entry>()
    var me by mutableStateOf<Entry?>(null)
        private set

    private fun key(name: String) = name.removePrefix("Dr. ").trim().lowercase()

    fun putDoctors(list: List<DoctorDto>) {
        list.forEach { doctors[key(it.name)] = Entry(it.gender, it.photoUrl) }
    }

    fun doctor(name: String): Entry? = doctors[key(name)]

    fun setMe(p: ProfileDto) {
        me = Entry(p.gender, p.photoUrl)
        if (p.role.equals("DOCTOR", ignoreCase = true)) {
            doctors[key(p.name)] = Entry(p.gender, p.photoUrl)
        }
    }

    fun clear() {
        doctors.clear()
        me = null
    }
}

private val AvatarTints = listOf(
    Color(0xFFD8F0EE),
    Color(0xFFDCE9FB),
    Color(0xFFFDE9D9),
    Color(0xFFEBE4FA),
    Color(0xFFFCE4EA)
)

private fun tintFor(name: String): Color {
    val seed = name.removePrefix("Dr. ").trim().hashCode()
    return AvatarTints[((seed % AvatarTints.size) + AvatarTints.size) % AvatarTints.size]
}

/** Illustration for a person. A patient without gender gets null (the caller shows initials). */
@DrawableRes
fun avatarRes(isDoctor: Boolean, gender: String?): Int? = when (gender?.uppercase()) {
    "MALE" -> if (isDoctor) R.drawable.ic_avatar_doctor_male else R.drawable.ic_avatar_user_male
    "FEMALE" -> if (isDoctor) R.drawable.ic_avatar_doctor_female else R.drawable.ic_avatar_user_female
    else -> if (isDoctor) R.drawable.ic_avatar_doctor_neutral else null
}

/** Real photo if there is one. While loading or on error it shows the illustration. */
@Composable
private fun PhotoOrIllustration(photoUrl: String?, @DrawableRes res: Int) {
    val illustration: @Composable () -> Unit = {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
    val url = ApiClient.absoluteUrl(photoUrl)
    if (url == null) {
        illustration()
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = { illustration() },
            error = { illustration() }
        )
    }
}

/**
 * Doctor avatar: real photo, else a male / female / neutral doctor illustration.
 * Gender and photo are looked up by the doctor's name (see AvatarStore).
 */
@Composable
fun DoctorAvatar(
    name: String,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 56.dp,
    corner: Dp = avatarSize * 0.3f
) {
    val entry = AvatarStore.doctor(name)
    Box(
        modifier = Modifier
            .size(avatarSize)
            .clip(RoundedCornerShape(corner))
            .background(tintFor(name))
            .then(modifier),
        contentAlignment = Alignment.BottomCenter
    ) {
        PhotoOrIllustration(entry?.photoUrl, avatarRes(true, entry?.gender)!!)
    }
}

/**
 * The logged-in person's own avatar (patient / admin): photo, else gender illustration,
 * else gradient circle with initials.
 */
@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 46.dp,
    shape: Shape = CircleShape
) {
    val me = AvatarStore.me
    val res = avatarRes(false, me?.gender)
    Box(
        modifier = Modifier
            .size(avatarSize)
            .clip(shape)
            .then(modifier),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (res == null && me?.photoUrl == null) {
            InitialsFill(name, avatarSize)
        } else {
            Box(Modifier.fillMaxSize().background(tintFor(name)), contentAlignment = Alignment.BottomCenter) {
                PhotoOrIllustration(me?.photoUrl, res ?: R.drawable.ic_avatar_user_neutral)
            }
        }
    }
}

/**
 * Any patient's avatar (e.g. in the doctor's appointment list): photo, else gender
 * illustration, else initials. Data comes with the appointment, not from AvatarStore.
 */
@Composable
fun PatientAvatar(
    name: String,
    gender: String?,
    photoUrl: String?,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 46.dp,
    shape: Shape = CircleShape
) {
    val res = avatarRes(false, gender)
    Box(
        modifier = Modifier
            .size(avatarSize)
            .clip(shape)
            .then(modifier),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (res == null && photoUrl.isNullOrBlank()) {
            InitialsFill(name, avatarSize)
        } else {
            Box(Modifier.fillMaxSize().background(tintFor(name)), contentAlignment = Alignment.BottomCenter) {
                PhotoOrIllustration(photoUrl, res ?: R.drawable.ic_avatar_user_neutral)
            }
        }
    }
}

@Composable
private fun InitialsFill(name: String, size: Dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(MediColors.TealLight, MediColors.TealDark))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialsOf(name),
            color = Color.White,
            fontSize = (size.value * 0.32f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Avatar for edit screens, with a small "+" badge. Tap to choose a photo.
 * [model] is a local picture (Uri) or a server photo URL; when null the illustration shows.
 */
@Composable
fun PhotoPickerAvatar(
    name: String,
    model: Any?,
    fallbackRes: Int?,
    avatarSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    busy: Boolean = false
) {
    Box(modifier = modifier.size(avatarSize + 6.dp)) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(tintFor(name))
                .clickable(enabled = !busy, onClick = onClick),
            contentAlignment = Alignment.BottomCenter
        ) {
            val shown = if (model is String) ApiClient.absoluteUrl(model) else model
            val fallback: @Composable () -> Unit = {
                if (fallbackRes != null) {
                    Image(
                        painter = painterResource(fallbackRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    InitialsFill(name, avatarSize)
                }
            }
            if (shown == null) {
                fallback()
            } else {
                SubcomposeAsyncImage(
                    model = shown,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = { fallback() },
                    error = { fallback() }
                )
            }
            if (busy) {
                Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.6f)), Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(avatarSize * 0.3f),
                        strokeWidth = 2.dp,
                        color = MediColors.Teal
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(MediColors.Teal)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            MediIcon(IconKind.Plus, tint = Color.White, iconSize = 14.dp, strokeWidth = 2.2f)
        }
    }
}

/** Male / Female chips. Tap the selected one again to clear it. [selected] is "MALE", "FEMALE" or "". */
@Composable
fun GenderSelector(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf("MALE" to R.string.gender_male, "FEMALE" to R.string.gender_female).forEach { (value, label) ->
            val on = selected.equals(value, ignoreCase = true)
            val shape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(if (on) MediColors.Pill else MediColors.Card)
                    .border(1.5.dp, if (on) MediColors.Teal else MediColors.Border, shape)
                    .clickable { onSelect(if (on) "" else value) }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(label),
                    color = if (on) MediColors.TealDark else MediColors.Text,
                    fontSize = 13.sp,
                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
