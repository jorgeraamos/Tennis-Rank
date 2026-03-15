package com.jrg_upm.tennisrank.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jrg_upm.tennisrank.model.Jugador


// Clase "Fábrica" para el ProfileViewModel
// Se necesita ya que el ViewModel requiere un parámetro (jugador) y Android,
// por defecto, solo sabe crear ViewModels con constructores vacíos.
class ProfileViewModelFactory(val jugador: Jugador?) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verificamos que la clase solicitada sea nuestro ProfileViewModel
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            // Retornamos la instancia con el parámetro inyectado
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(jugador) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida para esta fábrica")
    }
}