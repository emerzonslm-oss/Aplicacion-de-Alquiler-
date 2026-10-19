package com.example.alquigo.ui.profile

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
import com.example.alquigo.ui.auth.AuthState
import com.example.alquigo.ui.auth.AuthViewModel
import com.example.alquigo.ui.components.AlquiTextField
import com.example.alquigo.ui.components.PrimaryButton
import com.example.alquigo.ui.theme.AirbnbPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userState by viewModel.userState
    val authState by viewModel.authState

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direcion by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    LaunchedEffect(userState) {
        userState?.let { user ->
            nombre = user.nombre
            apellido = user.apellido
            telefono = user.telefono
            direcion = user.direcion
            email = user.email
        }
    }

    val isLoading = authState is AuthState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de la cuenta", style = MaterialTheme.typography.titleLarge) },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            AlquiTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre",
                enabled = !isLoading
            )

            AlquiTextField(
                value = apellido,
                onValueChange = { apellido = it },
                label = "Apellido",
                enabled = !isLoading
            )

            AlquiTextField(
                value = email,
                onValueChange = {},
                label = "Correo electrónico (No editable)",
                enabled = false,
                readOnly = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            AlquiTextField(
                value = telefono,
                onValueChange = { if (it.length <= 9) telefono = it },
                label = "Teléfono (9 dígitos)",
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            AlquiTextField(
                value = direcion,
                onValueChange = { direcion = it },
                label = "Dirección",
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Guardar Cambios",
                onClick = {
                    viewModel.updateUserProfile(
                        nombre = nombre,
                        apellido = apellido,
                        telefono = telefono,
                        direcion = direcion,
                        onSuccess = {
                            Toast.makeText(context, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                isLoading = isLoading,
                enabled = !isLoading
            )
        }
    }
}
