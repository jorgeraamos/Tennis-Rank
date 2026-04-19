package com.jrg_upm.tennisrank.ui.historical

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.model.Partido
import com.jrg_upm.tennisrank.model.Set
import com.jrg_upm.tennisrank.supabase.getAllPartidosConSets
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.jrg_upm.tennisrank.model.Participante
import com.jrg_upm.tennisrank.supabase.getAllParticipantes
import com.jrg_upm.tennisrank.ui.components.ScoreboardCard
import androidx.compose.foundation.lazy.items

@Composable
fun HistoricalScreen(jugadorActual: Jugador?) {

    // variable de estado en la que se guardan todos los partidos que ha jugado un jugador
    var listaPartidos by remember { mutableStateOf(emptyList<Pair<Partido, List<Set>>>()) }

    var listaRanking by remember { mutableStateOf<List<Participante>>(emptyList()) }

    LaunchedEffect(jugadorActual) {
        // Cargamos todos los jugadores para el ranking
        if (jugadorActual != null) {
            listaRanking = getAllParticipantes(jugadorActual.id)
            listaPartidos = getAllPartidosConSets(jugadorActual.id, estado = "Finalizado")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = "Historial De Partidos",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 20.dp),
                color = Color.Cyan
            )
        }

        if(!listaPartidos.isEmpty() ){
            items(listaPartidos) { partidoYSets ->
                val juegosJ1 = mutableListOf(0, 0, 0)
                val juegosJ2 = mutableListOf(0, 0, 0)

                partidoYSets.second.forEachIndexed { index, set ->
                    if (index < 3) {
                        juegosJ1[index] = set.juegosJugador1
                        juegosJ2[index] = set.juegosJugador2
                    }
                }
                ScoreboardCard(
                    partido = partidoYSets.first,
                    juegosJ1 = juegosJ1,
                    juegosJ2 = juegosJ2,
                    participantes = listaRanking,
                    onJuegosJ1Changed = { _, _ -> }, // Vacío porque no es editable
                    onJuegosJ2Changed = { _, _ -> }, // Vacío porque no es editable
                    editable = false
                )
            }
        }
        else{
            item{
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)  // Le damos altura a la Card
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    // Usamos un Box para poder usar contentAlignment y centrar el texto
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no has disputado ningún partido",
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }




    }

}