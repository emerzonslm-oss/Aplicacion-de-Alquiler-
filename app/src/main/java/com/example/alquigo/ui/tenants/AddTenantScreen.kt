package com.example.alquigo.ui.tenants

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alquigo.ui.components.AlquiTextField
import com.example.alquigo.ui.components.PrimaryButton
import com.example.alquigo.ui.theme.AirbnbPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(
    onNavigateBack: () -> Unit,
    viewModel: TenantViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState

    var nombre by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Inquilino") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Volver", color = AirbnbPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            AlquiTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre Completo",
                enabled = !uiState.isLoading
            )

            AlquiTextField(
                value = dni,
                onValueChange = { dni = it },
                label = "DNI",
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            AlquiTextField(
                value = telefono,
                onValueChange = { if (it.length <= 9) telefono = it },
                label = "Teléfono (9 dígitos)",
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            AlquiTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo Electrónico",
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Registrar Inquilino - Acción: Guarda el nuevo inquilino con los datos ingresados en los Inputs ejecutando viewModel.saveTenant(...)
            PrimaryButton(
                text = "Registrar Inquilino",
                onClick = {
                    val cleanNombre = nombre.trim()
                    val cleanDni = dni.trim()
                    val cleanTelefono = telefono.trim()
                    val cleanEmail = email.trim()

                    if (cleanNombre.isBlank() || cleanDni.isBlank() || cleanTelefono.isBlank() || cleanEmail.isBlank()) {
                        Toast.makeText(context, "Por favor complete todos los campos obligatorios.", Toast.LENGTH_SHORT).show()
                    } else if (cleanNombre.length < 3) {
                        Toast.makeText(context, "El nombre completo debe tener al menos 3 caracteres.", Toast.LENGTH_SHORT).show()
                    } else if (cleanDni.length != 8 || !cleanDni.all { it.isDigit() }) {
                        Toast.makeText(context, "El DNI debe contener exactamente 8 dígitos numéricos.", Toast.LENGTH_LONG).show()
                    } else if (cleanTelefono.length != 9 || !cleanTelefono.all { it.isDigit() }) {
                        Toast.makeText(context, "El teléfono debe contener exactamente 9 dígitos numéricos.", Toast.LENGTH_LONG).show()
                    } else if (!cleanTelefono.startsWith("9")) {
                        Toast.makeText(context, "El teléfono debe iniciar con el dígito 9.", Toast.LENGTH_LONG).show()
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                        Toast.makeText(context, "Ingrese un formato de correo electrónico válido.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveTenant(
                            nombre = cleanNombre,
                            dni = cleanDni,
                            telefono = cleanTelefono,
                            email = cleanEmail,
                            onSuccess = {
                                Toast.makeText(context, "¡Inquilino registrado exitosamente!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
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
