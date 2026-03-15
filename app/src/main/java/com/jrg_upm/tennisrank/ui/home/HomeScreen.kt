package com.jrg_upm.tennisrank.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.model.getAllPlayers

@Composable
fun HomeScreen(jugadorActual: Jugador?) {
    // Nombre del jugador, no debería ser null pero por si acaso ponemos cargando en caso de que devuelva null:
    val nombreJugador = jugadorActual?.nombre ?: "Cargando..."
    // Variable estado para la lista de jugadores del ranking
    var listaRanking by remember {mutableStateOf<List<Jugador>>( emptyList() )}

    // Launched Effect indica que se ejecute solo cuando se redibuje la pantalla, para no tener que
    // llamar infinitas veces al getCurrentPlayer y no colapsar la base de datos a llamadas
    LaunchedEffect(Unit) {
        // Cargamos todos los jugadores para el ranking
        listaRanking = getAllPlayers()
    }

    // Utilizamos Lazy Column para que la pantalla sea scroleable
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // En las Lazy Column el contenido estático se debe de poner dentro de item
        item {
            Text(
                text = "Bienvenido ${nombreJugador}!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp),  // especificamos la altura
                color = Color.Yellow
            )
        }

        item {
            // Ponemos un espacio
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            // Card que contendrá el próximo partido de cada rival
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Próximo partido", fontWeight = FontWeight.Bold)
                    Text(text = "Aún no tienes partidos programados")
                }
            }
        }

        item{
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text(
                text = "Ranking Actual",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        items(listaRanking) { jugador ->  // función lambda
            var esUsuarioActual = jugador.id == jugadorActual?.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Si no se tiene el campo "posicion_ranking" en la DB,
                // puedes usar el índice de la lista + 1
                // Además, si destacamos la fila del usuario que está usando la app
                Text(
                    text = "${jugador.posicionRanking}. ${jugador.nombre}",
                    fontWeight = if(esUsuarioActual) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (esUsuarioActual) Color.Yellow else Color.White
                )
                Text(
                    text = "${jugador.puntos} pts",
                    fontWeight = if (esUsuarioActual) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (esUsuarioActual) Color.Yellow else Color.LightGray
                )
            }
            // Solo ponemos el divisor si NO es el usuario actual (para que el resaltado se vea limpio)
            if (!esUsuarioActual) {
                HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.5f))  // define la opacidad del color
            }
        }
    }
}

