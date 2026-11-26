package com.mediqueue.app.ui.screens.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.IconKind
import com.mediqueue.app.ui.components.MediIcon
import com.mediqueue.app.ui.theme.MediColors

@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val phone = stringResource(R.string.hs_phone_value)
    val email = stringResource(R.string.hs_email_value)
    val version = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    val faqs = listOf(
        R.string.faq_q1 to R.string.faq_a1,
        R.string.faq_q2 to R.string.faq_a2,
        R.string.faq_q3 to R.string.faq_a3,
        R.string.faq_q4 to R.string.faq_a4,
        R.string.faq_q5 to R.string.faq_a5,
        R.string.faq_q6 to R.string.faq_a6
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
            text = "‹ " + stringResource(R.string.back),
            color = MediColors.Text.copy(alpha = 0.85f),
            fontSize = 13.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onBack)
                .padding(vertical = 6.dp, horizontal = 2.dp)
        )

        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.help_support),
            color = MediColors.Text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.hs_subtitle),
            color = MediColors.Muted,
            fontSize = 12.5.sp
        )

        // ---------- Contact ----------
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ContactCard(
                icon = IconKind.Phone,
                title = stringResource(R.string.hs_call),
                value = phone,
                modifier = Modifier.weight(1f)
            ) {
                openIntent(
                    context,
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.filter { it.isDigit() || it == '+' }))
                )
            }
            ContactCard(
                icon = IconKind.Mail,
                title = stringResource(R.string.hs_email),
                value = email,
                modifier = Modifier.weight(1f)
            ) {
                openIntent(context, Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MediIcon(IconKind.Clock, tint = MediColors.Muted, iconSize = 15.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.hs_hours),
                color = MediColors.Muted,
                fontSize = 11.5.sp
            )
        }

        // ---------- FAQ ----------
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.hs_faq_title),
            color = MediColors.Text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        faqs.forEachIndexed { index, (q, a) ->
            if (index > 0) Spacer(Modifier.height(10.dp))
            FaqItem(question = stringResource(q), answer = stringResource(a))
        }

        Spacer(Modifier.height(26.dp))
        Text(
            text = stringResource(R.string.hs_version, version),
            color = MediColors.Muted,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(8.dp))
    }
}

private fun openIntent(context: android.content.Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // No dialer / mail app on this device (for example an emulator): ignore.
    }
}

@Composable
private fun ContactCard(
    icon: IconKind,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(MediColors.Pill),
            contentAlignment = Alignment.Center
        ) {
            MediIcon(icon, tint = MediColors.Teal, iconSize = 21.dp)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            color = MediColors.Text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            color = MediColors.Muted,
            fontSize = 11.sp,
            maxLines = 2
        )
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by rememberSaveable(question) { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 90f else 0f, label = "faqArrow")
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                color = MediColors.Text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            MediIcon(
                IconKind.ChevronRight,
                tint = MediColors.Muted,
                iconSize = 20.dp,
                modifier = Modifier.graphicsLayer { rotationZ = rotation }
            )
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = answer,
                color = MediColors.Muted,
                fontSize = 12.5.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HelpSupportPreview() {
    HelpSupportScreen(onBack = {})
}
