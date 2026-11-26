package com.mediqueue.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mediqueue.app.ui.theme.MediColors

/**
 * Line icons drawn from SVG path data (24 x 24 grid, 1.8 stroke).
 * No extra library needed.
 */
enum class IconKind(vararg val paths: String) {
    Home("M3 11.5L12 4l9 7.5", "M5.5 10v9.5a1 1 0 0 0 1 1H10v-6h4v6h3.5a1 1 0 0 0 1-1V10"),
    Stethoscope("M6 3H5v6a4 4 0 0 0 8 0V3h-1", "M9 13v2a5 5 0 0 0 10 0v-2.5", "M19 12.5a2 2 0 1 0 0-4 2 2 0 0 0 0 4z"),
    Calendar("M5 5h14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z", "M3 10h18", "M8 3v4", "M16 3v4", "M9 15.5l2 2 4-4"),
    User("M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8z", "M4 20.5a8 8 0 0 1 16 0"),
    Heart("M12 20.5C7 17 3.5 13.6 3.5 9.5A4.5 4.5 0 0 1 12 7.2 4.5 4.5 0 0 1 20.5 9.5c0 4.1-3.5 7.5-8.5 11z", "M7.5 12.5h2l1.5-2.5 2 4.5 1.5-2h2"),
    Brain("M12 4.5A3 3 0 0 0 6.5 6a3.2 3.2 0 0 0-1.8 4.6A3.4 3.4 0 0 0 6 16.6a3 3 0 0 0 6 2.9", "M12 4.5A3 3 0 0 1 17.5 6a3.2 3.2 0 0 1 1.8 4.6A3.4 3.4 0 0 1 18 16.6a3 3 0 0 1-6 2.9", "M12 4.5v15", "M9 10c1 0 2 .6 3 1.5", "M15 10c-1 0-2 .6-3 1.5"),
    Tooth("M7.5 3.5c-2.5 0-3.700 2-3.500 4.500.200 2.300 1.300 3.500 1.600 5.600.300 2 .600 7.400 2.400 7.400 1.600 0 1.500-4 2.600-5.400.500-.700 1-.700 1.400 0 1.100 1.400 1 5.400 2.600 5.400 1.800 0 2.100-5.400 2.400-7.400.300-2.100 1.400-3.300 1.600-5.600.200-2.500-1-4.500-3.500-4.500-1.800 0-2.700.8-4.200.8s-2.400-.8-4.200-.8z"),
    Eye("M2 12s3.600-6.500 10-6.500S22 12 22 12s-3.600 6.500-10 6.500S2 12 2 12z", "M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z"),
    Child("M12 20a8 8 0 1 0 0-16 8 8 0 0 0 0 16z", "M9 11h.01", "M15 11h.01", "M9 14.5c1.500 1.600 4.500 1.600 6 0", "M12 4c.8-1.200 2-1.500 3-1"),
    Bone("M17 10c.7-.7 1.69 0 2.5 0a2.5 2.5 0 1 0 0-5 .5.5 0 0 1-.5-.5 2.5 2.5 0 1 0-5 0c0 .81.7 1.8 0 2.5l-7 7c-.7.7-1.69 0-2.5 0a2.5 2.5 0 0 0 0 5c.28 0 .5.22.5.5a2.5 2.5 0 1 0 5 0c0-.81-.7-1.8 0-2.5Z"),
    Cross("M9 3h6v6h6v6h-6v6H9v-6H3V9h6z"),
    Pill("M10.500 20.500l-7-7a4.950 4.950 0 0 1 7-7l7 7a4.950 4.950 0 0 1-7 7z", "M8.500 8.500l7 7"),
    Clock("M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z", "M12 7v5l3 2"),
    Phone("M22 16.900v3a2 2 0 0 1-2.200 2 19.800 19.800 0 0 1-8.600-3.100 19.500 19.500 0 0 1-6-6A19.800 19.800 0 0 1 2.100 4.200 2 2 0 0 1 4.100 2h3a2 2 0 0 1 2 1.700c.1 1 .4 1.900.7 2.800a2 2 0 0 1-.5 2.100L8.100 9.900a16 16 0 0 0 6 6l1.300-1.300a2 2 0 0 1 2.100-.4c.9.300 1.800.6 2.800.7A2 2 0 0 1 22 16.900z"),
    Mail("M4 5h16a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1z", "M3 7l9 6 9-6"),
    Help("M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z", "M9.500 9.500a2.500 2.500 0 1 1 3.500 2.300c-.700.400-1 .9-1 1.700", "M12 17h.01"),
    Globe("M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z", "M3 12h18", "M12 3a14 14 0 0 1 0 18 14 14 0 0 1 0-18"),
    Logout("M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4", "M16 17l5-5-5-5", "M21 12H9"),
    ChevronRight("M9 6l6 6-6 6"),
    ChevronLeft("M15 6l-6 6 6 6"),
    Shield("M12 3l8 3v6c0 4.500-3.200 8-8 9-4.800-1-8-4.500-8-9V6l8-3z", "M9 12l2 2 4-4"),
    Bell("M6 16v-5a6 6 0 0 1 12 0v5l1.500 2h-15L6 16z", "M10 21h4"),
    Search("M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14z", "M20.500 20.500L16 16"),
    Plus("M12 5v14", "M5 12h14"),
    Check("M5 12.500l4.500 4.500L19 7.500"),
    Users("M16 20v-1.500a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4V20", "M9.500 11.500a3.500 3.500 0 1 0 0-7 3.500 3.500 0 0 0 0 7z", "M17 4.300a3.500 3.500 0 0 1 0 6.800", "M21 20v-1.500a4 4 0 0 0-3-3.900"),
    Document("M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8l-5-5z", "M14 3v5h5", "M9 13h6", "M9 17h6"),
    Building("M4 21V6l8-3 8 3v15", "M2 21h20", "M9 21v-5h6v5", "M12 7v4", "M10 9h4"),
    Info("M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z", "M12 11v5", "M12 8h.01"),
    Chat("M21 12a8 8 0 0 1-11.600 7.100L4 20.500l1.400-4.800A8 8 0 1 1 21 12z");
}

