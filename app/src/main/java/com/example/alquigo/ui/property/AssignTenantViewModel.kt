package com.example.alquigo.ui.property

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alquigo.data.model.Property
import com.example.alquigo.data.model.Tenant
import com.example.alquigo.data.repository.PropertyRepository
import com.example.alquigo.data.repository.TenantRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AssignTenantUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val property: Property? = null,
    val tenants: List<Tenant> = emptyList(),
    val selectedTenant: Tenant? = null,
    val fechaInicio: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
    val fechaFin: String = LocalDate.now().plusYears(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
    val montoMensual: String = "",
    val errorMessage: String? = null
)

class AssignTenantViewModel(
    private val propertyRepository: PropertyRepository = PropertyRepository(),
    private val tenantRepository: TenantRepository = TenantRepository()
) : ViewModel() {

    private val _uiState = mutableStateOf(AssignTenantUiState())
    val uiState: State<AssignTenantUiState> = _uiState

    fun loadData(propertyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val propertyResult = propertyRepository.getPropertyById(propertyId)
            val property = propertyResult.getOrNull()

            val tenantsResult = tenantRepository.getAllTenants()
            val tenants = tenantsResult.getOrNull() ?: emptyList()

            val initialPrice = property?.precio?.let { if (it > 0) it.toString() else "" } ?: ""

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                property = property,
                tenants = tenants,
                montoMensual = initialPrice
            )
        }
    }

    fun selectTenant(tenant: Tenant) {
        _uiState.value = _uiState.value.copy(selectedTenant = tenant)
    }

    fun updateFechaInicio(fecha: String) {
        _uiState.value = _uiState.value.copy(fechaInicio = fecha)
    }

    fun updateFechaFin(fecha: String) {
        _uiState.value = _uiState.value.copy(fechaFin = fecha)
    }

    fun updateMontoMensual(monto: String) {
        _uiState.value = _uiState.value.copy(montoMensual = monto)
    }

    fun createContract(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val state = _uiState.value
        val property = state.property

        if (property == null) {
            onError("Propiedad no válida.")
            return
        }

        val tenant = state.selectedTenant
        if (tenant == null) {
            onError("Debes seleccionar un inquilino.")
            return
        }

        if (state.fechaInicio.isBlank()) {
            onError("La fecha de inicio es requerida.")
            return
        }

        if (state.fechaFin.isBlank()) {
            onError("La fecha de fin es requerida.")
            return
        }

        val monto = state.montoMensual.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            onError("Ingresa un monto mensual válido.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)

            propertyRepository.createContractAndAssignTenant(
                propertyId = property.id,
                tenantId = tenant.id,
                fechaInicio = state.fechaInicio,
                fechaFin = state.fechaFin,
                montoMensual = monto
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    onError(error.message ?: "Error al asignar inquilino y crear contrato.")
                }
            )
        }
    }
}
