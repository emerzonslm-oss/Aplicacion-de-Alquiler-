package com.example.alquigo.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alquigo.ui.theme.AirbnbPrimary
import com.example.alquigo.ui.theme.CardShape
import com.example.alquigo.ui.theme.TextPrimary
import com.example.alquigo.ui.theme.TextSecondary

@Composable
fun ConfirmDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Confirmar",
    cancelText: String = "Cancelar",
    isDestructive: Boolean = false
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = CardShape,
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = message,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDestructive) AirbnbPrimary else TextPrimary
                    )
                ) {
                    Text(text = confirmText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text(text = cancelText, color = TextPrimary)
                }
            }
        )
    }
}