@Composable
fun MediIcon(
    icon: IconKind,
    modifier: Modifier = Modifier,
    tint: Color = MediColors.Text,
    iconSize: Dp = 24.dp,
    strokeWidth: Float = 1.8f
) {
    val parsed = remember(icon) {
        icon.paths.map { PathParser().parsePathString(it).toPath() }
    }
    Canvas(modifier = modifier.size(iconSize)) {
        val s = this.size.minDimension / 24f
        scale(s, s, pivot = Offset.Zero) {
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            parsed.forEach { drawPath(it, tint, style = stroke) }
        }
    }
}

// ---------- Department icons (coloured tile + line icon) ----------
private class DeptStyle(val kind: IconKind, val bg: Color, val fg: Color)

private fun deptStyle(icon: String?): DeptStyle {
    val key = icon.orEmpty().lowercase()
    return when {
        "heart" in key || "cardio" in key ->
            DeptStyle(IconKind.Heart, Color(0xFFFDE8EA), Color(0xFFD6455D))
        "brain" in key || "neuro" in key ->
            DeptStyle(IconKind.Brain, Color(0xFFEFE8FB), Color(0xFF7A4FC9))
        "tooth" in key || "dent" in key ->
            DeptStyle(IconKind.Tooth, Color(0xFFE4F1FB), Color(0xFF2F7DE1))
        "eye" in key || "ophth" in key ->
            DeptStyle(IconKind.Eye, Color(0xFFDFF5F2), Color(0xFF0A8A83))
        "child" in key || "pedia" in key || "paedia" in key ->
            DeptStyle(IconKind.Child, Color(0xFFFFF1D9), Color(0xFFD98A0F))
        "bone" in key || "ortho" in key ->
            DeptStyle(IconKind.Bone, Color(0xFFFDEBDD), Color(0xFFD2691E))
        else ->
            DeptStyle(IconKind.Cross, Color(0xFFE2F4F3), Color(0xFF0A8A83))
    }
}

@Composable
fun DepartmentIcon(
    icon: String?,
    modifier: Modifier = Modifier,
    boxSize: Dp = 44.dp,
    corner: Dp = 14.dp
) {
    val style = remember(icon) { deptStyle(icon) }
    Box(
        modifier = modifier
            .size(boxSize)
            .clip(RoundedCornerShape(corner))
            .background(style.bg),
        contentAlignment = Alignment.Center
    ) {
        MediIcon(style.kind, tint = style.fg, iconSize = boxSize * 0.52f)
    }
}
