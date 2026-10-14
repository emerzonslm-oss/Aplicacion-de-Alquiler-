package com.example.alquigo.ui.property

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alquigo.data.model.Property
import com.example.alquigo.ui.components.*
import com.example.alquigo.ui.theme.*

@Composable
fun PropertyListScreen(
    viewModel: PropertyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProperty: (String) -> Unit,
    onNavigateToPropertyDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState

    val filters = listOf("Todos", "Casa", "Apartamento", "Habitación", "Local Comercial")

    var propertyToDelete by remember { mutableStateOf<Property?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var blockedMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProperties()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .statusBarsPadding()
    ) {
        // Cabecera superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onNavigateBack,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("← Volver", fontSize = 16.sp, color = AirbnbPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Propiedades",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Fila de Chips de Categorías
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            items(filters) { filter ->
                GlassChip(
                    text = filter,
                    isSelected = uiState.selectedFilter == filter,
                    onClick = { viewModel.loadProperties(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AirbnbPrimary)
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else if (uiState.properties.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏢", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No hay propiedades encontradas.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(uiState.properties, key = { it.id }) { property ->
                    Column {
                        PropertyCard(
                            property = property,
                            onClick = { onNavigateToPropertyDetail(property.id) },
                            estado = if (property.estado.equals("OCUPADA", ignoreCase = true)) "Ocupada" else "Disponible"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onNavigateToEditProperty(property.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Editar",
                                    tint = TextPrimary
                                )
                            }
                            IconButton(onClick = {
                                propertyToDelete = property
                                showDeleteConfirmationDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Eliminar",
                                    tint = AirbnbPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmación de Borrado
    ConfirmDialog(
        show = showDeleteConfirmationDialog && propertyToDelete != null,
        onDismiss = {
            showDeleteConfirmationDialog = false
            propertyToDelete = null
        },
        onConfirm = {
            val propId = propertyToDelete?.id ?: ""
            showDeleteConfirmationDialog = false
            propertyToDelete = null

            viewModel.deleteProperty(
                propertyId = propId,
                onBlocked = { message ->
                    blockedMessage = message
                },
                onSuccess = {
                    Toast.makeText(context, "Propiedad eliminada correctamente", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                }
            )
        },
        title = "Confirmar Eliminación",
        message = "¿Estás seguro de que deseas eliminar la propiedad \"${propertyToDelete?.titulo}\"? Esta acción no se puede deshacer.",
        confirmText = "Eliminar",
        isDestructive = true
    )

    // Alerta de Bloqueo por Contrato
    if (blockedMessage != null) {
        AlertDialog(
            onDismissRequest = { blockedMessage = null },
            shape = CardShape,
            title = { Text("Operación Bloqueada", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text(blockedMessage!!, color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { blockedMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = TextPrimary)
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
