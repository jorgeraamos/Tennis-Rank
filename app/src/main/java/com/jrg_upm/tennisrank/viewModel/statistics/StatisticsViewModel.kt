package com.jrg_upm.tennisrank.viewModel.statistics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jrg_upm.tennisrank.model.Partido
import com.jrg_upm.tennisrank.model.Jornada
import com.jrg_upm.tennisrank.model.Set

class StatisticsViewModel : ViewModel() {

    // Variable de estado para calcular las estadísticas del jugador
    var totalPartidos by mutableStateOf(0)
    var partidosGanados by mutableStateOf(0)
    var totalSets by mutableStateOf(0)
    var setsGanados by mutableStateOf(0)
    var totalJuegos by mutableStateOf(0)
    var juegosGanados by mutableStateOf(0)
    var racha by mutableStateOf(emptyList<Boolean>())
    var tieBreaksGanados by mutableStateOf(0)
    var tieBreaksTotales by mutableStateOf(0)
    var remontadas by mutableStateOf(0)
    var totalPartidosComoVisitante by mutableStateOf(0)
    var victoriasComoVisitante by mutableStateOf(0)

    fun calcularEstadisticas(jugadorId: String, jornadas: List<Pair<Jornada, Pair<Partido, List<Set>>>>) {
        val listaPartidos = jornadas.filter { it.first.estado == "Finalizada" }.map { it.second }

        if (listaPartidos.isEmpty()) return

        // Reiniciamos contadores
        totalPartidos = listaPartidos.size
        partidosGanados = listaPartidos.count { it.first.idGanador == jugadorId }
        racha = listaPartidos.take(5).map { it.first.idGanador == jugadorId }.reversed()

        // Utilizamos variables auxiliares en caso de que pudiera cambiar el jugador para no duplicar los valores
        // G para ganados y T para totales
        var auxSetsG = 0; var auxSetsT = 0
        var auxJuegosG = 0; var auxJuegosT = 0
        var auxRem = 0; var auxTBG = 0; var auxTBT = 0
        var auxVisitT = 0; var auxVisitV = 0

        listaPartidos.forEach { (partido, sets) ->
            // Miramos si el jugador ha sido visitante en cada partido
            if (partido.idJugador2 == jugadorId) {
                auxVisitT++
                if (partido.idGanador == jugadorId) auxVisitV++
            }

            // Miramos cada set:
            sets.forEach { set ->
                val esJ1 = partido.idJugador1 == jugadorId
                val juegosUsuario = if (esJ1) set.juegosJugador1 else set.juegosJugador2
                val juegosRival = if (esJ1) set.juegosJugador2 else set.juegosJugador1

                // Sets y Remontadas
                if (juegosUsuario > juegosRival) {
                    auxSetsG++
                    auxSetsT++
                } else if (juegosUsuario < juegosRival) {
                    auxSetsT++
                    // Remontada: Perdió el 1º set pero ganó el partido
                    if (set.numeroSet == 1 && partido.idGanador == jugadorId) auxRem++
                }

                // Juegos
                auxJuegosG += juegosUsuario
                auxJuegosT += (juegosUsuario + juegosRival)

                // Tie-breaks
                if ((juegosUsuario == 7 && juegosRival == 6) || (juegosUsuario == 6 && juegosRival == 7)) {
                    auxTBT++
                    if (juegosUsuario == 7) auxTBG++
                }
            }
        }

        // Asignación final a las variables de estado
        setsGanados = auxSetsG; totalSets = auxSetsT
        juegosGanados = auxJuegosG; totalJuegos = auxJuegosT
        remontadas = auxRem; tieBreaksGanados = auxTBG; tieBreaksTotales = auxTBT
        totalPartidosComoVisitante = auxVisitT; victoriasComoVisitante = auxVisitV
    }
}