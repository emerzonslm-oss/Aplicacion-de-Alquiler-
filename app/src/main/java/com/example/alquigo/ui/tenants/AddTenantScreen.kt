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

            PrimaryButton(
                text = "Registrar Inquilino",
                onClick = {
                    if (nombre.isBlank() || dni.isBlank() || telefono.isBlank() || email.isBlank()) {
                        Toast.makeText(context, "Complete todos los campos.", Toast.LENGTH_SHORT).show()
                    } else if (telefono.length != 9) {
                        Toast.makeText(context, "El teléfono debe tener 9 dígitos.", Toast.LENGTH_SHORT).show()
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(context, "Formato de correo inválido.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveTenant(
                            nombre = nombre,
                            dni = dni,
                            telefono = telefono,
                            email = email,
                            onSuccess = {
                                Toast.makeText(context, "Inquilino registrado.", Toast.LENGTH_SHORT).show()
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
