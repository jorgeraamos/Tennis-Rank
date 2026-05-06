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
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jrg_upm.tennisrank.model.Jornada
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.model.Partido
import com.jrg_upm.tennisrank.model.Set
import com.jrg_upm.tennisrank.ui.components.StatBox
import com.jrg_upm.tennisrank.viewModel.statistics.StatisticsViewModel

@Composable
fun StatisticsScreen(
    jugadorActual: Jugador?,
    listaJornadas: List<Pair<Jornada, Pair<Partido, List<Set>>>>,
    viewModel: StatisticsViewModel = viewModel()
) {
    // Cada vez que la lista de jornadas o el jugador cambien, recalculamos
    LaunchedEffect(jugadorActual, listaJornadas) {
        jugadorActual?.let {
            viewModel.calcularEstadisticas(it.id, listaJornadas)
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 20.dp),
                color = Color(0xFF1976D2)
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
                    StatBox("Partidos", viewModel.partidosGanados, viewModel.totalPartidos, color = Color(0xFF27C2F5))
                    // Stats sets ganados
                    StatBox("Sets", viewModel.setsGanados, viewModel.totalSets, color = Color(0xFF27F55B))
                    // Stats juegos ganados
                    StatBox("Juegos", viewModel.juegosGanados, viewModel.totalJuegos, Color(0xFFF5DA27))
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
                    if (viewModel.racha.isEmpty()) {
                        Text(
                            "No hay partidos registrados",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        repeat(5){index ->
                            // Para los partidos que se han jugado pintamos V o D, pero los partidos que no
                            // se hayan jugado hasta llegar a 5 se pintan en gris
                            val resultado = viewModel.racha.getOrNull(index)
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
                        value = "${viewModel.remontadas}",
                        sublabel = "Partidos ganados tras perder el 1º set",
                        icon = Icons.Default.Whatshot,  // Fuego de remontada
                        iconColor = Color(0xFFFF5722)  // Color naranja
                    )

                    // Fila de Tie-breaks
                    val ratioTieBreak = if(viewModel.tieBreaksTotales > 0)
                        (viewModel.tieBreaksGanados.toFloat() / viewModel.tieBreaksTotales * 100).toInt() else 0

                    HitoRow(
                        label = "Tie-breaks Ganados",
                        value = "$ratioTieBreak%",
                        sublabel = "Has ganado ${viewModel.tieBreaksGanados} de ${viewModel.tieBreaksTotales} disputados",
                        icon = Icons.Default.Psychology,  // Icono de mentalidad
                        iconColor = Color(0xFF27BEF5)  // Color azul
                    )

                    // Fila victorias como visitante
                    val ratioVisitante = if(viewModel.totalPartidosComoVisitante > 0)
                        (viewModel.victoriasComoVisitante.toFloat() / viewModel.totalPartidosComoVisitante * 100).toInt() else 0

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
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
    }
}