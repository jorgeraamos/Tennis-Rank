package com.jrg_upm.tennisrank.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jrg_upm.tennisrank.model.Jornada
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.model.Participante
import com.jrg_upm.tennisrank.model.Partido
import com.jrg_upm.tennisrank.model.Set
import com.jrg_upm.tennisrank.ui.components.ScoreboardCard
import com.jrg_upm.tennisrank.viewModel.home.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    jugadorActual: Jugador?,
    listaRanking: List<Participante>,
    listaJornadas: List<Pair<Jornada, Pair<Partido, List<Set>>>>,
    homeViewModel: HomeViewModel = viewModel()
) {
    // Nombre del jugador, no debería ser null pero por si acaso ponemos cargando en caso de que devuelva null:
    val nombreJugador = jugadorActual?.nombre ?: "Cargando..."

    val jornadaYPartido = remember(listaJornadas) {
        listaJornadas.firstOrNull { it.first.estado == "Abierta" }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Necesario para lanzar el snackbar

    // Cuando cambia el partido, cargamos los puntos en el ViewModel
    LaunchedEffect(jornadaYPartido) {
        jornadaYPartido?.second?.second?.let { sets ->
            homeViewModel.inicializarMarcador(sets)
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
                    modifier = Modifier.padding(bottom= 20.dp),  // especificamos la altura
                    color = Color(0xFF1976D2)
                )
            }

            item {
                // Ponemos un espacio
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                // Card que contendrá el próximo partido de cada jugador
                jornadaYPartido?.let { (jornada, partidoConSets) ->
                    val partido = partidoConSets.first
                    // Pasamos los datos reales a la Card
                    Text(
                        text = "Jornada ${jornada?.numero}: ${jornada?.fechaInicio} - ${jornada?.fechaFin} ",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        textAlign = TextAlign.Start
                    )
                    ScoreboardCard(
                        partido,
                        juegosJ1 = homeViewModel.juegosJ1,
                        juegosJ2 = homeViewModel.juegosJ2,
                        listaRanking,
                        { index, valor -> homeViewModel.juegosJ1[index] = valor },
                        { index, valor -> homeViewModel.juegosJ2[index] = valor },
                        editable = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ){
                        Button(
                            onClick = {
                                homeViewModel.guardarResultado(
                                    partidoId = partido.id,
                                    onSuccess = {
                                        scope.launch { snackbarHostState.showSnackbar("Resultado actualizado correctamente") }
                                        // onResultadoActualizado() // Esto avisará al MenuViewModel para recargar todo
                                    },
                                    onError = { msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                )
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







