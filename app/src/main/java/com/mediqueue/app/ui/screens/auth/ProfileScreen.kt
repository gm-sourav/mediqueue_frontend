package com.mediqueue.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.DoctorAvatar
import com.mediqueue.app.ui.components.IconKind
import com.mediqueue.app.ui.components.MediIcon
import com.mediqueue.app.ui.theme.MediColors

private val LogoutRed = Color(0xFFC73B2E)

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    role: String, // "PATIENT", "DOCTOR" ba "ADMIN"
    onLanguageClick: () -> Unit,
    onLogout: () -> Unit,
    onPersonalDetailsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {}
) {
    val isBangla = LocalConfiguration.current.locales[0].language == "bn"
    val languageName = stringResource(if (isBangla) R.string.lang_bangla else R.string.lang_english)

    val roleLabel = stringResource(
        when (role.uppercase()) {
            "DOCTOR" -> R.string.role_doctor
            "ADMIN" -> R.string.role_admin
            else -> R.string.role_patient
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediColors.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_pill),
            color = MediColors.Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp
        )

        Spacer(Modifier.height(18.dp))

        // ---------- Header card ----------
        val cardShape = RoundedCornerShape(24.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(MediColors.Card)
                .border(1.dp, MediColors.Border, cardShape)
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (role.uppercase() == "DOCTOR") {
                DoctorAvatar(name = name, avatarSize = 92.dp, corner = 46.dp)
            } else {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(MediColors.TealLight, MediColors.TealDark))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialsOf(name),
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = name,
                color = MediColors.Text,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(text = email, color = MediColors.Muted, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MediColors.Pill)
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = roleLabel,
                    color = MediColors.Teal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        ProfileMenuRow(
            icon = IconKind.User,
            text = stringResource(R.string.personal_details),
            onClick = onPersonalDetailsClick
        )
        Spacer(Modifier.height(10.dp))
        ProfileMenuRow(
            icon = IconKind.Globe,
            text = stringResource(R.string.profile_language, languageName),
            onClick = onLanguageClick
        )
        Spacer(Modifier.height(10.dp))
        ProfileMenuRow(
            icon = IconKind.Help,
            text = stringResource(R.string.help_support),
            onClick = onHelpClick
        )

        Spacer(Modifier.height(16.dp))
        val logoutShape = RoundedCornerShape(18.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(logoutShape)
                .background(Color(0xFFFDE5E2))
                .clickable(onClick = onLogout),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediIcon(IconKind.Logout, tint = LogoutRed, iconSize = 20.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.log_out),
                color = LogoutRed,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(icon: IconKind, text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MediColors.Pill),
            contentAlignment = Alignment.Center
        ) {
            MediIcon(icon, tint = MediColors.Teal, iconSize = 20.dp)
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = text,
            color = MediColors.Text,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        MediIcon(IconKind.ChevronRight, tint = MediColors.Muted, iconSize = 20.dp)
    }
}

// "Nila Akter" -> "NA"
internal fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(
        name = "Nila Akter",
        email = "nila@example.com",
        role = "PATIENT",
        onLanguageClick = {},
        onLogout = {}
    )
}
