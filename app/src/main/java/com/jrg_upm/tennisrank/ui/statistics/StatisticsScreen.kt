package com.jrg_upm.tennisrank.ui.statistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jrg_upm.tennisrank.model.Jornada
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.model.Partido
import com.jrg_upm.tennisrank.model.Set
import com.jrg_upm.tennisrank.supabase.getAllParticipantes
import com.jrg_upm.tennisrank.supabase.getPartidosPorJornada
import com.jrg_upm.tennisrank.ui.components.CircularProgressBar

@Composable
fun StatisticsScreen(jugadorActual: Jugador?) {
    // variable de estado en la que se guardan todos los partidos que ha jugado un jugador
    var listaPartidos by remember { mutableStateOf(emptyList<Pair<Partido, List<Set>>>()) }
    var totalPartios by remember { mutableStateOf(0) }
    var partidosGanados by remember { mutableStateOf(0) }
    var totalSets by remember { mutableStateOf(0) }
    var setsGanados by remember { mutableStateOf(0) }


    LaunchedEffect(jugadorActual) {
        // Cargamos todos los jugadores para el ranking
        if (jugadorActual != null) {
            val listaJornadas = getPartidosPorJornada(jugadorActual.id, estado = "Finalizada")
            listaPartidos = listaJornadas.map{it.second}
            if(listaPartidos.isNotEmpty()){
                // Obtenemos el número total de partidos que ha disputado el usuario
                totalPartios = listaPartidos.size
                // Obtenemos el número total de partidos que ha ganado
                partidosGanados = listaPartidos.count { (partido, _ ) ->
                    partido.idGanador == jugadorActual.id }

                // Ahora recogemos el total de sets y los sets ganados por el jugador:
                // Utilizamos variables auxiliares en caso de que pudiera cambiar el jugador para no duplicar los valores
                var auxSetsGanados = 0
                var auxTotalSets = 0
                listaPartidos.forEach { (partido, sets) ->
                    sets.forEach { set ->
                        // Comprobamos si el jugadorActual es el Jugador 1 o el 2 en este partido
                        if (partido.idJugador1 == jugadorActual.id) {
                            if (set.juegosJugador1 > set.juegosJugador2){
                                auxSetsGanados++
                                auxTotalSets++
                            }else if(set.juegosJugador1 < set.juegosJugador2){
                                // Comprobamos que el set se ha jugado (puede que el 3º set no se haya jugado)
                                auxTotalSets++
                            }
                        } else {
                            if (set.juegosJugador2 > set.juegosJugador1){
                                auxSetsGanados++
                                auxTotalSets++
                            }else if(set.juegosJugador2 < set.juegosJugador1){
                                auxTotalSets++
                            }
                        }
                    }
                }
                setsGanados = auxSetsGanados
                totalSets = auxTotalSets
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "Mis Estadísticas",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 20.dp),
            color = Color.Cyan
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ){
            // CircularProgressBar()
        }
    }

}