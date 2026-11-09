package com.example.alquigo.ui.ejemplo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MiNuevaPantalla(
    onVolver: () -> Unit // Callback para regresar atrás
) {
    // 1. Estado para guardar el texto ingresado en el Input
    var textoIngresado by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón para volver atrás
        Button(onClick = onVolver) {
            Text("← Volver")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Esta es la Nueva Pantalla",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. DIBUJAR EL INPUT (Caja de Texto)
        OutlinedTextField(
            value = textoIngresado,
            onValueChange = { textoIngresado = it },
            label = { Text("Escribe algo aquí...") },
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Mostrar en tiempo real lo escrito
        if (textoIngresado.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Ingresado: $textoIngresado",
                fontWeight = FontWeight.Bold
            )
        }
    }
}