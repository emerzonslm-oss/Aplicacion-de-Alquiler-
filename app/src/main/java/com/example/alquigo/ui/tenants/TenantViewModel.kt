package com.example.alquigo.ui.tenants

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alquigo.data.model.Tenant
import com.example.alquigo.data.repository.TenantRepository
import kotlinx.coroutines.launch

data class TenantUiState(
    val isLoading: Boolean = false,
    val tenants: List<Tenant> = emptyList(),
    val errorMessage: String? = null
)

class TenantViewModel(
    private val repository: TenantRepository = TenantRepository()
) : ViewModel() {

    private val _uiState = mutableStateOf(TenantUiState())
    val uiState: State<TenantUiState> = _uiState

    fun loadTenants() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.getAllTenants().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, tenants = list)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar inquilinos"
                    )
                }
            )
        }
    }

    fun saveTenant(
        nombre: String,
        dni: String,
        telefono: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanNombre = nombre.trim()
        val cleanDni = dni.trim()
        val cleanTelefono = telefono.trim()
        val cleanEmail = email.trim()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Validar existencia de DNI (sin duplicados)
            val existsResult = repository.checkDniExists(cleanDni)
            if (existsResult.isSuccess && existsResult.getOrNull() == true) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onError("El DNI '$cleanDni' ya se encuentra registrado.")
                return@launch
            }

            val tenant = Tenant(
                nombre = cleanNombre,
                dni = cleanDni,
                telefono = cleanTelefono,
                email = cleanEmail
            )

            repository.saveTenant(tenant).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadTenants()
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError(error.message ?: "Error al guardar el inquilino.")
                }
            )
        }
    }
}
