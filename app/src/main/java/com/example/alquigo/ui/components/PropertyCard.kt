package com.example.alquigo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.alquigo.data.model.Property
import com.example.alquigo.ui.theme.AirbnbPrimary
import com.example.alquigo.ui.theme.BorderLight
import com.example.alquigo.ui.theme.TextPrimary
import com.example.alquigo.ui.theme.TextSecondary
import dev.chrisbanes.haze.HazeState

@Composable
fun PropertyCard(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    estado: String? = null,
    isFavorite: Boolean = false,
    hazeState: HazeState? = null,
    onFavoriteClick: (() -> Unit)? = null
) {
    var localIsFavorite by remember(property.id, isFavorite) { mutableStateOf(isFavorite) }
    val favState = if (onFavoriteClick != null) isFavorite else localIsFavorite

    Column(
        modifier = modifier
            .clickable { onClick() }
    ) {
        // Foto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(0.5.dp, BorderLight, RoundedCornerShape(20.dp))
                .background(Color(0xFFF0F0F0))
        ) {
            if (property.imagenUri.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = getImageModel(property.imagenUri),
                    contentDescription = property.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF0F0F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HomeWork,
                                contentDescription = null,
                                tint = Color(0xFFB0B0B0),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF0F0F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HomeWork,
                                contentDescription = null,
                                tint = Color(0xFFB0B0B0),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HomeWork,
                        contentDescription = null,
                        tint = Color(0xFFB0B0B0),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Insignia: Píldora BLANCA sólida en esquina superior izquierda
            if (!estado.isNullOrBlank()) {
                val statusText = if (estado.equals("OCUPADA", ignoreCase = true) || estado.equals("ACTIVO", ignoreCase = true)) "Ocupada" else "Disponible"
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, start = 10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(0.5.dp, BorderLight, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Corazón en esquina superior derecha
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable {
                        localIsFavorite = !localIsFavorite
                        onFavoriteClick?.invoke()
                    }
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = if (favState) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (favState) Color(0xFFFF385C) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Línea 1: Título 16sp Bold
        Text(
            text = property.titulo.ifBlank { "Propiedad en ${property.direccion}" },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Línea 2: tipo · dirección 14sp
        Text(
            text = "${property.tipo} · ${property.direccion}",
            fontSize = 14.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Línea 3: "S/ 100 por mes"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${formatPrecio(property.precio)} ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AirbnbPrimary
            )
            Text(
                text = "por mes",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}
