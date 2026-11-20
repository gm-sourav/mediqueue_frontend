package com.mediqueue.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mediqueue.app.R

enum class AdminTab { Home, Doctors, Departments, Profile }

@Composable
fun AdminBottomBar(
    selected: AdminTab?,
    onSelect: (AdminTab) -> Unit
) {
    val tabs = AdminTab.values()
    val items = tabs.map { tab ->
        BarItem(
            icon = when (tab) {
                AdminTab.Home -> IconKind.Home
                AdminTab.Doctors -> IconKind.Stethoscope
                AdminTab.Departments -> IconKind.Building
                AdminTab.Profile -> IconKind.User
            },
            label = stringResource(
                when (tab) {
                    AdminTab.Home -> R.string.tab_home
                    AdminTab.Doctors -> R.string.tab_doctors
                    AdminTab.Departments -> R.string.admin_tab_departments
                    AdminTab.Profile -> R.string.tab_profile
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
