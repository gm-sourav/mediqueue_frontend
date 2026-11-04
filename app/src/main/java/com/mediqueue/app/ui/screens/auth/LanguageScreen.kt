package com.mediqueue.app.ui.screens.auth

import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.mediqueue.app.R
import com.mediqueue.app.ui.theme.MediColors

private val PreviewCard = Color(0xFF1D3530)
private val BadgeBg = Color(0xFF1D4034)
private val BadgeText = Color(0xFF55D6A0)

@Composable
fun LanguageScreen(onSaved: () -> Unit) {
    // Ekhon app e kon language chalu ache
    val appLang = LocalConfiguration.current.locales[0].language
    val currentTag = if (appLang == "bn") "bn" else "en"

    // User je language ta tap korbe (save er age)
    var pending by rememberSaveable { mutableStateOf(currentTag) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediColors.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top pill (full width)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MediColors.Pill)
                .padding(horizontal = 15.dp, vertical = 11.dp)
        ) {
            Text(
                text = stringResource(R.string.language_pill),
                color = MediColors.Teal,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.language_title),
            color = MediColors.Text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.language_subtitle),
            color = MediColors.Muted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(18.dp))
        LanguageOptionCard(
            title = stringResource(R.string.lang_english),
            description = stringResource(R.string.lang_english_desc),
            isApplied = currentTag == "en",
            isPending = pending == "en",
            onClick = { pending = "en" }
        )
        Spacer(Modifier.height(10.dp))
        LanguageOptionCard(
            title = stringResource(R.string.lang_bangla),
            description = stringResource(R.string.lang_bangla_desc),
            isApplied = currentTag == "bn",
            isPending = pending == "bn",
            onClick = { pending = "bn" }
        )

        Spacer(Modifier.height(14.dp))
        // Live preview: pending language er sample text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(PreviewCard)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.live_preview),
                color = MediColors.Muted,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = if (pending == "bn") {
                    "আপনার পরবর্তী অ্যাপয়েন্টমেন্ট\nসকাল ১০:৩০ · টোকেন ৫"
                } else {
                    "Your next appointment\n10:30 AM · Token 5"
                },
                color = MediColors.Text,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 26.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.preview_note),
                color = MediColors.Muted,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (pending != currentTag) {
                    // Device e save hoy (manifest er autoStoreLocales er karone)
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(pending)
                    )
                }
                onSaved()
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MediColors.Teal,
                contentColor = MediColors.OnTeal
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = stringResource(R.string.save_language), fontSize = 14.sp)
        }
    }
}

@Composable
private fun LanguageOptionCard(
    title: String,
    description: String,
    isApplied: Boolean,
    isPending: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(15.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, if (isPending) MediColors.Teal else MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MediColors.Text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(text = description, color = MediColors.Muted, fontSize = 12.sp)
        }

        if (isApplied) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(BadgeBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.selected),
                    color = BadgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(1.5.dp, if (isPending) MediColors.Teal else MediColors.Muted, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isPending) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(MediColors.Teal, CircleShape)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LanguageScreenPreview() {
    LanguageScreen(onSaved = {})
}