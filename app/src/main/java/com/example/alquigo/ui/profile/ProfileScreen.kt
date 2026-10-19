package com.example.alquigo.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.CardTravel
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.HouseSiding
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alquigo.ui.auth.AuthViewModel
import com.example.alquigo.ui.components.AvatarInitial
import com.example.alquigo.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onNavigateToEditProfile: () -> Unit = {},
    hazeState: HazeState? = null
) {
    val currentUser = viewModel.isUserLoggedIn()
    val userState by viewModel.userState
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    val authEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
    val emailUser = authEmail?.substringBefore("@") ?: ""
    val formattedEmailUser = emailUser.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    val userName = when {
        !userState?.nombre.isNullOrBlank() -> "${userState?.nombre} ${userState?.apellido ?: ""}".trim()
        formattedEmailUser.isNotBlank() -> formattedEmailUser
        else -> "Usuario AlquiGo"
    }
    val userInitial = userName.take(1).uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .then(if (hazeState != null) Modifier.hazeSource(state = hazeState) else Modifier)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 120.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Notificaciones (Campana)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F2F2))
                    .clickable { Toast.makeText(context, "No tienes notificaciones nuevas", Toast.LENGTH_SHORT).show() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notificaciones",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Título "Perfil" (32sp)
        Text(
            text = "Perfil",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tarjeta del Usuario
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderLight, CardShape),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarInitial(
                    name = userName,
                    size = 90.dp,
                    fontSize = 40
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = userName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Propietario",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fila de Tarjetas Accesos Rápido (Viajes anteriores / Conexiones)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .border(1.dp, BorderLight, CardShape)
                    .clickable { Toast.makeText(context, "Viajes anteriores", Toast.LENGTH_SHORT).show() },
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("NOVEDAD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Column(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CardTravel,
                            contentDescription = null,
                            tint = AirbnbPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Viajes anteriores", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .border(1.dp, BorderLight, CardShape)
                    .clickable { Toast.makeText(context, "Conexiones", Toast.LENGTH_SHORT).show() },
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("NOVEDAD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Column(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.People,
                            contentDescription = null,
                            tint = AirbnbPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Conexiones", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banner Promocional "Conviértete en anfitrión"
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderLight, CardShape)
                .clickable { Toast.makeText(context, "Modo anfitrión disponible", Toast.LENGTH_SHORT).show() },
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.HouseSiding,
                    contentDescription = null,
                    tint = AirbnbPrimary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Conviértete en anfitrión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Empieza a anfitrionar y genera ingresos adicionales, ¡es muy sencillo!",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botón Flotante Oscuro
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkButtonBg)
                    .clickable { Toast.makeText(context, "Cambiando a modo anfitrión", Toast.LENGTH_SHORT).show() }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.SwapHoriz,
                        contentDescription = null,
                        tint = DarkButtonText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cambiar a modo anfitrión",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkButtonText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Configuración de la cuenta
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToEditProfile() }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Configuración de la cuenta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Divider(color = BorderLight, thickness = 0.5.dp)

        // Cerrar sesión
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.logout() }
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ExitToApp,
                    contentDescription = null,
                    tint = AirbnbPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Cerrar sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AirbnbPrimary
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
