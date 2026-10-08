package com.example.alquigo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alquigo.ui.theme.AirbnbPrimary

@Composable
fun AlquiLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    showText: Boolean = true,
    fontSize: Int = 24
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Home,
            contentDescription = "AlquiGo Logo",
            tint = AirbnbPrimary,
            modifier = Modifier.size(iconSize)
        )
        if (showText) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "AlquiGo",
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                color = AirbnbPrimary
            )
        }
    }
}
