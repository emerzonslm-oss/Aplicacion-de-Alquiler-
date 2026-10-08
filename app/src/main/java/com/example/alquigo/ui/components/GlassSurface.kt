package com.example.alquigo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

fun Modifier.glassSurface(
    hazeState: HazeState? = null,
    shape: Shape = RoundedCornerShape(0.dp),
    tintAlpha: Float = 0.92f,
    borderAlpha: Float = 0.60f
): Modifier {
    val base = this.clip(shape)

    val explicitHazeStyle = HazeStyle(
        backgroundColor = Color.White,
        tint = HazeTint(Color.White.copy(alpha = tintAlpha)),
        blurRadius = 20.dp,
        noiseFactor = 0f
    )

    val glassEffect = if (hazeState != null) {
        base.hazeEffect(
            state = hazeState,
            style = explicitHazeStyle
        )
    } else {
        base.background(Color.White.copy(alpha = tintAlpha))
    }

    return glassEffect
}
