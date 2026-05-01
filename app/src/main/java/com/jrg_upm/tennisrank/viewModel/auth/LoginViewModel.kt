package com.jrg_upm.tennisrank.viewModel.Auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrg_upm.tennisrank.supabase.loginUser
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    // Nos creamos las variables state para el correo y la contraseña que debe introducir el usuario:
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    // La lógica de negocio vive aquí
    fun onLoginClick(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onError("Por favor, rellena todos los campos")
            return
        }

        viewModelScope.launch {
            // Función que verifica el inicio de sesión:
            // Usamos .trim() para eliminar cualquier espacio accidental
            isLoading = true
            val success = loginUser(email.trim(), password.trim())
            isLoading = false

            if (success) {
                onSuccess()
            } else {
                onError("Usuario o contraseña incorrectos")
            }
        }
    }
}