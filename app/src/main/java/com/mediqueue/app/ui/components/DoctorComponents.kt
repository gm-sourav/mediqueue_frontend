package com.mediqueue.app.ui.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.theme.MediColors

enum class DoctorTab { Today, Slots, Queue, Profile }

@Composable
fun DoctorBottomBar(
    selected: DoctorTab?,
    onSelect: (DoctorTab) -> Unit
) {
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
            DoctorTab.values().forEach { tab ->
                val active = tab == selected
                val color = if (active) MediColors.Teal else MediColors.Muted
                val glyph = when (tab) {
                    DoctorTab.Today -> "⌂"
                    DoctorTab.Slots -> "≡"
                    DoctorTab.Queue -> "◉"
                    DoctorTab.Profile -> "○"
                }
                val label = stringResource(
                    when (tab) {
                        DoctorTab.Today -> R.string.doc_tab_today
                        DoctorTab.Slots -> R.string.doc_tab_slots
                        DoctorTab.Queue -> R.string.doc_tab_queue
                        DoctorTab.Profile -> R.string.tab_profile
                    }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(tab) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(glyph, color = color, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}