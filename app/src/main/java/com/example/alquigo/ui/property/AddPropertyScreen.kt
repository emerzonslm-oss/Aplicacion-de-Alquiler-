package com.example.alquigo.ui.property

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import com.example.alquigo.data.model.Property
import com.example.alquigo.data.repository.PropertyRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { PropertyRepository() }
    
    // Estados locales para el formulario
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Casa") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var expandedTipoDropdown by remember { mutableStateOf(false) }
    val tiposDisponibles = listOf("Casa", "Apartamento", "Habitación", "Local Comercial")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            Toast.makeText(context, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Propiedad") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Volver", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de la foto simulada de la propiedad
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clickable(enabled = !isLoading) { imagePickerLauncher.launch("image/*") },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
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
                            Text(
                                text = "📷",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "(Tocar para elegir de Galería)",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Campos del formulario
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título de la Propiedad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio por Mes ($)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección Completa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            // Selector de Tipo de Propiedad
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = tipo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Propiedad") },
                    trailingIcon = {
                        Text(
                            text = "▼ ",
                            modifier = Modifier.clickable(enabled = !isLoading) { expandedTipoDropdown = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isLoading) { expandedTipoDropdown = true },
                    enabled = !isLoading
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

            Spacer(modifier = Modifier.height(16.dp))
// Botón de Registro Real
            Button(
                onClick = {
                    val precioDouble = precio.toDoubleOrNull()

                    if (titulo.isBlank() || descripcion.isBlank() || precio.isBlank() || direccion.isBlank()) {
                        Toast.makeText(context, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                    } else if (precioDouble == null || precioDouble <= 0) {
                        Toast.makeText(context, "El precio por mes debe ser un número válido mayor a cero", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        val nuevaPropiedad = Property(
                            titulo = titulo,
                            descripcion = descripcion,
                            precio = precioDouble,
                            direccion = direccion,
                            tipo = tipo,
                            imagenUri = imageUri?.toString() ?: ""
                        )

                        scope.launch {
                            repository.saveProperty(nuevaPropiedad).fold(
                                onSuccess = {
                                    isLoading = false
                                    Toast.makeText(context, "¡Propiedad registrada exitosamente en Firestore!", Toast.LENGTH_LONG).show()
                                    onNavigateBack()
                                },
                                onFailure = {
                                    isLoading = false
                                    Toast.makeText(context, "Error al guardar: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Registrar Propiedad", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // boton para limpiar el formulario
            OutlinedButton(
                onClick = {
                    titulo =""
                    descripcion = ""
                    precio = ""
                    direccion = ""
                    tipo = "Casa"
                    imageUri =null
                    Toast.makeText(context, "Formulario limpiado", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ){
                Text("Limpiar Formulario",style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
