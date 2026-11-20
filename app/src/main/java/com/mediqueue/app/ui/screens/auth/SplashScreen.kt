package com.mediqueue.app.ui.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import kotlinx.coroutines.launch

private val SplashTop = Color(0xFF0FA79E)
private val SplashMid = Color(0xFF087A78)
private val SplashBottom = Color(0xFF05505A)

/**
 * Splash: teal gradient, logo tile with soft ripples, and a heartbeat (ECG) line
 * that draws itself while the app gets ready.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val appear = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.7f) }

    val infinite = rememberInfiniteTransition(label = "ripple")
    val ripple by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleValue"
    )

    LaunchedEffect(Unit) {
        launch { appear.animateTo(1f, tween(800)) }
        launch { logoScale.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        progress.animateTo(1f, tween(2300, easing = FastOutSlowInEasing))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SplashTop, SplashMid, SplashBottom)))
    ) {
        // soft decorative circles in the corners
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.07f),
                radius = size.width * 0.75f,
                center = Offset(size.width, 0f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = size.width * 0.6f,
                center = Offset(0f, size.height)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .alpha(appear.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (k in 0..1) {
                        val p = (ripple + k * 0.5f) % 1f
                        drawCircle(
                            color = Color.White.copy(alpha = (1f - p) * 0.30f),
                            radius = (size.minDimension / 2f) * (0.55f + 0.45f * p),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(logoScale.value)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_logo_mark_teal),
                        contentDescription = null,
                        modifier = Modifier.size(84.dp)
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))
            // Heartbeat line: draws from left to right while loading
            Canvas(
                modifier = Modifier
                    .width(240.dp)
                    .height(48.dp)
            ) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(0f, h * 0.5f)
                    lineTo(w * 0.22f, h * 0.5f)
                    lineTo(w * 0.27f, h * 0.36f)
                    lineTo(w * 0.31f, h * 0.5f)
                    lineTo(w * 0.38f, h * 0.5f)
                    lineTo(w * 0.43f, h * 0.04f)
                    lineTo(w * 0.50f, h * 0.96f)
                    lineTo(w * 0.55f, h * 0.5f)
                    lineTo(w * 0.63f, h * 0.5f)
                    lineTo(w * 0.68f, h * 0.34f)
                    lineTo(w * 0.74f, h * 0.5f)
                    lineTo(w, h * 0.5f)
                }
                val measure = PathMeasure()
                measure.setPath(path, false)
                val segment = Path()
                measure.getSegment(0f, measure.length * progress.value, segment, true)
                drawPath(
                    path = segment,
                    color = Color.White,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.splash_preparing),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SplashScreenPreview() {
    SplashScreen(onFinished = {})
}
