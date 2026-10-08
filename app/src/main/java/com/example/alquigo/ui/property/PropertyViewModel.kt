package com.example.alquigo.ui.property

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alquigo.data.model.Property
import com.example.alquigo.data.repository.PropertyRepository
import kotlinx.coroutines.launch

data class PropertyUiState(
    val properties: List<Property> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedFilter: String = "Todos"
)

class PropertyViewModel(
    private val repository: PropertyRepository = PropertyRepository()
) : ViewModel() {

    private val _uiState = mutableStateOf(PropertyUiState())
    val uiState: State<PropertyUiState> = _uiState

    fun loadProperties(filter: String = _uiState.value.selectedFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                selectedFilter = filter
            )
            val result = if (filter == "Todos") {
                repository.getAllProperties()
            } else {
                repository.getPropertiesByType(filter)
            }

            result.fold(
                onSuccess = { propertiesList ->
                    _uiState.value = _uiState.value.copy(
                        properties = propertiesList,
                        isLoading = false
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
}
