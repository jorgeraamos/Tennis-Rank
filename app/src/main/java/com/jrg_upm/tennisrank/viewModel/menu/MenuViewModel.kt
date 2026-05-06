package com.jrg_upm.tennisrank.viewModel.menu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrg_upm.tennisrank.model.Jornada
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.model.Participante
import com.jrg_upm.tennisrank.model.Partido
import com.jrg_upm.tennisrank.model.Set
import com.jrg_upm.tennisrank.supabase.getAllParticipantes
import com.jrg_upm.tennisrank.supabase.getCurrentPlayer
import com.jrg_upm.tennisrank.supabase.getEdicionJugador
import com.jrg_upm.tennisrank.supabase.getPartidosPorJornada
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {
    // Variables compartidas por distintas pantallas:

    // Estado del jugador
    var jugadorActual by mutableStateOf<Jugador?>(null)
        private set // Solo el ViewModel puede modificarlo

    // Variable estado para la lista de jugadores del ranking
    var listaRanking by mutableStateOf<List<Participante>>(emptyList())
        private set

    // Variable de estado en la que se guardan todos los partidos que ha jugado un jugador
    var listaJornadas by mutableStateOf(emptyList<Pair<Jornada, Pair<Partido, List<Set>>>>())

    // Estado para saber si estamos cargando los datos de Supabase
    var isLoading by mutableStateOf(false)
        private set

    // Estado para capturar errores de red o sesión
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Función para cargar o refrescar los datos
    fun cargarJugador() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val jugador = getCurrentPlayer() ?: throw Exception("Sesión no encontrada")
                jugadorActual = jugador

                val idEdicion = getEdicionJugador(jugador.id)
                    ?: throw Exception("No perteneces a ninguna edición activa")

                // Cargamos ranking y jornadas (podrías usar async/await para ir en paralelo)
                val ranking = getAllParticipantes(jugador.id, idEdicion)
                val jornadas = getPartidosPorJornada(jugador.id, idEdicion)

                if (ranking != null) {
                    listaRanking = ranking
                } else {
                    errorMessage = "Error al cargar el ranking"
                }

                // Si jornadas es null, simplemente dejamos la lista vacía,
                // no tiene por qué ser un error crítico de la app
                listaJornadas = jornadas ?: emptyList()

            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}