package com.example.alquigo.ui.property

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle

import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Refresh
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
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.alquigo.data.model.Payment
import com.example.alquigo.data.model.PropertyRequest
import com.example.alquigo.data.model.User
import com.example.alquigo.ui.auth.AuthViewModel
import com.example.alquigo.ui.components.AlquiTextField
import com.example.alquigo.ui.components.AvatarInitial
import com.example.alquigo.ui.components.GlassChip
import com.example.alquigo.ui.components.PrimaryButton
import com.example.alquigo.ui.components.StatusBadge
import com.example.alquigo.ui.components.compressAndConvertUriToBase64
import com.example.alquigo.ui.components.getImageModel
import com.example.alquigo.ui.components.sendReceiptEmail
import com.example.alquigo.ui.components.formatPrecio
import com.example.alquigo.ui.components.glassSurface
import com.example.alquigo.ui.components.parseFlexibleDate
import com.example.alquigo.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PropertyDetailScreen(
    propertyId: String,
    authViewModel: AuthViewModel? = null,
    onNavigateBack: () -> Unit,
    onNavigateToAssignTenant: (String) -> Unit = {},
    viewModel: PropertyDetailViewModel = viewModel(),
    propertyViewModel: PropertyViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val mainUiState by propertyViewModel.uiState
    val userState by authViewModel?.userState ?: remember { mutableStateOf<User?>(null) }
    val isTenant = userState?.rol == "INQUILINO"

    var paymentFilter by remember { mutableStateOf("Todos") }
    var selectedPaymentForPay by remember { mutableStateOf<Payment?>(null) }
    var paymentDateInput by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }

    var paymentVoucherUri by remember { mutableStateOf("") }
    var selectedVoucherImageForView by remember { mutableStateOf<String?>(null) }
    var showRenewContractDialog by remember { mutableStateOf(false) }
    var renewFechaFinInput by remember { mutableStateOf("") }
    var renewMontoInput by remember { mutableStateOf("") }

    var selectedRequestForApproval by remember { mutableStateOf<PropertyRequest?>(null) }
    var approveFechaInicioInput by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var approveFechaFinInput by remember { mutableStateOf(LocalDate.now().plusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var approveMontoInput by remember { mutableStateOf("") }

    val voucherPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedUri = compressAndConvertUriToBase64(context, uri)
            paymentVoucherUri = savedUri
            Toast.makeText(context, "Foto del comprobante cargada", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(propertyId, userState) {
        viewModel.loadPropertyDetail(propertyId)
        if (isTenant) {
            if (!userState?.uid.isNullOrBlank()) {
                propertyViewModel.loadTenantRequests(userState!!.uid)
            }
        } else {
            propertyViewModel.loadPendingRequests()
        }
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
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Color(0xFFF0F0F0))
        ) {
            if (property.imagenUri.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = getImageModel(property.imagenUri),
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

            val tenantReqForThisProp = remember(mainUiState.tenantRequests, property.id) {
                mainUiState.tenantRequests.find { it.propertyId == property.id }
            }
            val pendingReqsForThisProp = remember(mainUiState.pendingRequests, property.id) {
                mainUiState.pendingRequests.filter { it.propertyId == property.id }
            }
            val isMyAssignedContract = isTenant && uiState.currentTenant?.email.equals(userState?.email, ignoreCase = true)

            if (isTenant) {
                // ROL INQUILINO
                if (!property.estado.equals("OCUPADA", ignoreCase = true)) {
                    // Propiedad Disponible
                    Text(
                        text = "Solicitud de Alquiler",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (tenantReqForThisProp == null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "¿Te interesa esta propiedad?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Envía una solicitud de alquiler para que el Administrador revise tu propuesta.",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                PrimaryButton(
                                    text = "Solicitar Propiedad",
                                    onClick = {
                                        propertyViewModel.sendPropertyRequest(
                                            property = property,
                                            tenantUid = userState?.uid ?: "",
                                            tenantName = "${userState?.nombre} ${userState?.apellido}".trim(),
                                            tenantEmail = userState?.email ?: "",
                                            tenantPhone = userState?.telefono ?: "",
                                            onSuccess = {
                                                Toast.makeText(context, "¡Solicitud de propiedad enviada al Administrador!", Toast.LENGTH_LONG).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Estado de tu Solicitud",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    StatusBadge(status = tenantReqForThisProp.estado)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val statusDesc = when (tenantReqForThisProp.estado) {
                                    "PENDIENTE" -> "Tu solicitud enviada el ${tenantReqForThisProp.fechaSolicitud} se encuentra en revisión por el Administrador."
                                    "APROBADA" -> "¡Felicidades! Tu solicitud fue aprobada. Revisa tu contrato de alquiler abajo."
                                    "RECHAZADA" -> "Tu solicitud no fue aprobada por el Administrador."
                                    else -> tenantReqForThisProp.estado
                                }
                                Text(text = statusDesc, fontSize = 14.sp, color = TextSecondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else if (isMyAssignedContract) {
                    // Propiedad Ocupada por ESTE inquilino -> Ver su contrato y sus pagos
                    Text(
                        text = "Mi Contrato Vigente",
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
                                Text(text = "• Inicio: ${contract.fechaInicio}", fontSize = 15.sp, color = TextSecondary)
                                Text(text = "• Fin: ${contract.fechaFin}", fontSize = 15.sp, color = TextSecondary)
                                if (contract.montoMensual > 0) {
                                    Text(text = "• Cuota mensual: ${formatPrecio(contract.montoMensual)}", fontSize = 15.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    // Propiedad Ocupada por OTRO inquilino -> Mostrar información general sin datos privados
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Propiedad Ocupada",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Esta propiedad se encuentra actualmente alquilada.",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                // ROL ADMINISTRADOR
                if (pendingReqsForThisProp.isNotEmpty()) {
                    Text(
                        text = "Solicitudes para esta propiedad",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    pendingReqsForThisProp.forEach { req ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = req.tenantName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Email: ${req.tenantEmail} · Tel: ${req.tenantPhone}",
                                            fontSize = 13.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    StatusBadge(status = "PENDIENTE")
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedRequestForApproval = req
                                            approveMontoInput = if (req.propertyPrecio > 0) req.propertyPrecio.toInt().toString() else ""
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AirbnbPrimary),
                                        shape = ButtonShape
                                    ) {
                                        Text("Aceptar Solicitud", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            propertyViewModel.rejectRequest(
                                                requestId = req.id,
                                                onSuccess = {
                                                    Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                                                    viewModel.loadPropertyDetail(propertyId)
                                                },
                                                onError = { err ->
                                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = ButtonShape
                                    ) {
                                        Text("Rechazar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Inquilino Actual (Admin)
                Text(
                    text = "Inquilino actual",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.currentTenant != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarInitial(name = uiState.currentTenant!!.nombre, size = 60.dp, fontSize = 26)
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

                // Contrato Vigente (Admin)
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
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = {
                                    val currentEnd = contract.fechaFin
                                    val parsed = parseFlexibleDate(currentEnd)
                                    val defaultNewEnd = parsed?.plusMonths(6)?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: ""
                                    renewFechaFinInput = defaultNewEnd
                                    renewMontoInput = if (contract.montoMensual > 0) contract.montoMensual.toInt().toString() else ""
                                    showRenewContractDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = InputShape,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AirbnbPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Renovar Contrato", fontWeight = FontWeight.Bold)
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

                // Historial de Contratos (Admin)
                Text(
                    text = "Historial de contratos",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.contractHistory.isNotEmpty()) {
                    uiState.contractHistory.forEach { item ->
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
                                    Text(
                                        text = item.tenantName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    StatusBadge(status = item.contract.estado)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Periodo: ${item.contract.fechaInicio} al ${item.contract.fechaFin}",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                                if (item.contract.montoMensual > 0) {
                                    Text(
                                        text = "Monto mensual: ${formatPrecio(item.contract.montoMensual)}",
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No hay registro de contratos pasados.",
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Historial de Pagos (Admin)
                Text(
                    text = "Historial de pagos",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.recentPayments.isNotEmpty()) {
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
                                            paymentVoucherUri = ""
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
                                } else if (payment.comprobanteUrl.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = {
                                            selectedVoucherImageForView = payment.comprobanteUrl
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = InputShape,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Receipt,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Ver Comprobante", fontWeight = FontWeight.Bold)
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
            containerColor = Color.White,
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
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Foto del Comprobante (Voucher):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .border(1.dp, BorderLight, InputShape)
                            .clickable {
                                voucherPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        shape = InputShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (paymentVoucherUri.isNotBlank()) {
                                AsyncImage(
                                    model = getImageModel(paymentVoucherUri),
                                    contentDescription = "Comprobante",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📷", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Tocar para adjuntar foto del voucher", fontSize = 13.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { selectedPaymentForPay = null }
                    ) {
                        Text("Cancelar", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanDate = paymentDateInput.trim()
                            if (cleanDate.isBlank()) {
                                Toast.makeText(context, "Por favor ingrese la fecha real de pago.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val parsedDate = parseFlexibleDate(cleanDate)
                            if (parsedDate == null) {
                                Toast.makeText(context, "Formato de fecha inválido. Ingrese una fecha válida (AAAA-MM-DD).", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            val payId = payment.id
                            selectedPaymentForPay = null
                            viewModel.markPaymentAsPaid(
                                paymentId = payId,
                                fechaPagoReal = cleanDate,
                                comprobanteUrl = paymentVoucherUri,
                                onSuccess = {
                                    Toast.makeText(context, "¡Pago registrado como PAGADO!", Toast.LENGTH_SHORT).show()

                                    val tenantEmail = uiState.currentTenant?.email ?: ""
                                    val tenantName = uiState.currentTenant?.nombre ?: "Inquilino"
                                    val propTitle = uiState.property?.titulo ?: "Propiedad"
                                    val montoStr = formatPrecio(payment.monto)

                                    sendReceiptEmail(
                                        context = context,
                                        tenantEmail = tenantEmail,
                                        tenantName = tenantName,
                                        propertyTitle = propTitle,
                                        montoStr = montoStr,
                                        concepto = payment.concepto,
                                        fechaPagoReal = cleanDate
                                    )
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AirbnbPrimary),
                        shape = ButtonShape
                    ) {
                        Text("Confirmar Pago", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // Modal para Renovar Contrato
    if (showRenewContractDialog && uiState.activeContract != null) {
        val activeContract = uiState.activeContract!!
        AlertDialog(
            onDismissRequest = { showRenewContractDialog = false },
            shape = CardShape,
            containerColor = Color.White,
            title = {
                Text(
                    text = "Renovar Contrato de Alquiler",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Periodo actual: ${activeContract.fechaInicio} al ${activeContract.fechaFin}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AlquiTextField(
                        value = renewFechaFinInput,
                        onValueChange = { renewFechaFinInput = it },
                        label = "Nueva Fecha de Fin (AAAA-MM-DD)"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AlquiTextField(
                        value = renewMontoInput,
                        onValueChange = { renewMontoInput = it },
                        label = "Cuota Mensual (S/)"
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showRenewContractDialog = false }
                    ) {
                        Text("Cancelar", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanDate = renewFechaFinInput.trim()
                            val montoDouble = renewMontoInput.trim().toDoubleOrNull()

                            if (cleanDate.isBlank()) {
                                Toast.makeText(context, "Ingrese la nueva fecha de fin.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (parseFlexibleDate(cleanDate) == null) {
                                Toast.makeText(context, "Formato de fecha inválido (AAAA-MM-DD).", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (montoDouble == null || montoDouble <= 0) {
                                Toast.makeText(context, "Ingrese un monto mensual válido mayor a cero.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val contractId = activeContract.id
                            showRenewContractDialog = false
                            viewModel.renewContract(
                                contractId = contractId,
                                nuevaFechaFin = cleanDate,
                                nuevoMontoMensual = montoDouble,
                                onSuccess = {
                                    Toast.makeText(context, "¡Contrato renovado exitosamente!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AirbnbPrimary),
                        shape = ButtonShape
                    ) {
                        Text("Confirmar Renovación", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // Modal Lightbox para Ver Comprobante
    if (selectedVoucherImageForView != null) {
        val voucherUrl = selectedVoucherImageForView!!
        AlertDialog(
            onDismissRequest = { selectedVoucherImageForView = null },
            shape = CardShape,
            containerColor = Color.White,
            title = {
                Text(
                    text = "Comprobante de Pago",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = getImageModel(voucherUrl),
                        contentDescription = "Comprobante de Pago",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedVoucherImageForView = null }) {
                    Text("Cerrar", fontWeight = FontWeight.Bold, color = AirbnbPrimary)
                }
            }
        )
    }

    // Modal Aceptar Solicitud y Asignar Contrato
    if (selectedRequestForApproval != null) {
        val req = selectedRequestForApproval!!
        AlertDialog(
            onDismissRequest = { selectedRequestForApproval = null },
            shape = CardShape,
            containerColor = Color.White,
            title = {
                Text(
                    text = "Aprobar Solicitud y Generar Contrato",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Solicitante: ${req.tenantName}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Email: ${req.tenantEmail}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AlquiTextField(
                        value = approveFechaInicioInput,
                        onValueChange = { approveFechaInicioInput = it },
                        label = "Fecha de Inicio (AAAA-MM-DD)"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AlquiTextField(
                        value = approveFechaFinInput,
                        onValueChange = { approveFechaFinInput = it },
                        label = "Fecha de Fin (AAAA-MM-DD)"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AlquiTextField(
                        value = approveMontoInput,
                        onValueChange = { approveMontoInput = it },
                        label = "Monto Mensual (S/)"
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { selectedRequestForApproval = null }) {
                        Text("Cancelar", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanInicio = approveFechaInicioInput.trim()
                            val cleanFin = approveFechaFinInput.trim()
                            val montoD = approveMontoInput.trim().toDoubleOrNull()

                            if (cleanInicio.isBlank() || cleanFin.isBlank()) {
                                Toast.makeText(context, "Ingrese las fechas de contrato", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (montoD == null || montoD <= 0) {
                                Toast.makeText(context, "Monto mensual inválido", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val requestToApprove = req
                            selectedRequestForApproval = null
                            propertyViewModel.approveRequest(
                                request = requestToApprove,
                                fechaInicio = cleanInicio,
                                fechaFin = cleanFin,
                                montoMensual = montoD,
                                onSuccess = {
                                    Toast.makeText(context, "¡Solicitud aprobada y contrato creado!", Toast.LENGTH_SHORT).show()
                                    viewModel.loadPropertyDetail(propertyId)
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AirbnbPrimary),
                        shape = ButtonShape
                    ) {
                        Text("Aprobar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

