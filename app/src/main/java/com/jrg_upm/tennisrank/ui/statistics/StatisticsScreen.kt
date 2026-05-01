package com.jrg_upm.tennisrank.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplaneTicket
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.model.Partido
import com.jrg_upm.tennisrank.model.Set
import com.jrg_upm.tennisrank.supabase.getPartidosPorJornada
import com.jrg_upm.tennisrank.ui.components.StatBox

@Composable
fun StatisticsScreen(jugadorActual: Jugador?) {
    // variable de estado en la que se guardan todos los partidos que ha jugado un jugador
    var listaPartidos by remember { mutableStateOf(emptyList<Pair<Partido, List<Set>>>()) }
    var totalPartidos by remember { mutableStateOf(0) }
    var partidosGanados by remember { mutableStateOf(0) }
    var totalSets by remember { mutableStateOf(0) }
    var setsGanados by remember { mutableStateOf(0) }
    var totalJuegos by remember { mutableStateOf(0) }
    var juegosGanados by remember { mutableStateOf(0) }
    var racha by remember { mutableStateOf(emptyList<Boolean>()) }
    var tieBreaksGanados by remember { mutableStateOf(0) }
    var tieBreaksTotales by remember { mutableStateOf(0) }
    var remontadas by remember { mutableStateOf(0) }
    var totalPartidosComoVisitante by remember { mutableStateOf(0) }
    var victoriasComoVisitante by remember { mutableStateOf(0) }


    LaunchedEffect(jugadorActual) {
        // Cargamos todos los jugadores para el ranking
        if (jugadorActual != null) {
            val listaJornadas = getPartidosPorJornada(jugadorActual.id, estado = "Finalizada")
            listaPartidos = listaJornadas.map{it.second}
            if(listaPartidos.isNotEmpty()){
                // Obtenemos el número total de partidos que ha disputado el usuario
                totalPartidos = listaPartidos.size
                // Obtenemos el número total de partidos que ha ganado
                partidosGanados = listaPartidos.count { (partido, _ ) ->
                    partido.idGanador == jugadorActual.id }

                racha = listaPartidos.take(5)  // Cogemos los 5 últimos partidos
                    .map { (partido, _) -> partido.idGanador == jugadorActual.id }
                    .reversed()  // Invertimos para que el más antiguo salga a la izquierda

                // Ahora recogemos el total de sets y los sets ganados por el jugador:
                // Utilizamos variables auxiliares en caso de que pudiera cambiar el jugador para no duplicar los valores
                var auxSetsGanados = 0
                var auxTotalSets = 0
                var auxJuegosGanados = 0
                var auxTotalJuegos = 0
                var auxRemontadas = 0
                var auxTieBreaksGanados = 0
                var auxTieBreaksTotales = 0
                var auxPartidosComoVisitante = 0
                var auxVictoriasComoVisitante = 0

                listaPartidos.forEach { (partido, sets) ->
                    if(partido.idJugador2 == jugadorActual.id){
                        auxPartidosComoVisitante++
                        if(partido.idGanador == jugadorActual.id){
                            auxVictoriasComoVisitante++
                        }
                    }
                    // Miramos cada set
                    sets.forEach { set ->
                        val juegosJugador1 = set.juegosJugador1
                        val juegosJugador2 = set.juegosJugador2
                        // Comprobamos si el jugadorActual es el Jugador 1 o el 2 en este partido
                        if (partido.idJugador1 == jugadorActual.id) {
                            if (juegosJugador1> juegosJugador2) {
                                auxSetsGanados++
                                auxTotalSets++
                            } else if (juegosJugador1 < juegosJugador2) {
                                // Comprobamos que el set se ha jugado (puede que el 3º set no se haya jugado)
                                auxTotalSets++

                                // Comprobamos además con el primer set si el partido se ha remontado:
                                if( set.numeroSet == 1 && partido.idGanador == jugadorActual.id )
                                    auxRemontadas++
                            }
                            auxJuegosGanados += juegosJugador1

                            // Miramos los tie breaks:
                            if ((juegosJugador1 == 7 && juegosJugador2 == 6) || (juegosJugador1 == 6 && juegosJugador2 == 7)) {
                                auxTieBreaksTotales++ // Siempre sumamos al total si hubo tie-break
                                if (juegosJugador1== 7) auxTieBreaksGanados++ // Sumamos a ganados solo si lo ganó J1
                            }

                        } else {
                            if (juegosJugador2 > juegosJugador1){
                                auxSetsGanados++
                                auxTotalSets++
                            }else if(juegosJugador2 < juegosJugador1){
                                auxTotalSets++

                                // Comprobamos además con el primer set si el partido se ha remontado:
                                if( set.numeroSet == 1 && partido.idGanador == jugadorActual.id )
                                    auxRemontadas++
                            }
                            auxJuegosGanados += juegosJugador2
                            // Miramos los tie breaks:
                            if ((juegosJugador2 == 7 && juegosJugador1 == 6) || (juegosJugador1 == 6 && juegosJugador2 == 7)) {
                                auxTieBreaksTotales++
                                if (juegosJugador2 == 7) auxTieBreaksGanados++
                            }
                        }
                        auxTotalJuegos += set.juegosJugador1 + set.juegosJugador2
                    }
                }
                setsGanados = auxSetsGanados
                totalSets = auxTotalSets
                juegosGanados = auxJuegosGanados
                totalJuegos = auxTotalJuegos
                remontadas = auxRemontadas
                tieBreaksGanados = auxTieBreaksGanados
                tieBreaksTotales = auxTieBreaksTotales
                totalPartidosComoVisitante = auxPartidosComoVisitante
                victoriasComoVisitante = auxVictoriasComoVisitante
            }
        }
    }


    // Componentes de la pantalla:
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        item {
            Text(
                text = "Mis Estadísticas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 20.dp),
                color = Color.Cyan
            )
        }
        item {
            StatisticsSection(title = "Rendimiento Global:") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp), // Un poco de margen en los bordes de la pantalla
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
                ){
                    // Stats partidos ganados
                    StatBox("Partidos", partidosGanados, totalPartidos, Color.Cyan)
                    // Stats sets ganados
                    StatBox("Sets", setsGanados, totalSets, Color.Green)
                    // Stats juegos ganados
                    StatBox("Juegos", juegosGanados, totalJuegos, Color.Yellow)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            StatisticsSection(title = "Racha Últimos 5 Partidos") {
                // Mostramos la racha de los últimos 5 partidos del usuario:
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (racha.isEmpty()) {
                        Text(
                            "No hay partidos registrados",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        repeat(5){index ->
                            // Para los partidos que se han jugado pintamos V o D, pero los partidos que no
                            // se hayan jugado hasta llegar a 5 se pintan en gris
                            val resultado = racha.getOrNull(index)
                            if(resultado != null){
                                RachaItem(victoria = resultado, activo = true)
                            }else{
                                RachaItem(victoria = false, activo = false)
                            }
                        }
                    }
                }
            }
        }

        item{
            StatisticsSection(title = "Análisis de Juego") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Fila de Remontadas
                    HitoRow(

                        label = "Remontadas",
                        value = "$remontadas",
                        sublabel = "Partidos ganados tras perder el 1º set",
                        icon = Icons.Default.Whatshot,  // Fuego de remontada
                        iconColor = Color(0xFFFF5722)  // Color naranja
                    )

                    // Fila de Tie-breaks
                    val ratioTieBreak = if(tieBreaksTotales > 0)
                        (tieBreaksGanados.toFloat() / tieBreaksTotales * 100).toInt() else 0

                    HitoRow(
                        label = "Tie-breaks Ganados",
                        value = "$ratioTieBreak%",
                        sublabel = "Has ganado $tieBreaksGanados de $tieBreaksTotales disputados",
                        icon = Icons.Default.Psychology,  // Icono de mentalidad
                        iconColor = Color(0xFF27BEF5)  // Color azul
                    )

                    // Fila victorias como visitante
                    val ratioVisitante = if(totalPartidosComoVisitante > 0)
                        (victoriasComoVisitante.toFloat() / totalPartidosComoVisitante * 100).toInt() else 0

                    HitoRow(
                        label = "Victorias como visitante",
                        value = "$ratioVisitante%",
                        sublabel = "Partidos ganados a rivales con más puntos",
                        icon = Icons.Default.AirplanemodeActive,  // Icono avión
                        iconColor = Color.White  // Color blanco como el avión
                    )
                }
            }
        }
    }
}

// Composable dónde se representará la racha de los últimos 5 partidos del jugador
@Composable
fun RachaItem(victoria: Boolean, activo: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                when {
                    !activo -> Color.LightGray.copy(alpha = 0.6f)  // Gris si no se ha jugado
                    victoria -> Color(0xFF4CAF50)  // Verde para victoria
                    else -> Color(0xFFF44336)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
            Text(
                text = if(!activo){
                    "-"
                }else {
                    if (victoria) "V" else "D"
                },
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
    }
}

@Composable
fun StatisticsSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

// Componente pequeño para las filas de hitos
@Composable
fun HitoRow(
    label: String,
    value: String,
    sublabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text(sublabel, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Cyan,
            fontWeight = FontWeight.ExtraBold
        )
    }
}