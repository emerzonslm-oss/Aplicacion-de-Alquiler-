package com.example.alquigo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alquigo.ui.theme.TextPrimary

@Composable
fun GlassChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emoji = when (text) {
        "Casa" -> "🏠"
        "Apartamento" -> "🏢"
        "Habitación" -> "🛏️"
        "Local Comercial" -> "🏪"
        else -> "🌐"
    }

    val chipModifier = if (isSelected) {
        modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFE4E4E4))
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
    } else {
        modifier
            .height(48.dp)
            .shadow(elevation = 4.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
    }

    Box(
        modifier = chipModifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}
