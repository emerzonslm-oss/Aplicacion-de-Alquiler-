package com.example.alquigo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alquigo.ui.theme.AirbnbActive
import com.example.alquigo.ui.theme.BorderLight
import com.example.alquigo.ui.theme.TextSecondary
import dev.chrisbanes.haze.HazeState

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("home", "Explora", Icons.Outlined.Home),
    BottomNavItem("property_list", "Propiedades", Icons.Outlined.HomeWork),
    BottomNavItem("tenants", "Inquilinos", Icons.Outlined.Group),
    BottomNavItem("profile", "Perfil", Icons.Outlined.Person)
)

@Composable
fun GlassBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(
                hazeState = hazeState,
                shape = RectangleShape,
                tintAlpha = 0.92f
            )
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = BorderLight, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route
                val tint = if (isSelected) AirbnbActive else TextSecondary
                val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onNavigate(item.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        fontSize = 12.sp,
                        fontWeight = fontWeight,
                        color = tint
                    )
                }
            }
        }
    }
}
