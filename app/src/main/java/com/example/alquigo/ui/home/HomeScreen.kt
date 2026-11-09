package com.example.alquigo.ui.home

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.alquigo.data.model.DuePaymentInfo
import com.example.alquigo.data.model.Payment
import com.example.alquigo.data.model.PropertyRequest
import com.example.alquigo.data.model.User
import com.example.alquigo.ui.auth.AuthViewModel
import com.example.alquigo.ui.components.*
import com.example.alquigo.ui.property.DashboardMetrics
import com.example.alquigo.ui.property.PropertyViewModel
import com.example.alquigo.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    propertyViewModel: PropertyViewModel,
    authViewModel: AuthViewModel? = null,
    onNavigateToAddProperty: () -> Unit,
    onNavigateToPropertyList: () -> Unit,
    onNavigateToPropertyDetail: (String) -> Unit,
    hazeState: HazeState? = null
) {
    val context = LocalContext.current
    val uiState by propertyViewModel.uiState
    val userState by authViewModel?.userState ?: remember { mutableStateOf<User?>(null) }
    val isTenantMode = userState?.rol == "INQUILINO"
    val userEmail = userState?.email ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: ""

    var selectedRequestForApproval by remember { mutableStateOf<PropertyRequest?>(null) }
    var approveFechaInicioInput by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var approveFechaFinInput by remember { mutableStateOf(LocalDate.now().plusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var approveMontoInput by remember { mutableStateOf("") }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userState?.uid) {
        if (!userState?.uid.isNullOrBlank()) {
            propertyViewModel.loadNotifications(userState!!.uid)
        }
    }

    if (isTenantMode) {
        LaunchedEffect(userEmail, userState?.uid) {
            if (userEmail.isNotBlank()) {
                propertyViewModel.loadTenantRentalInfo(userEmail)
            }
            if (!userState?.uid.isNullOrBlank()) {
                propertyViewModel.loadTenantRequests(userState!!.uid)
            }
            propertyViewModel.loadProperties()
        }
        TenantHomeScreenView(
            uiState = uiState,
            user = userState,
            propertyViewModel = propertyViewModel,
            hazeState = hazeState,
            onNavigateToPropertyDetail = onNavigateToPropertyDetail,
            onNavigateToPropertyList = onNavigateToPropertyList,
            onSelectFilter = { category -> propertyViewModel.loadProperties(category) }
        )
        return
    }

    val categories = listOf("Todos", "Favoritos", "Casa", "Apartamento", "Habitación", "Local Comercial")

    LaunchedEffect(Unit) {
        propertyViewModel.loadProperties()
        propertyViewModel.loadPendingRequests()
    }

    val disponiblesProps = remember(uiState.properties) {
        uiState.properties.filter { !it.estado.equals("OCUPADA", ignoreCase = true) }
    }
    val ocupadasProps = remember(uiState.properties) {
        uiState.properties.filter { it.estado.equals("OCUPADA", ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE6E6E6), Color.White),
                    startY = 0f,
                    endY = 500f
                )
            )
            .then(if (hazeState != null) Modifier.hazeSource(state = hazeState) else Modifier)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Cabecera con Botón de Notificaciones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AlquiGo",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(0.5.dp, BorderLight, CircleShape)
                    .clickable { showNotificationsDialog = true },
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (uiState.unreadNotificationsCount > 0) {
                            Badge(containerColor = AirbnbPrimary) {
                                Text("${uiState.unreadNotificationsCount}", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notificaciones",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Barra de Búsqueda Superior (con 24dp de padding horizontal)
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            GlassSearchBar(
                placeholder = "Busca una propiedad",
                onClick = onNavigateToPropertyList,
                hazeState = hazeState
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chips Desplazables
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp)
        ) {
            items(categories) { category ->
                GlassChip(
                    text = category,
                    isSelected = uiState.selectedFilter == category,
                    onClick = { propertyViewModel.loadProperties(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botones "Publicar" y "Ver catálogo"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 📌 BOTÓN QUE ABRE UNA NUEVA PANTALLA CON INPUTS: Llama a onNavigateToAddProperty que abre la pantalla AddPropertyScreen (la cual dibuja las cajas de texto/inputs de título, dirección, precio y descripción)
            PrimaryButton(
                text = "Publicar",
                onClick = onNavigateToAddProperty,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            // Botón de Navegación a Catálogo - Abre la pantalla PropertyListScreen con el listado de propiedades
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(InputShape)
                    .background(Color.White)
                    .border(1.dp, BorderLight, InputShape)
                    .clickable { onNavigateToPropertyList() }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ver catálogo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DASHBOARD DE MÉTRICAS DEL PROPIETARIO
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            DashboardHeaderSection(metrics = uiState.metrics)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LISTA DE PAGOS POR VENCER / VENCIDOS & RECORDATORIO POR WHATSAPP
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            DuePaymentsWhatsAppSection(duePayments = uiState.duePayments)
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SOLICITUDES DE PROPIEDAD PENDIENTES (ROL ADMINISTRADOR)
        if (uiState.pendingRequests.isNotEmpty()) {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                PendingRequestsAdminSection(
                    pendingRequests = uiState.pendingRequests,
                    onApproveClick = { req ->
                        selectedRequestForApproval = req
                        approveMontoInput = if (req.propertyPrecio > 0) req.propertyPrecio.toInt().toString() else ""
                    },
                    onRejectClick = { reqId ->
                        propertyViewModel.rejectRequest(
                            requestId = reqId,
                            onSuccess = {
                                Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AirbnbPrimary)
            }
        } else if (uiState.properties.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.selectedFilter == "Favoritos") {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = AirbnbPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Sin favoritos guardados",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toca el icono de corazón en cualquier propiedad para guardarla en tus favoritos.",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        } else if (uiState.selectedFilter != "Todos") {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No hay propiedades en '${uiState.selectedFilter}'",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.HomeWork,
                                contentDescription = null,
                                tint = AirbnbPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aún no tienes propiedades publicadas.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Publica la primera para empezar a gestionar tus alquileres.",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryButton(
                                text = "Publicar la primera",
                                onClick = onNavigateToAddProperty,
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }
        } else if (uiState.selectedFilter != "Todos") {
            // Sección para Filtro Activo ("Favoritos", "Casa", "Apartamento", etc.)
            val sectionTitle = if (uiState.selectedFilter == "Favoritos") "Mis Favoritos (${uiState.properties.size})" else uiState.selectedFilter
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                SectionHeader(
                    title = sectionTitle,
                    onArrowClick = onNavigateToPropertyList
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                items(uiState.properties, key = { it.id }) { property ->
                    PropertyCard(
                        property = property,
                        onClick = { onNavigateToPropertyDetail(property.id) },
                        modifier = Modifier.width(180.dp),
                        estado = if (property.estado.equals("OCUPADA", ignoreCase = true)) "Ocupada" else "Disponible",
                        isFavorite = uiState.favoritePropertyIds.contains(property.id),
                        onFavoriteClick = { propertyViewModel.toggleFavorite(property.id) },
                        hazeState = hazeState
                    )
                }
            }
        } else {
            // SECCIÓN 1: Disponibles ahora
            if (disponiblesProps.isNotEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SectionHeader(
                        title = "Disponibles ahora",
                        onArrowClick = onNavigateToPropertyList
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    items(disponiblesProps, key = { it.id }) { property ->
                        PropertyCard(
                            property = property,
                            onClick = { onNavigateToPropertyDetail(property.id) },
                            modifier = Modifier.width(180.dp),
                            estado = "Disponible",
                            isFavorite = uiState.favoritePropertyIds.contains(property.id),
                            onFavoriteClick = { propertyViewModel.toggleFavorite(property.id) },
                            hazeState = hazeState
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // SECCIÓN 2: Ocupadas este mes
            if (ocupadasProps.isNotEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SectionHeader(
                        title = "Ocupadas este mes",
                        onArrowClick = onNavigateToPropertyList
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    items(ocupadasProps, key = { "ocupada_${it.id}" }) { property ->
                        PropertyCard(
                            property = property,
                            onClick = { onNavigateToPropertyDetail(property.id) },
                            modifier = Modifier.width(180.dp),
                            estado = "Ocupada",
                            isFavorite = uiState.favoritePropertyIds.contains(property.id),
                            onFavoriteClick = { propertyViewModel.toggleFavorite(property.id) },
                            hazeState = hazeState
                        )
                    }
                }
            }
        }
    }

    // Modal para Aprobar Solicitud (Administrador)
    if (selectedRequestForApproval != null) {
        val req = selectedRequestForApproval!!
        AlertDialog(
            onDismissRequest = { selectedRequestForApproval = null },
            shape = CardShape,
            containerColor = Color.White,
            title = {
                Text(
                    text = "Aprobar Solicitud de Alquiler",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Inquilino: ${req.tenantName}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Correo: ${req.tenantEmail} · Tel: ${req.tenantPhone}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Propiedad: ${req.propertyTitle}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AlquiTextField(
                        value = approveFechaInicioInput,
                        onValueChange = { approveFechaInicioInput = it },
                        label = "Fecha de Inicio (AAAA-MM-DD)"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AlquiTextField(
                        value = approveFechaFinInput,
                        onValueChange = { approveFechaFinInput = it },
                        label = "Fecha de Fin (AAAA-MM-DD)"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AlquiTextField(
                        value = approveMontoInput,
                        onValueChange = { approveMontoInput = it },
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
                    TextButton(onClick = { selectedRequestForApproval = null }) {
                        Text("Cancelar", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanStart = approveFechaInicioInput.trim()
                            val cleanEnd = approveFechaFinInput.trim()
                            val montoDouble = approveMontoInput.trim().toDoubleOrNull() ?: 0.0

                            if (cleanStart.isBlank() || cleanEnd.isBlank() || montoDouble <= 0) {
                                Toast.makeText(context, "Por favor ingrese fechas y un monto válido.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val targetReq = req
                            selectedRequestForApproval = null

                            propertyViewModel.approveRequest(
                                request = targetReq,
                                fechaInicio = cleanStart,
                                fechaFin = cleanEnd,
                                montoMensual = montoDouble,
                                onSuccess = {
                                    Toast.makeText(context, "¡Solicitud aprobada y contrato creado exitosamente!", Toast.LENGTH_LONG).show()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AirbnbPrimary),
                        shape = ButtonShape
                    ) {
                        Text("Aceptar y Crear Contrato", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // Modal de Notificaciones
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            shape = CardShape,
            containerColor = Color.White,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notificaciones", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    if (uiState.unreadNotificationsCount > 0) {
                        Text("${uiState.unreadNotificationsCount} nuevas", fontSize = 12.sp, color = AirbnbPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (uiState.notifications.isEmpty()) {
                        Text("No tienes notificaciones registradas.", fontSize = 14.sp, color = TextSecondary)
                    } else {
                        uiState.notifications.forEach { notif ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!notif.leida) Color(0xFFFFF8F8) else Color(0xFFF9F9F9)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(notif.titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(notif.fecha, fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(notif.mensaje, fontSize = 13.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.End) {
                    if (uiState.unreadNotificationsCount > 0) {
                        TextButton(
                            onClick = {
                                propertyViewModel.markNotificationsAsRead(userState?.uid ?: "")
                            }
                        ) {
                            Text("Marcar como leídas", color = AirbnbPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = { showNotificationsDialog = false }) {
                        Text("Cerrar", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

@Composable
fun PendingRequestsAdminSection(
    pendingRequests: List<PropertyRequest>,
    onApproveClick: (PropertyRequest) -> Unit,
    onRejectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Solicitudes de Alquiler",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Surface(
                shape = CircleShape,
                color = AirbnbPrimary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${pendingRequests.size} pendientes",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AirbnbPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            pendingRequests.forEach { req ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = req.tenantName.ifBlank { "Inquilino" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Propiedad: ${req.propertyTitle}",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                            StatusBadge(status = "PENDIENTE")
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Email: ${req.tenantEmail} · Tel: ${req.tenantPhone}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Fecha de solicitud: ${req.fechaSolicitud}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Botón de Aprobación de Solicitud - Activa selectedRequestForApproval y dibuja el AlertDialog emergente con los Inputs de fecha e importe
                            Button(
                                onClick = { onApproveClick(req) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = ButtonShape,
                                colors = ButtonDefaults.buttonColors(containerColor = AirbnbPrimary)
                            ) {
                                Text("Aceptar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            // Botón de Rechazo - Acción directa que rechaza la solicitud llamando a propertyViewModel.rejectRequest
                            OutlinedButton(
                                onClick = { onRejectClick(req.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = ButtonShape,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                            ) {
                                Text("Rechazar", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardHeaderSection(
    metrics: DashboardMetrics,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Resumen del Propietario",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tarjeta 1: % Ocupación
            Card(
                modifier = Modifier.weight(1f),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Ocupación", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${metrics.occupancyPercentage}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AirbnbPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${metrics.occupiedProperties} de ${metrics.totalProperties} ocupadas",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Tarjeta 2: Propiedades Libres
            Card(
                modifier = Modifier.weight(1f),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Disponibles", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${metrics.availableProperties}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "propiedades libres",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tarjeta 3: Ingresos cobrados del mes
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9F4)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Cobrado este mes", fontSize = 14.sp, color = PaidStatusText, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatPrecio(metrics.monthlyEarnings),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PaidStatusText
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PaidStatusText.copy(alpha = 0.1f))
                        .padding(10.dp)
                ) {
                    Text("💰", fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun DuePaymentsWhatsAppSection(
    duePayments: List<DuePaymentInfo>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val whatsAppGreen = Color(0xFF25D366)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pagos por vencer y vencidos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (duePayments.isNotEmpty()) {
                Surface(
                    shape = CircleShape,
                    color = AirbnbPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${duePayments.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AirbnbPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (duePayments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✅", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "¡Al día! Sin pagos pendientes por vencer.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "No tienes mensualidades vencidas ni próximas a vencer en 7 días.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                duePayments.forEach { item ->
                    val isOverdue = item.isOverdue
                    val badgeColor = if (isOverdue) PendingStatusText else Color(0xFFFF9800)
                    val badgeBg = if (isOverdue) PendingStatusBg else Color(0xFFFFF3E0)
                    val badgeText = if (isOverdue) "VENCIDO" else "Vence en ${item.daysRemaining} días"

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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.tenantName.ifBlank { "Inquilino registrado" },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.propertyTitle,
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = badgeBg
                                ) {
                                    Text(
                                        text = badgeText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Vence: ${item.payment.fecha}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = formatPrecio(item.payment.monto),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Botón para WhatsApp - Acción: Abre la app de WhatsApp con el mensaje de recordatorio prellenado para la cuota por vencer
                            Button(
                                onClick = {
                                    openWhatsAppReminder(
                                        context = context,
                                        tenantPhone = item.tenantPhone,
                                        tenantName = item.tenantName,
                                        propertyTitle = item.propertyTitle,
                                        amount = item.payment.monto,
                                        dueDateStr = item.payment.fecha,
                                        isOverdue = isOverdue
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = InputShape,
                                colors = ButtonDefaults.buttonColors(containerColor = whatsAppGreen)
                            ) {
                                Text(
                                    text = "💬 Recordar por WhatsApp",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TenantHomeScreenView(
    uiState: com.example.alquigo.ui.property.PropertyUiState,
    user: User?,
    propertyViewModel: PropertyViewModel? = null,
    hazeState: HazeState? = null,
    onNavigateToPropertyDetail: (String) -> Unit = {},
    onNavigateToPropertyList: () -> Unit = {},
    onSelectFilter: (String) -> Unit = {}
) {
    val info = uiState.tenantRentalInfo
    val property = info?.property
    val contract = info?.contract
    val payments = info?.payments ?: emptyList()

    val context = LocalContext.current
    var selectedTenantPaymentForUpload by remember { mutableStateOf<Payment?>(null) }
    var tenantVoucherUri by remember { mutableStateOf("") }
    var tenantPaymentDateInput by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var selectedTenantVoucherForView by remember { mutableStateOf<String?>(null) }
    var showTenantNotifications by remember { mutableStateOf(false) }

    val tenantVoucherPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedUri = compressAndConvertUriToBase64(context, uri)
            tenantVoucherUri = savedUri
            Toast.makeText(context, "Foto del comprobante seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    val totalPagado = payments.filter { it.estado.equals("PAGADO", ignoreCase = true) }.sumOf { it.monto }
    val totalPendiente = payments.filter { it.estado.equals("PENDIENTE", ignoreCase = true) }.sumOf { it.monto }
    val categories = listOf("Todos", "Favoritos", "Casa", "Apartamento", "Habitación", "Local Comercial")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .then(if (hazeState != null) Modifier.hazeSource(state = hazeState) else Modifier)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 120.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarInitial(
                    name = user?.nombre?.ifEmpty { "Inquilino" } ?: "Inquilino",
                    size = 50.dp,
                    fontSize = 22
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Mi Alquiler",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Bienvenido, ${user?.nombre ?: "Inquilino"}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(0.5.dp, BorderLight, CircleShape)
                    .clickable { showTenantNotifications = true },
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (uiState.unreadNotificationsCount > 0) {
                            Badge(containerColor = AirbnbPrimary) {
                                Text("${uiState.unreadNotificationsCount}", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notificaciones",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de Búsqueda Superior para Inquilino / Explorador
        GlassSearchBar(
            placeholder = "Busca una propiedad",
            onClick = onNavigateToPropertyList,
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Chips Desplazables de Categorías
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categories) { category ->
                GlassChip(
                    text = category,
                    isSelected = uiState.selectedFilter == category,
                    onClick = { onSelectFilter(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AirbnbPrimary)
            }
            return
        }

        // SOLICITUDES DE ALQUILER REALIZADAS POR EL INQUILINO
        if (uiState.tenantRequests.isNotEmpty()) {
            Text(
                text = "Mis Solicitudes de Alquiler",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            uiState.tenantRequests.forEach { req ->
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
                                text = req.propertyTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            StatusBadge(status = req.estado)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fecha de solicitud: ${req.fechaSolicitud}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        val statusText = when (req.estado) {
                            "PENDIENTE" -> "En revisión por el Administrador"
                            "APROBADA" -> "¡Solicitud Aprobada! Tu contrato fue asignado."
                            "RECHAZADA" -> "Solicitud No Aprobada"
                            else -> req.estado
                        }
                        Text(
                            text = statusText,
                            fontSize = 13.sp,
                            color = if (req.estado == "APROBADA") PaidStatusText else TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 1. MI PROPIEDAD ALQUILADA
        Text(
            text = "Propiedad en Alquiler",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (property != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PropertyCard(
                        property = property,
                        onClick = { onNavigateToPropertyDetail(property.id) },
                        modifier = Modifier.fillMaxWidth(),
                        estado = "ALQUILADO_POR_TI"
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏠", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aún no tienes una propiedad asignada",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Contacta a tu propietario para que registre tu contrato con el correo: ${user?.email}",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 2. MI CONTRATO VIGENTE
        if (contract != null) {
            Text(
                text = "Mi Contrato Vigente",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

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
                        Text("Período del Contrato", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        StatusBadge(status = contract.estado)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Fecha de Inicio: ${contract.fechaInicio}", fontSize = 14.sp, color = TextSecondary)
                    Text("• Fecha de Fin: ${contract.fechaFin}", fontSize = 14.sp, color = TextSecondary)
                    Text("• Renta mensual: ${formatPrecio(contract.montoMensual)}", fontSize = 14.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // 3. MIS PAGOS Y CUOTAS
        Text(
            text = "Mis Pagos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (payments.isNotEmpty()) {
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
                        Text("Pagado", fontSize = 13.sp, color = PaidStatusText, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatPrecio(totalPagado), fontSize = 18.sp, color = PaidStatusText, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = PendingStatusBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pendiente", fontSize = 13.sp, color = PendingStatusText, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatPrecio(totalPendiente), fontSize = 18.sp, color = PendingStatusText, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            payments.forEach { payment ->
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
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Vencimiento: ${payment.fecha}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                if (!isPending && payment.fechaPagoReal.isNotBlank()) {
                                    Text(
                                        text = "Pagado el: ${payment.fechaPagoReal}",
                                        fontSize = 12.sp,
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
                                    selectedTenantPaymentForUpload = payment
                                    tenantVoucherUri = ""
                                    tenantPaymentDateInput = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = InputShape,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AirbnbPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Adjuntar Voucher de Pago", fontWeight = FontWeight.Bold)
                            }
                        } else if (payment.comprobanteUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    selectedTenantVoucherForView = payment.comprobanteUrl
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
        } else if (property != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay registros de pagos asociados.", fontSize = 14.sp, color = TextSecondary)
                }
            }
        }

        // 4. EXPLORAR INMUEBLES DISPONIBLES
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Explorar Inmuebles",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            TextButton(onClick = onNavigateToPropertyList) {
                Text("Ver todos", color = AirbnbPrimary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        val disponiblesProps = uiState.properties.filter { !it.estado.equals("OCUPADA", ignoreCase = true) }
        if (disponiblesProps.isNotEmpty()) {
            disponiblesProps.take(4).forEach { prop ->
                PropertyCard(
                    property = prop,
                    onClick = { onNavigateToPropertyDetail(prop.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        } else {
            Text(
                text = "No hay inmuebles disponibles en este momento.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }

    // Modal para que el Inquilino adjunte Voucher de Pago
    if (selectedTenantPaymentForUpload != null) {
        val payment = selectedTenantPaymentForUpload!!
        AlertDialog(
            onDismissRequest = { selectedTenantPaymentForUpload = null },
            shape = CardShape,
            containerColor = Color.White,
            title = {
                Text(
                    text = "Adjuntar Comprobante de Pago",
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
                        text = "Monto a Pagar: ${formatPrecio(payment.monto)}",
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AlquiTextField(
                        value = tenantPaymentDateInput,
                        onValueChange = { tenantPaymentDateInput = it },
                        label = "Fecha de Realización del Pago (AAAA-MM-DD)"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Foto del Voucher o Transferencia:",
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
                                tenantVoucherPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        shape = InputShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (tenantVoucherUri.isNotBlank()) {
                                AsyncImage(
                                    model = getImageModel(tenantVoucherUri),
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
                    TextButton(onClick = { selectedTenantPaymentForUpload = null }) {
                        Text("Cancelar", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanDate = tenantPaymentDateInput.trim()
                            if (cleanDate.isBlank()) {
                                Toast.makeText(context, "Ingrese la fecha de realización del pago.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (parseFlexibleDate(cleanDate) == null) {
                                Toast.makeText(context, "Formato de fecha inválido (AAAA-MM-DD).", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (tenantVoucherUri.isBlank()) {
                                Toast.makeText(context, "Por favor adjunte la foto del comprobante de pago.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val payId = payment.id
                            val userEmailStr = user?.email ?: ""
                            selectedTenantPaymentForUpload = null

                            propertyViewModel?.uploadTenantPaymentVoucher(
                                paymentId = payId,
                                fechaPagoReal = cleanDate,
                                comprobanteUrl = tenantVoucherUri,
                                userEmail = userEmailStr,
                                onSuccess = {
                                    Toast.makeText(context, "¡Comprobante enviado al propietario!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AirbnbPrimary),
                        shape = ButtonShape
                    ) {
                        Text("Enviar Comprobante", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // Modal Lightbox para Ver Comprobante
    if (selectedTenantVoucherForView != null) {
        val voucherUrl = selectedTenantVoucherForView!!
        AlertDialog(
            onDismissRequest = { selectedTenantVoucherForView = null },
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
                TextButton(onClick = { selectedTenantVoucherForView = null }) {
                    Text("Cerrar", fontWeight = FontWeight.Bold, color = AirbnbPrimary)
                }
            }
        )
    }

    // Modal de Notificaciones para Inquilino
    if (showTenantNotifications) {
        AlertDialog(
            onDismissRequest = { showTenantNotifications = false },
            shape = CardShape,
            containerColor = Color.White,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mis Notificaciones", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    if (uiState.unreadNotificationsCount > 0) {
                        Text("${uiState.unreadNotificationsCount} nuevas", fontSize = 12.sp, color = AirbnbPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (uiState.notifications.isEmpty()) {
                        Text("No tienes notificaciones por el momento.", fontSize = 14.sp, color = TextSecondary)
                    } else {
                        uiState.notifications.forEach { notif ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!notif.leida) Color(0xFFFFF8F8) else Color(0xFFF9F9F9)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(notif.titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(notif.fecha, fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(notif.mensaje, fontSize = 13.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.End) {
                    if (uiState.unreadNotificationsCount > 0) {
                        TextButton(
                            onClick = {
                                propertyViewModel?.markNotificationsAsRead(user?.uid ?: "")
                            }
                        ) {
                            Text("Marcar como leídas", color = AirbnbPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = { showTenantNotifications = false }) {
                        Text("Cerrar", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}
