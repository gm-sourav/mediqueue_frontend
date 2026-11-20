package com.mediqueue.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mediqueue.app.R

enum class DoctorTab { Today, Slots, Queue, Profile }

@Composable
fun DoctorBottomBar(
    selected: DoctorTab?,
    onSelect: (DoctorTab) -> Unit
) {
    val tabs = DoctorTab.values()
    val items = tabs.map { tab ->
        BarItem(
            icon = when (tab) {
                DoctorTab.Today -> IconKind.Home
                DoctorTab.Slots -> IconKind.Clock
                DoctorTab.Queue -> IconKind.Users
                DoctorTab.Profile -> IconKind.User
            },
            label = stringResource(
                when (tab) {
                    DoctorTab.Today -> R.string.doc_tab_today
                    DoctorTab.Slots -> R.string.doc_tab_slots
                    DoctorTab.Queue -> R.string.doc_tab_queue
                    DoctorTab.Profile -> R.string.tab_profile
                }
            )
        )
    }
    MediBottomBar(
        items = items,
        selectedIndex = selected?.ordinal ?: -1,
        onSelect = { onSelect(tabs[it]) }
    )
}
