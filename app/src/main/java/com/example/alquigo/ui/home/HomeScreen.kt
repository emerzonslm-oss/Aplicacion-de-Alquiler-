package com.example.alquigo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alquigo.data.model.DuePaymentInfo
import com.example.alquigo.data.model.User
import com.example.alquigo.ui.auth.AuthViewModel
import com.example.alquigo.ui.components.*
import com.example.alquigo.ui.property.DashboardMetrics
import com.example.alquigo.ui.property.PropertyViewModel
import com.example.alquigo.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun HomeScreen(
    propertyViewModel: PropertyViewModel,
    authViewModel: AuthViewModel? = null,
    onNavigateToAddProperty: () -> Unit,
    onNavigateToPropertyList: () -> Unit,
    onNavigateToPropertyDetail: (String) -> Unit,
    hazeState: HazeState? = null
) {
    val uiState by propertyViewModel.uiState
    val userState by authViewModel?.userState ?: remember { mutableStateOf<User?>(null) }
    val isTenant = userState?.rol == "INQUILINO"
    val userEmail = userState?.email ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: ""

    if (isTenant) {
        LaunchedEffect(userEmail) {
            if (userEmail.isNotBlank()) {
                propertyViewModel.loadTenantRentalInfo(userEmail)
            }
        }
        TenantHomeScreenView(
            uiState = uiState,
            user = userState,
            hazeState = hazeState
        )
        return
    }

    val categories = listOf("Todos", "Favoritos", "Casa", "Apartamento", "Habitación", "Local Comercial")

    LaunchedEffect(Unit) {
        propertyViewModel.loadProperties()
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

        // Botones Discretos "Publicar" y "Ver catálogo"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
    hazeState: HazeState? = null
) {
    val info = uiState.tenantRentalInfo
    val property = info?.property
    val contract = info?.contract
    val payments = info?.payments ?: emptyList()

    val totalPagado = payments.filter { it.estado.equals("PAGADO", ignoreCase = true) }.sumOf { it.monto }
    val totalPendiente = payments.filter { it.estado.equals("PENDIENTE", ignoreCase = true) }.sumOf { it.monto }

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
            verticalAlignment = Alignment.CenterVertically
        ) {
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

        Spacer(modifier = Modifier.height(24.dp))

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
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        estado = "Alquilada"
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                            if (payment.estado.equals("PAGADO", ignoreCase = true) && payment.fechaPagoReal.isNotBlank()) {
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
    }
}
