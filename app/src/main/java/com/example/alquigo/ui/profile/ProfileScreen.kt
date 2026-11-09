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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    val userRoleTitle = if (userState?.rol == "INQUILINO") "Inquilino" else "Propietario / Admin"

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
                    text = userRoleTitle,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📌 BOTÓN/OPCIÓN QUE ABRE UNA NUEVA PANTALLA CON INPUTS: Al hacer clic en Configuración de la cuenta llama a onNavigateToEditProfile que abre la pantalla EditProfileScreen (donde se dibujan las cajas de texto/inputs para modificar el nombre, apellido y teléfono)
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
