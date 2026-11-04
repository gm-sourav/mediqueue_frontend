package com.mediqueue.app.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.theme.MediColors

private val ProgressTrack = Color(0xFF2A4A44)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0.1f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 1800))
        onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediColors.Bg)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(MediColors.Teal),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = MediColors.OnTeal,
                fontSize = 30.sp,
                fontWeight = FontWeight.Light
            )
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.app_name),
            color = MediColors.Text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.splash_tagline),
            color = MediColors.Muted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(60.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ProgressTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.value)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(MediColors.Teal)
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.splash_preparing),
            color = MediColors.Muted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SplashScreenPreview() {
    SplashScreen(onFinished = {})
}