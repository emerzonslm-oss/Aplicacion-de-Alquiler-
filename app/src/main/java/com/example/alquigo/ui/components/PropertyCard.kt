package com.example.alquigo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.alquigo.ui.theme.TextPrimary
import com.example.alquigo.ui.theme.TextSecondary
import dev.chrisbanes.haze.HazeState

@Composable
fun PropertyCard(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    estado: String? = null,
    hazeState: HazeState? = null,
    onFavoriteClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .width(170.dp)
            .clickable { onClick() }
    ) {
        // Foto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF0F0F0))
        ) {
            if (property.imagenUri.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = property.imagenUri,
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
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            // Corazón BLANCO de 26dp en esquina superior derecha sin fondo
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .clickable { onFavoriteClick?.invoke() }
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Línea 1: Título 16sp Medium #222222 (máx 2 líneas)
        Text(
            text = property.titulo.ifBlank { "Propiedad en ${property.direccion}" },
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Línea 2: tipo · dirección 15sp #6A6A6A
        Text(
            text = "${property.tipo} · ${property.direccion}",
            fontSize = 15.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Línea 3: "S/ 100 por mes" 15sp #6A6A6A con monto en Medium #222222
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${formatPrecio(property.precio)} ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "por mes",
                fontSize = 15.sp,
                color = TextSecondary
            )
        }
    }
}
