package com.mediqueue.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediqueue.app.ui.theme.MediColors

@Composable
fun MediPill(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    letterSpacing: TextUnit = TextUnit.Unspecified
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MediColors.Pill)
            .padding(horizontal = 15.dp, vertical = 11.dp)
    ) {
        Text(
            text = text,
            color = MediColors.Teal,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = letterSpacing
        )
    }
}

@Composable
fun MediFieldCard(
    label: String,
    modifier: Modifier = Modifier,
    error: Boolean = false,
    errorText: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(15.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MediColors.Card)
            .border(1.dp, if (error) MediColors.Danger else MediColors.Border, shape)
            .padding(horizontal = 14.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = label,
            color = MediColors.Text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        content()
        if (error && errorText != null) {
            Text(text = errorText, color = MediColors.Danger, fontSize = 11.sp)
        }
    }
}

@Composable
fun MediInput(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    password: Boolean = false
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        textStyle = TextStyle(color = MediColors.Text, fontSize = 13.sp),
        cursorBrush = SolidColor(MediColors.Teal),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text(text = hint, color = MediColors.Muted, fontSize = 13.sp)
                }
                inner()
            }
        }
    )
}