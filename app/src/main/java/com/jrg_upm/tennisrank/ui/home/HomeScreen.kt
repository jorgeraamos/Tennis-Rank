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
import com.jrg_upm.tennisrank.supabase.getUltimoPartidoConSets
import com.jrg_upm.tennisrank.supabase.updateResult
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
            partidoYSets = getUltimoPartidoConSets(jugadorActual.id)
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
                        { index, valor -> juegosJ2[index] = valor })
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
                                    if ((i == 2 && (setsJ1 == 2 || setsJ2 == 2) && juegosJ1[i] != 0 && juegosJ2[i] != 0)) {
                                        resultadoValido = false
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



// Funciones para la card en la que se mostrará el partido de cada usuario:
@Composable
fun ScoreboardCard(
    partido: Partido,
    juegosJ1: List<Int>, // Ahora recibe el estado editable de J1
    juegosJ2: List<Int>, // Ahora recibe el estado editable de J2
    participantes: List<Participante>,
    onJuegosJ1Changed: (Int, Int) -> Unit, // Callback para avisar del cambio
    onJuegosJ2Changed: (Int, Int) -> Unit
) {
    // Buscamos los nombres (Corregido idJugador para que coincida con tu modelo)
    val nombreJ1 = participantes.find { it.id == partido.idJugador1 }
        ?.jugador?.nombre ?: if (partido.idJugador1 == "SISTEMA_BYE") "DESCANSO" else "Cargando..."

    val nombreJ2 = participantes.find { it.id == partido.idJugador2 }
        ?.jugador?.nombre ?: if (partido.idJugador2 == "SISTEMA_BYE") "DESCANSO" else "Cargando..."

    // Calculamos cuántos sets ha ganado cada uno para la columna "SETS"
    val setsGanados = calcularSetsGanados(juegosJ1, juegosJ2)

    // Definimos como se verá la card que contiene el partido
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Cabecera del marcador
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                listOf("SETS", "1", "2", "3").forEach {
                    Text(
                        text = it,
                        modifier = Modifier.width(44.dp),
                        textAlign = TextAlign.Center,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Fila Jugador 1
            ScoreRow(
                nombre =  nombreJ1,
                setsGanados = setsGanados.first,
                juegos = juegosJ1,
                onJuegosChanged = onJuegosJ1Changed
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "VS",
                color = Color.Cyan, modifier = Modifier.align(Alignment.CenterHorizontally).offset(x = (-30).dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            // Fila Jugador 2
            ScoreRow(
                nombre =  nombreJ2,
                setsGanados = setsGanados.second,
                juegos = juegosJ2,
                onJuegosChanged = onJuegosJ2Changed
            )
        }
    }
}

@Composable
fun ScoreRow(
    nombre: String,
    setsGanados: Int,
    juegos: List<Int>,
    onJuegosChanged: (Int, Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = nombre,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        // Columna SETS (No editable, solo visualiza el total)
        ScoreCell(valor = setsGanados.toString(), editable = false, onValueChange = {})

        // Columnas 1, 2 y 3 (Editables)
        repeat(3) { index ->
            ScoreCell(
                valor = if(juegos[index] == 0 && index >= 0) "" else juegos[index].toString(),
                editable = true,
                onValueChange = { nuevoString ->
                    val num = nuevoString.toIntOrNull() ?: 0
                    onJuegosChanged(index, num)
                }
            )
        }
    }
}

@Composable
fun ScoreCell(
    valor: String,
    editable: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(35.dp)
            .background(color = if (editable) Color(0xFF263238) else Color.DarkGray)
            .border(1.dp, Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        if (editable) {
            BasicTextField(
                value = valor,
                onValueChange = { newValue ->
                    // Validamos que sea un número entre 0 y 7
                    if (newValue.isEmpty() || (newValue.toIntOrNull() != null && newValue.length <= 1)) {
                        onValueChange(newValue)
                    }
                },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            // Si no es editable (columna SETS), mostramos un simple Text
            Text(
                text = valor,
                color = Color.Cyan, // Color diferente para destacar los sets ganados
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
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



