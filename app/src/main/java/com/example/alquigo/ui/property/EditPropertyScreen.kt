package com.example.alquigo.ui.property

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.alquigo.data.model.Property
import com.example.alquigo.ui.components.AlquiTextField
import com.example.alquigo.ui.components.PrimaryButton
import com.example.alquigo.ui.components.copyUriToInternalStorage
import com.example.alquigo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPropertyScreen(
    propertyId: String,
    viewModel: PropertyViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState

    var currentProperty by remember { mutableStateOf<Property?>(null) }
    var isDataLoaded by remember { mutableStateOf(false) }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Casa") }
    var persistentImageUriString by remember { mutableStateOf("") }

    var expandedTipoDropdown by remember { mutableStateOf(false) }
    val tiposDisponibles = listOf("Casa", "Apartamento", "Habitación", "Local Comercial")

    LaunchedEffect(propertyId) {
        viewModel.getPropertyById(propertyId) { prop ->
            if (prop != null) {
                currentProperty = prop
                titulo = prop.titulo
                descripcion = prop.descripcion
                precio = if (prop.precio > 0) prop.precio.toInt().toString() else ""
                direccion = prop.direccion
                tipo = prop.tipo.ifBlank { "Casa" }
                persistentImageUriString = prop.imagenUri
                isDataLoaded = true
            } else {
                Toast.makeText(context, "No se encontró la propiedad", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedLocalUri = copyUriToInternalStorage(context, uri)
            persistentImageUriString = savedLocalUri
            Toast.makeText(context, "Nueva imagen guardada correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Propiedad", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Volver", color = AirbnbPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!isDataLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AirbnbPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Foto de la propiedad
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable(enabled = !uiState.isLoading) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (persistentImageUriString.isNotBlank()) {
                            AsyncImage(
                                model = persistentImageUriString,
                                contentDescription = "Foto de la propiedad",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("📷", style = MaterialTheme.typography.displayMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tocar para cambiar imagen",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                AlquiTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = "Título de la Propiedad",
                    enabled = !uiState.isLoading
                )

                AlquiTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = "Descripción",
                    enabled = !uiState.isLoading,
                    singleLine = false,
                    minLines = 3
                )

                AlquiTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = "Precio por Mes (S/)",
                    enabled = !uiState.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                AlquiTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = "Dirección Completa",
                    enabled = !uiState.isLoading
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    AlquiTextField(
                        value = tipo,
                        onValueChange = {},
                        readOnly = true,
                        label = "Tipo de Propiedad",
                        trailingIcon = {
                            Text(
                                text = "▼ ",
                                modifier = Modifier.clickable(enabled = !uiState.isLoading) { expandedTipoDropdown = true }
                            )
                        },
                        enabled = !uiState.isLoading
                    )

                    DropdownMenu(
                        expanded = expandedTipoDropdown,
                        onDismissRequest = { expandedTipoDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        tiposDisponibles.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    tipo = item
                                    expandedTipoDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                PrimaryButton(
                    text = "Guardar Cambios",
                    onClick = {
                        val precioDouble = precio.toDoubleOrNull()

                        if (titulo.isBlank() || descripcion.isBlank() || precio.isBlank() || direccion.isBlank()) {
                            Toast.makeText(context, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                        } else if (precioDouble == null || precioDouble <= 0) {
                            Toast.makeText(context, "El precio por mes debe ser un número válido mayor a cero", Toast.LENGTH_SHORT).show()
                        } else {
                            val propiedadActualizada = currentProperty?.copy(
                                titulo = titulo,
                                descripcion = descripcion,
                                precio = precioDouble,
                                direccion = direccion,
                                tipo = tipo,
                                imagenUri = persistentImageUriString
                            ) ?: Property(
                                id = propertyId,
                                titulo = titulo,
                                descripcion = descripcion,
                                precio = precioDouble,
                                direccion = direccion,
                                tipo = tipo,
                                imagenUri = persistentImageUriString
                            )

                            viewModel.updateProperty(
                                property = propiedadActualizada,
                                onSuccess = {
                                    Toast.makeText(context, "Propiedad actualizada exitosamente", Toast.LENGTH_LONG).show()
                                    onNavigateBack()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, "Error al actualizar: $msg", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    isLoading = uiState.isLoading,
                    enabled = !uiState.isLoading
                )
            }
        }
    }
}
