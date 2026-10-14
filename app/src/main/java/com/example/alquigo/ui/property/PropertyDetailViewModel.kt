package com.example.alquigo.ui.property

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alquigo.data.model.Contract
import com.example.alquigo.data.model.Payment
import com.example.alquigo.data.model.Property
import com.example.alquigo.data.model.Tenant
import com.example.alquigo.data.repository.PropertyRepository
import com.example.alquigo.data.repository.TenantRepository
import kotlinx.coroutines.launch

data class PropertyDetailUiState(
    val isLoading: Boolean = true,
    val property: Property? = null,
    val activeContract: Contract? = null,
    val currentTenant: Tenant? = null,
    val recentPayments: List<Payment> = emptyList(),
    val errorMessage: String? = null
)

class PropertyDetailViewModel(
    private val propertyRepository: PropertyRepository = PropertyRepository(),
    private val tenantRepository: TenantRepository = TenantRepository()
) : ViewModel() {

    private val _uiState = mutableStateOf(PropertyDetailUiState())
    val uiState: State<PropertyDetailUiState> = _uiState

    fun loadPropertyDetail(propertyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // 1. Obtener la Propiedad
            val propertyResult = propertyRepository.getPropertyById(propertyId)
            val property = propertyResult.getOrNull()

            if (property == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Propiedad no encontrada"
                )
                return@launch
            }

            // 2. Obtener el Contrato Activo
            val contractResult = propertyRepository.getActiveContractForProperty(propertyId)
            val contract = contractResult.getOrNull()

            var tenant: Tenant? = null
            var payments: List<Payment> = emptyList()

            if (contract != null) {
                // 3. Obtener el Inquilino Actual
                if (contract.tenantId.isNotBlank()) {
                    val tenantResult = tenantRepository.getTenantById(contract.tenantId)
                    tenant = tenantResult.getOrNull()
                }

                // 4. Obtener Pagos del Contrato
                val paymentsResult = propertyRepository.getRecentPaymentsForContract(contract.id)
                payments = paymentsResult.getOrNull() ?: emptyList()
            } else {
                // Si no hay contrato activo, obtener todos los pagos de la propiedad
                val paymentsResult = propertyRepository.getAllPaymentsForProperty(propertyId)
                payments = paymentsResult.getOrNull() ?: emptyList()
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                property = property,
                activeContract = contract,
                currentTenant = tenant,
                recentPayments = payments
            )
        }
    }

    fun markPaymentAsPaid(
        paymentId: String,
        fechaPagoReal: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val propertyId = _uiState.value.property?.id ?: ""
            propertyRepository.markPaymentAsPaid(paymentId, fechaPagoReal).fold(
                onSuccess = {
                    if (propertyId.isNotBlank()) {
                        loadPropertyDetail(propertyId)
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    onError(error.message ?: "Error al marcar el pago como pagado")
                }
            )
        }
    }
}
