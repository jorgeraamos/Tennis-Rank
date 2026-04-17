package com.jrg_upm.tennisrank.viewModel.Menu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.supabase.getCurrentPlayer
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {
    // El estado del jugador ahora vive aquí
    var jugadorActual by mutableStateOf<Jugador?>(null)
        private set // Solo el ViewModel puede modificarlo

    // Función para cargar o refrescar los datos
    fun cargarJugador() {
        viewModelScope.launch {
            jugadorActual = getCurrentPlayer()
        }
    }
}