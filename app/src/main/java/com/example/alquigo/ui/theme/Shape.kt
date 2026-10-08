package com.example.alquigo.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val CardShape = RoundedCornerShape(24.dp)
val InputShape = RoundedCornerShape(12.dp)
val ButtonShape = RoundedCornerShape(12.dp)
val PillShape = CircleShape
val BottomSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

val Shapes = Shapes(
    small = InputShape,
    medium = CardShape,
    large = BottomSheetShape
)
