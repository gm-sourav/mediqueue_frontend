package com.mediqueue.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.ui.theme.MediColors

class BarItem(val icon: IconKind, val label: String)


private object BarMemory {
    var last: Int = -1
}


@Composable
fun MediBottomBar(
    items: List<BarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val startIndex = remember {
        if (BarMemory.last in items.indices) BarMemory.last else selectedIndex
    }
    SideEffect {
        if (selectedIndex >= 0) BarMemory.last = selectedIndex
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MediColors.Card)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MediColors.Border)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            items.forEachIndexed { index, item ->
                BarTab(
                    item = item,
                    active = index == selectedIndex,
                    startActive = index == startIndex,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(index) }
                )
            }
        }
    }
}

@Composable
private fun BarTab(
    item: BarItem,
    active: Boolean,
    startActive: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {

    val progress = remember { Animatable(if (startActive) 1f else 0f) }
    val pop = remember { Animatable(if (startActive) 1.12f else 1f) }

    LaunchedEffect(active) {
        progress.animateTo(if (active) 1f else 0f, tween(280))
    }
    LaunchedEffect(active) {
        pop.animateTo(
            if (active) 1.12f else 1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    val tint = lerp(MediColors.Muted, MediColors.Teal, progress.value)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(MediColors.Pill.copy(alpha = progress.value)),
            contentAlignment = Alignment.Center
        ) {
            MediIcon(
                icon = item.icon,
                tint = tint,
                iconSize = 22.dp,
                strokeWidth = 1.8f + 0.3f * progress.value,
                modifier = Modifier.graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                }
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.label,
            color = tint,
            fontSize = 10.5.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}