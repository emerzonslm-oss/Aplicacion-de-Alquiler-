package com.example.alquigo.ui.property

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alquigo.ui.components.AlquiTextField
import com.example.alquigo.ui.components.AvatarInitial
import com.example.alquigo.ui.components.PrimaryButton
import com.example.alquigo.ui.components.formatPrecio
import com.example.alquigo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTenantScreen(
    propertyId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddTenant: () -> Unit,
    viewModel: AssignTenantViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState

    var isTenantDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(propertyId) {
        viewModel.loadData(propertyId)
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AirbnbPrimary)
        }
        return
    }

    val property = uiState.property
    if (property == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite),
            contentAlignment = Alignment.Center
        ) {
            Text("Propiedad no encontrada", color = TextPrimary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Asignar Inquilino",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tarjeta resumen de propiedad
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(InputShape)
                        .background(AirbnbPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HomeWork,
                        contentDescription = null,
                        tint = AirbnbPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = property.titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${property.tipo} · ${property.direccion}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 1. SELECCIONAR INQUILINO
        Text(
            text = "Seleccionar Inquilino",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.tenants.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No tienes inquilinos registrados.",
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onNavigateToAddTenant,
                        shape = InputShape
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar Inquilino")
                    }
                }
            }
        } else {
            ExposedDropdownMenuBox(
                expanded = isTenantDropdownExpanded,
                onExpandedChange = { isTenantDropdownExpanded = !isTenantDropdownExpanded }
            ) {
                val selectedName = uiState.selectedTenant?.nombre ?: "Selecciona un inquilino"

                Box(
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(InputShape)
                        .background(Color.White)
                        .border(1.dp, BorderLight, InputShape)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.selectedTenant != null) {
                                AvatarInitial(name = uiState.selectedTenant!!.nombre, size = 32.dp, fontSize = 14)
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Text(
                                text = selectedName,
                                fontSize = 16.sp,
                                color = if (uiState.selectedTenant != null) TextPrimary else TextSecondary,
                                fontWeight = if (uiState.selectedTenant != null) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.ArrowDropDown,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = isTenantDropdownExpanded,
                    onDismissRequest = { isTenantDropdownExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    uiState.tenants.forEach { tenant ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AvatarInitial(name = tenant.nombre, size = 36.dp, fontSize = 16)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = tenant.nombre,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "DNI: ${tenant.dni} · ${tenant.telefono}",
                                            fontSize = 13.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.selectTenant(tenant)
                                isTenantDropdownExpanded = false
                            },
                            trailingIcon = if (uiState.selectedTenant?.id == tenant.id) {
                                { Icon(Icons.Outlined.Check, contentDescription = null, tint = AirbnbPrimary) }
                            } else null
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. DETALLES DEL CONTRATO
        Text(
            text = "Detalles del Contrato",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Fecha Inicio
        AlquiTextField(
            value = uiState.fechaInicio,
            onValueChange = { viewModel.updateFechaInicio(it) },
            label = "Fecha de Inicio (AAAA-MM-DD)"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fecha Fin
        AlquiTextField(
            value = uiState.fechaFin,
            onValueChange = { viewModel.updateFechaFin(it) },
            label = "Fecha de Fin (AAAA-MM-DD)"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Monto Mensual
        AlquiTextField(
            value = uiState.montoMensual,
            onValueChange = { viewModel.updateMontoMensual(it) },
            label = "Monto Mensual (S/)"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Resumen Informativo
        if (uiState.selectedTenant != null && uiState.montoMensual.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = AirbnbPrimary.copy(alpha = 0.05f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumen del Acuerdo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Inquilino: ${uiState.selectedTenant?.nombre}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "• Periodo: ${uiState.fechaInicio} al ${uiState.fechaFin}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    val montoDouble = uiState.montoMensual.toDoubleOrNull() ?: 0.0
                    Text(
                        text = "• Alquiler mensual: ${formatPrecio(montoDouble)}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Se generarán los pagos pendientes mensuales y la propiedad se marcará como Ocupada.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Botón Guardar
        PrimaryButton(
            text = if (uiState.isSubmitting) "Creando Contrato..." else "Crear Contrato y Asignar",
            onClick = {
                viewModel.createContract(
                    onSuccess = {
                        Toast.makeText(context, "¡Inquilino asignado y contrato creado exitosamente!", Toast.LENGTH_LONG).show()
                        onNavigateBack()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            },
            enabled = !uiState.isSubmitting
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}
