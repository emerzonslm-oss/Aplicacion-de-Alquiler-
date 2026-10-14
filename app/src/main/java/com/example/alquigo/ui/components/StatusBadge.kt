package com.example.alquigo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alquigo.ui.theme.PaidStatusBg
import com.example.alquigo.ui.theme.PaidStatusText
import com.example.alquigo.ui.theme.PendingStatusBg
import com.example.alquigo.ui.theme.PendingStatusText

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, labelText) = when (status.uppercase()) {
        "PAGADO", "COMPLETADO" -> Triple(PaidStatusBg, PaidStatusText, "Pagado")
        "ACTIVO" -> Triple(PaidStatusBg, PaidStatusText, "Activo")
        "DISPONIBLE" -> Triple(PaidStatusBg, PaidStatusText, "Disponible")
        "OCUPADA" -> Triple(PendingStatusBg, PendingStatusText, "Ocupada")
        "PENDIENTE" -> Triple(PendingStatusBg, PendingStatusText, "Pendiente")
        else -> Triple(PendingStatusBg, PendingStatusText, status.lowercase().replaceFirstChar { it.uppercase() })
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = labelText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
