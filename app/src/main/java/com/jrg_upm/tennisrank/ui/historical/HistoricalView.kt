package com.jrg_upm.tennisrank.ui.historical

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.model.Partido
import com.jrg_upm.tennisrank.model.Set
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.jrg_upm.tennisrank.model.Participante
import com.jrg_upm.tennisrank.ui.components.ScoreboardCard
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import com.jrg_upm.tennisrank.model.Jornada
import com.jrg_upm.tennisrank.ui.components.infoJornada

@Composable
fun HistoricalScreen(jugadorActual: Jugador?, listaRanking: List<Participante>, listaJornadas: List<Pair<Jornada, Pair<Partido, List<Set>>>>) {

    val listaJornadasFinalizadas = remember(listaJornadas) {
        listaJornadas.filter { it.first.estado == "Finalizada" }
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 20.dp),
                color = Color(0xFF1976D2)
            )
        }

        if(listaJornadasFinalizadas.isNotEmpty() ){
            items(listaJornadasFinalizadas) { (jornada, partidoYSets) ->

                val juegosJ1 = mutableListOf(0, 0, 0)
                val juegosJ2 = mutableListOf(0, 0, 0)

                partidoYSets.second.forEachIndexed { index, set ->
                    if (index < 3) {
                        juegosJ1[index] = set.juegosJugador1
                        juegosJ2[index] = set.juegosJugador2
                    }
                }

                // Mostramos la información de la jornada:
                infoJornada(jornada, partidoYSets.first, jugadorActual!!.id)

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