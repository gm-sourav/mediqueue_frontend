package com.mediqueue.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.theme.MediColors
import java.text.NumberFormat

// ---------- Number (5 -> ৫ jokhon Bangla) ----------
@Composable
fun localizedNumber(value: Int): String {
    val locale = LocalConfiguration.current.locales[0]
    return NumberFormat.getInstance(locale).format(value)
}

@Composable
fun localizedDecimal(value: Double): String {
    val locale = LocalConfiguration.current.locales[0]
    val nf = NumberFormat.getInstance(locale)
    nf.minimumFractionDigits = 1
    nf.maximumFractionDigits = 1
    return nf.format(value)
}

// "Nila Akter" -> "NA"
fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

// ---------- Badges ----------
val BadgeGreenBg = Color(0xFF1D4034)
val BadgeGreenText = Color(0xFF55D6A0)
private val BadgeAmberBg = Color(0xFF3A3014)
private val BadgeAmberText = Color(0xFFF5C451)
private val BadgeBlueBg = Color(0xFF1C2F45)
private val BadgeBlueText = Color(0xFF7DB8F0)
private val BadgeRedBg = Color(0xFF3A1F1C)
private val BadgeRedText = Color(0xFFFF8F7A)

@Composable
fun MediBadge(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// status: PENDING, CONFIRMED, COMPLETED, CANCELLED, REJECTED
@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (status.uppercase()) {
        "CONFIRMED" -> Triple(stringResource(R.string.status_confirmed), BadgeGreenBg, BadgeGreenText)
        "COMPLETED" -> Triple(stringResource(R.string.status_completed), BadgeBlueBg, BadgeBlueText)
        "CANCELLED" -> Triple(stringResource(R.string.status_cancelled), BadgeRedBg, BadgeRedText)
        "REJECTED" -> Triple(stringResource(R.string.status_rejected), BadgeRedBg, BadgeRedText)
        else -> Triple(stringResource(R.string.status_pending), BadgeAmberBg, BadgeAmberText)
    }
    MediBadge(text = label, bg = bg, fg = fg, modifier = modifier)
}

// ---------- Bottom bar ----------
enum class PatientTab { Home, Doctors, Bookings, Profile }

@Composable
fun PatientBottomBar(selected: PatientTab?, onSelect: (PatientTab) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MediColors.Bg)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(MediColors.Border)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            TabItem("⌂", stringResource(R.string.tab_home), selected == PatientTab.Home) {
                onSelect(PatientTab.Home)
            }
            TabItem("◉", stringResource(R.string.tab_doctors), selected == PatientTab.Doctors) {
                onSelect(PatientTab.Doctors)
            }
            TabItem("≡", stringResource(R.string.tab_bookings), selected == PatientTab.Bookings) {
                onSelect(PatientTab.Bookings)
            }
            TabItem("○", stringResource(R.string.tab_profile), selected == PatientTab.Profile) {
                onSelect(PatientTab.Profile)
            }
        }
    }
}

@Composable
private fun RowScope.TabItem(glyph: String, label: String, active: Boolean, onClick: () -> Unit) {
    val color = if (active) MediColors.Teal else MediColors.Muted
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = glyph, color = color, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = label, color = color, fontSize = 10.5.sp)
    }
}