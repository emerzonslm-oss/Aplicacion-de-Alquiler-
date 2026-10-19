package com.example.alquigo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alquigo.ui.components.*
import com.example.alquigo.ui.property.PropertyViewModel
import com.example.alquigo.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun HomeScreen(
    propertyViewModel: PropertyViewModel,
    onNavigateToAddProperty: () -> Unit,
    onNavigateToPropertyList: () -> Unit,
    onNavigateToPropertyDetail: (String) -> Unit,
    hazeState: HazeState? = null
) {
    val uiState by propertyViewModel.uiState
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
