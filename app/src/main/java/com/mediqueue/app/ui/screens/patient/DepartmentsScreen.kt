package com.mediqueue.app.ui.screens.patient

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.R
import com.mediqueue.app.ui.components.MediInput
import com.mediqueue.app.ui.components.PatientBottomBar
import com.mediqueue.app.ui.components.PatientTab
import com.mediqueue.app.ui.components.localizedNumber
import com.mediqueue.app.ui.theme.MediColors

private val IconBoxBg = Color(0xFF1D3530)

data class Department(
    val id: Long,
    val name: String,
    val doctorCount: Int,
    val icon: String
)

@Composable
fun DepartmentsScreen(
    departments: List<Department>,
    onDepartmentClick: (Department) -> Unit,
    onTabSelected: (PatientTab) -> Unit,
    selectedTab: PatientTab = PatientTab.Home
) {
    var query by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val filtered = remember(query, departments) {
        val q = query.trim()
        if (q.isEmpty()) departments
        else departments.filter { it.name.contains(q, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediColors.Bg)
    ) {
        // Header
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.find_care),
                        color = MediColors.Muted,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.departments_title),
                        color = MediColors.Text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(IconBoxBg)
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⌕", color = MediColors.Teal, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(18.dp))

            // Search bar
            val searchShape = RoundedCornerShape(14.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(searchShape)
                    .background(MediColors.Card)
                    .border(1.dp, MediColors.Border, searchShape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⌕", color = MediColors.Teal, fontSize = 14.sp)
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    MediInput(
                        value = query,
                        onValueChange = { query = it },
                        hint = stringResource(R.string.search_hint),
                        modifier = Modifier.focusRequester(focusRequester),
                        imeAction = ImeAction.Search
                    )
                }
            }
        }

        // Department grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filtered.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = stringResource(R.string.no_departments),
                        color = MediColors.Muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
            items(filtered, key = { it.id }) { dept ->
                DepartmentCard(dept) { onDepartmentClick(dept) }
            }
        }

        PatientBottomBar(selected = selectedTab, onSelect = onTabSelected)
    }
}

@Composable
private fun DepartmentCard(department: Department, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, MediColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(IconBoxBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = department.icon, color = MediColors.Teal, fontSize = 15.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = department.name,
            color = MediColors.Text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.doctors_count, localizedNumber(department.doctorCount)),
            color = MediColors.Muted,
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DepartmentsPreview() {
    DepartmentsScreen(
        departments = listOf(
            Department(1, "Cardiology", 12, "♡"),
            Department(2, "Neurology", 8, "✳"),
            Department(3, "Dental", 10, "+"),
            Department(4, "Eye", 6, "◉"),
            Department(5, "Pediatrics", 9, "⌂"),
            Department(6, "Orthopedics", 7, "+")
        ),
        onDepartmentClick = {},
        onTabSelected = {}
    )
}