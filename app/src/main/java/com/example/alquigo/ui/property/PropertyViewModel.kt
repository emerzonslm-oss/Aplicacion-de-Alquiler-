package com.example.alquigo.ui.property

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alquigo.data.model.DuePaymentInfo
import com.example.alquigo.data.model.Property
import com.example.alquigo.data.model.PropertyRequest
import com.example.alquigo.data.model.TenantRentalInfo
import com.example.alquigo.data.repository.PropertyRepository
import com.example.alquigo.data.repository.TenantRepository
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val totalProperties: Int = 0,
    val occupiedProperties: Int = 0,
    val availableProperties: Int = 0,
    val occupancyPercentage: Int = 0,
    val monthlyEarnings: Double = 0.0
)

data class PropertyUiState(
    val properties: List<Property> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedFilter: String = "Todos",
    val favoritePropertyIds: Set<String> = emptySet(),
    val metrics: DashboardMetrics = DashboardMetrics(),
    val duePayments: List<DuePaymentInfo> = emptyList(),
    val tenantRentalInfo: TenantRentalInfo? = null,
    val pendingRequests: List<PropertyRequest> = emptyList(),
    val tenantRequests: List<PropertyRequest> = emptyList()
)

class PropertyViewModel(
    private val repository: PropertyRepository = PropertyRepository(),
    private val tenantRepository: TenantRepository = TenantRepository()
) : ViewModel() {

    private val _uiState = mutableStateOf(PropertyUiState())
    val uiState: State<PropertyUiState> = _uiState

    fun toggleFavorite(propertyId: String) {
        val currentFavorites = _uiState.value.favoritePropertyIds.toMutableSet()
        if (currentFavorites.contains(propertyId)) {
            currentFavorites.remove(propertyId)
        } else {
            currentFavorites.add(propertyId)
        }
        _uiState.value = _uiState.value.copy(favoritePropertyIds = currentFavorites)

        if (_uiState.value.selectedFilter == "Favoritos") {
            loadProperties("Favoritos")
        }
    }

    fun loadProperties(filter: String = _uiState.value.selectedFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                selectedFilter = filter
            )
            val result = if (filter == "Todos" || filter == "Favoritos") {
                repository.getAllProperties()
            } else {
                repository.getPropertiesByType(filter)
            }

            result.fold(
                onSuccess = { propertiesList ->
                    val filteredList = if (filter == "Favoritos") {
                        propertiesList.filter { _uiState.value.favoritePropertyIds.contains(it.id) }
                    } else {
                        propertiesList
                    }

                    val totalProps = propertiesList.size
                    val occupiedProps = propertiesList.count { it.estado.equals("OCUPADA", ignoreCase = true) }
                    val availableProps = propertiesList.count { !it.estado.equals("OCUPADA", ignoreCase = true) }
                    val percentage = if (totalProps > 0) ((occupiedProps.toDouble() / totalProps) * 100).toInt() else 0

                    val duePaymentsResult = repository.getDueAndOverduePaymentsForOwner()
                    val duePaymentsList = duePaymentsResult.getOrNull() ?: emptyList()

                    val earningsResult = repository.getMonthlyEarningsForOwner()
                    val monthlyTotal = earningsResult.getOrNull() ?: 0.0

                    _uiState.value = _uiState.value.copy(
                        properties = filteredList,
                        isLoading = false,
                        metrics = DashboardMetrics(
                            totalProperties = totalProps,
                            occupiedProperties = occupiedProps,
                            availableProperties = availableProps,
                            occupancyPercentage = percentage,
                            monthlyEarnings = monthlyTotal
                        ),
                        duePayments = duePaymentsList
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Error al cargar propiedades",
                        isLoading = false
                    )
                }
            )
        }
    }

    fun getPropertyById(id: String, onResult: (Property?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getPropertyById(id).fold(
                onSuccess = { property ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onResult(property)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onResult(null)
                }
            )
        }
    }

    fun loadTenantRentalInfo(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            tenantRepository.getTenantRentalInfo(email).fold(
                onSuccess = { info ->
                    _uiState.value = _uiState.value.copy(
                        tenantRentalInfo = info,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    fun saveProperty(
        property: Property,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.saveProperty(property).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadProperties()
                    onSuccess()
                },
                onFailure = { error ->
                    val msg = error.message ?: "Error al guardar propiedad"
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(msg)
                }
            )
        }
    }

    fun updateProperty(
        property: Property,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.updateProperty(property).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadProperties()
                    onSuccess()
                },
                onFailure = { error ->
                    val msg = error.message ?: "Error al actualizar propiedad"
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(msg)
                }
            )
        }
    }

    fun deleteProperty(
        propertyId: String,
        onBlocked: (String) -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 1. Validar si tiene un contrato activo
            val hasContractResult = repository.hasActiveContract(propertyId)

            hasContractResult.fold(
                onSuccess = { hasActive ->
                    if (hasActive) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        onBlocked("No se puede eliminar la propiedad porque tiene un contrato activo.")
                    } else {
                        // 2. Si no tiene contrato activo, proceder a eliminar
                        repository.deleteProperty(propertyId).fold(
                            onSuccess = {
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                loadProperties()
                                onSuccess()
                            },
                            onFailure = { error ->
                                val msg = error.message ?: "Error al eliminar la propiedad"
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                onError(msg)
                            }
                        )
                    }
                },
                onFailure = { error ->
                    val msg = error.message ?: "Error al verificar contratos"
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(msg)
                }
            )
        }
    }

    fun uploadTenantPaymentVoucher(
        paymentId: String,
        fechaPagoReal: String,
        comprobanteUrl: String,
        userEmail: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.markPaymentAsPaid(paymentId, fechaPagoReal, comprobanteUrl).fold(
                onSuccess = {
                    if (userEmail.isNotBlank()) {
                        loadTenantRentalInfo(userEmail)
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    onError(error.message ?: "Error al enviar el comprobante de pago")
                }
            )
        }
    }

    // --- SOLICITUDES DE PROPIEDADES ---

    fun loadPendingRequests() {
        viewModelScope.launch {
            repository.getAllPendingRequests().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(pendingRequests = list)
                },
                onFailure = { /* opcional log */ }
            )
        }
    }

    fun loadTenantRequests(tenantUid: String) {
        if (tenantUid.isBlank()) return
        viewModelScope.launch {
            repository.getRequestsForTenant(tenantUid).fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(tenantRequests = list)
                },
                onFailure = { /* opcional log */ }
            )
        }
    }

    fun sendPropertyRequest(
        property: Property,
        tenantUid: String,
        tenantName: String,
        tenantEmail: String,
        tenantPhone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val request = PropertyRequest(
                propertyId = property.id,
                propertyTitle = property.titulo,
                propertyTipo = property.tipo,
                propertyPrecio = property.precio,
                propertyImagenUri = property.imagenUri,
                tenantUid = tenantUid,
                tenantName = tenantName,
                tenantEmail = tenantEmail,
                tenantPhone = tenantPhone
            )
            repository.createPropertyRequest(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadTenantRequests(tenantUid)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(error.message ?: "Error al solicitar la propiedad.")
                }
            )
        }
    }

    fun approveRequest(
        request: PropertyRequest,
        fechaInicio: String,
        fechaFin: String,
        montoMensual: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.approvePropertyRequest(request, fechaInicio, fechaFin, montoMensual).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadProperties()
                    loadPendingRequests()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(error.message ?: "Error al aprobar la solicitud.")
                }
            )
        }
    }

    fun rejectRequest(
        requestId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.rejectPropertyRequest(requestId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadPendingRequests()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(error.message ?: "Error al rechazar la solicitud.")
                }
            )
        }
    }
}
