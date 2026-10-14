package com.example.alquigo.ui.property

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.alquigo.data.model.Payment
import com.example.alquigo.ui.components.AlquiTextField
import com.example.alquigo.ui.components.AvatarInitial
import com.example.alquigo.ui.components.GlassChip
import com.example.alquigo.ui.components.PrimaryButton
import com.example.alquigo.ui.components.StatusBadge
import com.example.alquigo.ui.components.formatPrecio
import com.example.alquigo.ui.components.glassSurface
import com.example.alquigo.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PropertyDetailScreen(
    propertyId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAssignTenant: (String) -> Unit = {},
    viewModel: PropertyDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState

    var paymentFilter by remember { mutableStateOf("Todos") }
    var selectedPaymentForPay by remember { mutableStateOf<Payment?>(null) }
    var paymentDateInput by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }

    LaunchedEffect(propertyId) {
        viewModel.loadPropertyDetail(propertyId)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundWhite), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AirbnbPrimary)
        }
        return
    }

    val property = uiState.property
    if (property == null) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundWhite), contentAlignment = Alignment.Center) {
            Text("Propiedad no encontrada.", color = TextPrimary)
        }
        return
    }

    val filteredPayments = remember(uiState.recentPayments, paymentFilter) {
        when (paymentFilter) {
            "Pendientes" -> uiState.recentPayments.filter { it.estado.equals("PENDIENTE", ignoreCase = true) }
            "Pagados" -> uiState.recentPayments.filter { it.estado.equals("PAGADO", ignoreCase = true) }
            else -> uiState.recentPayments
        }
    }

    val totalRecaudado = remember(uiState.recentPayments) {
        uiState.recentPayments
            .filter { it.estado.equals("PAGADO", ignoreCase = true) }
            .sumOf { it.monto }
    }

    val totalPendiente = remember(uiState.recentPayments) {
        uiState.recentPayments
            .filter { it.estado.equals("PENDIENTE", ignoreCase = true) }
            .sumOf { it.monto }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .verticalScroll(rememberScrollState())
    ) {
        // HEADER: Imagen y Botón Atrás
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color(0xFFF0F0F0))
        ) {
            if (property.imagenUri.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = property.imagenUri,
                    contentDescription = property.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.HomeWork,
                        contentDescription = null,
                        tint = Color(0xFFB0B0B0),
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .glassSurface(shape = CircleShape, tintAlpha = 0.8f)
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
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Título principal
            Text(
                text = property.titulo,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${property.tipo} · ${property.direccion}",
                fontSize = 16.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Descripción
            if (property.descripcion.isNotBlank()) {
                Text(
                    text = property.descripcion,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${formatPrecio(property.precio)} ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "por mes",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN: Inquilino Actual
            Text(
                text = "Inquilino actual",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.currentTenant != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarInitial(name = uiState.currentTenant!!.nombre, size = 64.dp, fontSize = 28)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = uiState.currentTenant!!.nombre,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = uiState.currentTenant!!.telefono, fontSize = 15.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = uiState.currentTenant!!.email, fontSize = 15.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                Text(
                    text = "Propiedad disponible - Sin inquilino",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN: Contrato Vigente
            Text(
                text = "Contrato vigente",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.activeContract != null) {
                val contract = uiState.activeContract!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Periodo del contrato", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            StatusBadge(status = contract.estado)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Inicio: ${contract.fechaInicio}", fontSize = 15.sp, color = TextSecondary)
                        Text(text = "Fin: ${contract.fechaFin}", fontSize = 15.sp, color = TextSecondary)
                        if (contract.montoMensual > 0) {
                            Text(text = "Cuota mensual: ${formatPrecio(contract.montoMensual)}", fontSize = 15.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                Column {
                    Text(
                        text = "No hay ningún contrato activo.",
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Asignar Inquilino",
                        onClick = { onNavigateToAssignTenant(property.id) },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN: Historial de Pagos
            Text(
                text = "Historial de pagos",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.recentPayments.isNotEmpty()) {
                // Tarjetas resumen de montos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = PaidStatusBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Cobrado", fontSize = 13.sp, color = PaidStatusText, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatPrecio(totalRecaudado), fontSize = 18.sp, color = PaidStatusText, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = PendingStatusBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Por cobrar", fontSize = 13.sp, color = PendingStatusText, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatPrecio(totalPendiente), fontSize = 18.sp, color = PendingStatusText, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fila de Filtros
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todos", "Pendientes", "Pagados").forEach { filterOption ->
                        GlassChip(
                            text = filterOption,
                            isSelected = paymentFilter == filterOption,
                            onClick = { paymentFilter = filterOption }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                filteredPayments.forEach { payment ->
                    val isPending = payment.estado.equals("PENDIENTE", ignoreCase = true)
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = payment.concepto,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Programado: ${payment.fecha}",
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                    if (!isPending && payment.fechaPagoReal.isNotBlank()) {
                                        Text(
                                            text = "Pagado el: ${payment.fechaPagoReal}",
                                            fontSize = 13.sp,
                                            color = PaidStatusText,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatPrecio(payment.monto),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    StatusBadge(status = payment.estado)
                                }
                            }

                            if (isPending) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        selectedPaymentForPay = payment
                                        paymentDateInput = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = InputShape,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AirbnbPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Marcar como Pagado", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No hay pagos registrados asociados a esta propiedad.",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Modal para Marcar Pago como Pagado
    if (selectedPaymentForPay != null) {
        val payment = selectedPaymentForPay!!
        AlertDialog(
            onDismissRequest = { selectedPaymentForPay = null },
            shape = CardShape,
            title = {
                Text(
                    text = "Confirmar Recepción de Pago",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Concepto: ${payment.concepto}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Monto: ${formatPrecio(payment.monto)}",
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AlquiTextField(
                        value = paymentDateInput,
                        onValueChange = { paymentDateInput = it },
                        label = "Fecha Real de Pago (AAAA-MM-DD)"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val payId = payment.id
                        selectedPaymentForPay = null
                        viewModel.markPaymentAsPaid(
                            paymentId = payId,
                            fechaPagoReal = paymentDateInput,
                            onSuccess = {
                                Toast.makeText(context, "¡Pago registrado como PAGADO!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AirbnbPrimary)
                ) {
                    Text("Confirmar Pago", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPaymentForPay = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}
