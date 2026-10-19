package com.example.alquigo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alquigo.ui.theme.AirbnbPrimary
import com.example.alquigo.ui.theme.BorderLight
import com.example.alquigo.ui.theme.TextPrimary

@Composable
fun GlassChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (text) {
        "Favoritos" -> if (isSelected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
        "Casa" -> Icons.Outlined.Home
        "Apartamento" -> Icons.Outlined.Apartment
        "Habitación" -> Icons.Outlined.Bed
        "Local Comercial" -> Icons.Outlined.Storefront
        "Todos" -> Icons.Outlined.AutoAwesome
        else -> Icons.Outlined.HomeWork
    }

    val chipModifier = if (isSelected) {
        modifier
            .height(40.dp)
            .shadow(elevation = 2.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(AirbnbPrimary)
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
    } else {
        modifier
            .height(40.dp)
            .shadow(elevation = 1.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, BorderLight, CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
    }

    val contentColor = if (isSelected) Color.White else TextPrimary
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Box(
        modifier = chipModifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = fontWeight,
                color = contentColor
            )
        }
    }
}
