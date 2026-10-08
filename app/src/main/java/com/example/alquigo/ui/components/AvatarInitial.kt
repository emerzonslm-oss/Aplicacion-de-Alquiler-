package com.example.alquigo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alquigo.ui.theme.AvatarBackground
import com.example.alquigo.ui.theme.AvatarText

@Composable
fun AvatarInitial(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    fontSize: Int = 36
) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "A"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(AvatarBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = AvatarText
        )
    }
}
