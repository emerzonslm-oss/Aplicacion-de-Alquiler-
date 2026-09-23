package com.example.alquigo.ui.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alquigo.data.model.User
import com.example.alquigo.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun isUserLoggedIn() = repository.isUserLoggedIn()

    fun login(email: String, contrasena: String) {
        if (email.isBlank() || contrasena.isBlank()) {
            _authState.value = AuthState.Error("Por favor complete todos los campos")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(email, contrasena).fold(
                onSuccess = {
                    _authState.value = AuthState.Success
                    _uiEvent.emit(UiEvent.NavigateToHome)
                },
                onFailure = {
                    _authState.value = AuthState.Error(it.message ?: "Error desconocido al iniciar sesión")
                }
            )
        }
    }

    fun register(
        nombre: String,
        apellido: String,
        email: String,
        telefono: String,
        contrasena: String,
        confirmarContrasena: String
    ) {
        if (nombre.isBlank() || apellido.isBlank() || email.isBlank() || telefono.isBlank() || contrasena.isBlank()) {
            _authState.value = AuthState.Error("Por favor complete todos los campos")
            return
        }

        if (contrasena != confirmarContrasena) {
            _authState.value = AuthState.Error("Las contraseñas no coinciden")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Formato de correo inválido")
            return
        }
        if (contrasena.length <6){
            _authState.value = AuthState.Error("La contraseña es muy pequeña")
            return
        }
        if (telefono.length !=9) {
            _authState.value =
                AuthState.Error("La cantidad de numeros del telefono no es la suficiente ")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = User(nombre = nombre, apellido = apellido, email = email, telefono = telefono)
            repository.register(user, contrasena).fold(
                onSuccess = {
                    _authState.value = AuthState.Success
                    _uiEvent.emit(UiEvent.NavigateToHome)
                },
                onFailure = {
                    _authState.value = AuthState.Error(it.message ?: "Error desconocido al registrarse")
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateToLogin)
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class UiEvent {
    object NavigateToHome : UiEvent()
    object NavigateToLogin : UiEvent()
}
