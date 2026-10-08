package com.example.alquigo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AirbnbFontFamily = FontFamily.SansSerif

val Typography = Typography(
    // Títulos de pantalla (32sp Bold)
    headlineLarge = TextStyle(
        fontFamily = AirbnbFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = TextPrimary
    ),
    // Títulos de sección (22sp SemiBold)
    titleMedium = TextStyle(
        fontFamily = AirbnbFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = AirbnbFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = TextPrimary
    ),
    // Título de tarjetas (16sp Medium)
    bodyLarge = TextStyle(
        fontFamily = AirbnbFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = TextPrimary
    ),
    // Texto secundario (15sp Regular #6A6A6A)
    bodyMedium = TextStyle(
        fontFamily = AirbnbFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TextSecondary
    ),
    // Chips y etiquetas (16sp Medium / 12sp)
    labelMedium = TextStyle(
        fontFamily = AirbnbFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = AirbnbFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextSecondary
    )
)
