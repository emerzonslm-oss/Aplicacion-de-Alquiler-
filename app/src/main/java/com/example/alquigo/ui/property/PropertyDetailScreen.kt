package com.example.alquigo.ui.property

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.alquigo.ui.components.AvatarInitial
import com.example.alquigo.ui.components.StatusBadge
import com.example.alquigo.ui.components.formatPrecio
import com.example.alquigo.ui.components.glassSurface
import com.example.alquigo.ui.theme.*

@Composable
fun PropertyDetailScreen(
    propertyId: String,
    onNavigateBack: () -> Unit,
    viewModel: PropertyDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState

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
                    }
                }
            } else {
                Text(
                    text = "No hay ningún contrato activo.",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN: Últimos Pagos
            Text(
                text = "Últimos pagos",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.recentPayments.isNotEmpty()) {
                uiState.recentPayments.forEach { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = payment.concepto, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = payment.fecha, fontSize = 14.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = formatPrecio(payment.monto), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                StatusBadge(status = payment.estado)
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No hay pagos registrados asociados a este contrato.",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
