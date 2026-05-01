package com.jrg_upm.tennisrank.viewModel.Auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrg_upm.tennisrank.supabase.registerUser
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    // Nos creamos las variables state para el correo y la contraseña que debe introducir el usuario:
    var user_name by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordRepeated by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    // Función de lógica de negocio
    fun onRegisterClick(onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Validamos que los campos introducidos sean correctos:
        if (email.isBlank() || password.isBlank() || passwordRepeated.isBlank()) {
            onError("Por favor, rellena todos los campos")
            return
        }
        if (password != passwordRepeated) {
            onError("Las contraseñas no coinciden")
            return
        }
        if (password.length < 8) {
            onError("La contraseña introducida es muy corta (mínimo 8 caracteres)")
            return
        }

        // Una vez hemos validado que los campos sean correctos ejecutamos el registro
        viewModelScope.launch {
            isLoading = true
            try {
                val success = registerUser(email.trim(), password.trim(), user_name)
                if (success) {
                    onSuccess()
                } else {
                    onError("Error al registrarse, el usuario ya existe o hubo un problema de red")
                }
            } catch (e: Exception) {
                onError("Error inesperado: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}