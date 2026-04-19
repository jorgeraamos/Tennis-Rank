package com.jrg_upm.tennisrank.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.model.Participante
import com.jrg_upm.tennisrank.model.Partido
import com.jrg_upm.tennisrank.model.Set
import com.jrg_upm.tennisrank.supabase.getAllParticipantes
import com.jrg_upm.tennisrank.supabase.getAllPartidosConSets
import com.jrg_upm.tennisrank.supabase.updateResult
import com.jrg_upm.tennisrank.ui.components.ScoreboardCard
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(jugadorActual: Jugador?) {
    // Nombre del jugador, no debería ser null pero por si acaso ponemos cargando en caso de que devuelva null:
    val nombreJugador = jugadorActual?.nombre ?: "Cargando..."
    // Variable estado para la lista de jugadores del ranking
    var listaRanking by remember { mutableStateOf<List<Participante>>(emptyList()) }

    var partidoYSets by remember { mutableStateOf<Pair<Partido, List<Set>>?>(null) }


    // Estados para los juegos (editables)
    val juegosJ1 = remember { mutableStateListOf(0, 0, 0) }
    val juegosJ2 = remember { mutableStateListOf(0, 0, 0) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Necesario para lanzar el snackbar

    // Launched Effect indica que se ejecute solo cuando se redibuje la pantalla, para no tener que
    // llamar infinitas veces al getCurrentPlayer y no colapsar la base de datos a llamadas
    LaunchedEffect(jugadorActual) {
        // Cargamos todos los jugadores para el ranking
        if (jugadorActual != null) {
            listaRanking = getAllParticipantes(jugadorActual.id)
            val partidos = getAllPartidosConSets(jugadorActual.id, estado = "Pendiente de jugar")
            val primerPartido = partidos.firstOrNull()
            partidoYSets = primerPartido
        }
        partidoYSets?.second?.let { listaDeSets ->
            val setsOrdenados = listaDeSets.sortedBy { it.numeroSet }
            setsOrdenados.forEachIndexed { index, set ->
                if (index < 3) { // Evitamos salirnos del array si hay más de 3
                    juegosJ1[index] = set.juegosJugador1
                    juegosJ2[index] = set.juegosJugador2
                }
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        // Utilizamos Lazy Column para que la pantalla sea scroleable
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
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
                    color = Color.Cyan
                )
            }

            item {
                // Ponemos un espacio
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                // Card que contendrá el próximo partido de cada jugador
                partidoYSets?.let { (partido, sets) ->
                    // Pasamos los datos reales a la Card
                    ScoreboardCard(
                        partido,
                        juegosJ1,
                        juegosJ2,
                        listaRanking,
                        { index, valor -> juegosJ1[index] = valor },
                        { index, valor -> juegosJ2[index] = valor },
                        editable = true
                    )
                } ?: // Si no hay partido se mostrará un texto al usuario
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
                            text = "No tienes partidos programados para esta jornada",
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                if(partidoYSets != null) {
                    // Ponemos el botón dentro de un Row para modificar su tamaño y posición
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End  // Lo empujamos a la derecha
                    ) {
                        Button(
                            onClick = {
                                var resultadoValido = true
                                var setsJ1 = 0
                                var setsJ2 = 0
                                for (i in 0 until 3) {
                                    // Caso en el que se haya ganado 2 sets a 0
                                    if (i == 2 && (setsJ1 == 2 || setsJ2 == 2)) {
                                        // Si en este caso el tercer set no está a 0 será un error.
                                        if(juegosJ1[i] != 0 && juegosJ2[i] != 0){
                                            resultadoValido = false
                                        }
                                        break
                                    } else if (
                                        (juegosJ1[i] == 6 && juegosJ2[i] in 0..4) ||
                                        (juegosJ1[i] == 7 && juegosJ2[i] in 5..6)
                                    ) {
                                        setsJ1++
                                    } else if (
                                        (juegosJ2[i] == 6 && juegosJ1[i] in 0..4) ||
                                        (juegosJ2[i] == 7 && juegosJ1[i] in 5..6)
                                    ) {
                                        setsJ2++
                                    } else {
                                        resultadoValido = false
                                        break
                                    }
                                }
                                if (resultadoValido) {
                                    scope.launch {
                                        val exito = updateResult(partidoYSets!!.first.id, juegosJ1, juegosJ2)
                                        if(exito){
                                            snackbarHostState.showSnackbar(
                                                message = "Resultado actualizado correctamente",
                                                withDismissAction = true
                                            )
                                        }
                                        else{
                                            snackbarHostState.showSnackbar(
                                                message = "Error: No se ha podido actualizar el resultado",
                                                withDismissAction = true
                                            )
                                        }
                                    }

                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Error: El resultado introducido no es válido",
                                            withDismissAction = true
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50), // Un verde estándar (Material Green 500)
                                contentColor = Color.White          // Color del texto
                            )
                        ) {
                            Text("Guardar Resultado")
                        }
                    }
                }

            }

            item {
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

            itemsIndexed(listaRanking) { index, jugador ->  // función lambda
                val posicion = index + 1
                val esUsuarioActual = jugador.id == jugadorActual?.id
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
                        text = "${posicion}. ${jugador.jugador.nombre}",
                        fontWeight = if (esUsuarioActual) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (esUsuarioActual) Color.Magenta else Color.DarkGray
                    )
                    Text(
                        text = "${jugador.puntos} pts",
                        fontWeight = if (esUsuarioActual) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (esUsuarioActual) Color.Magenta.copy(alpha = 0.5f) else Color.DarkGray.copy(
                            alpha = 0.5f
                        )
                    )
                }
                // Solo ponemos el divisor si NO es el usuario actual (para que el resaltado se vea limpio)
                if (!esUsuarioActual) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color.Gray.copy(alpha = 0.5f)
                    )  // define la opacidad del color
                }
            }
        }
    }
}




fun calcularSetsGanados(juegosJ1: List<Int>, juegosJ2: List<Int>): Pair<Int, Int> {
    var setsJ1 = 0
    var setsJ2 = 0
    for (i in 0 until 3) {
        if(
            (juegosJ1[i] == 6 && juegosJ1[i] > juegosJ2[i] && juegosJ2[i] >= 0) ||
            (juegosJ1[i] == 7 && (juegosJ2[i] == 6 || juegosJ2[i] == 5))){
            setsJ1 ++
        }else if(
            (juegosJ2[i] == 6 && juegosJ2[i] > juegosJ1[i] && juegosJ1[i] >= 0) ||
            (juegosJ2[i] == 7 && (juegosJ1[i] == 6 || juegosJ1[i] == 5))){
            setsJ2++
        }
    }
    return Pair<Int, Int>(setsJ1, setsJ2)
}



