package com.jrg_upm.tennisrank.ui.home

import android.R.attr.background
import android.R.id.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
                color = Color.Cyan
            )
        }

        item {
            // Ponemos un espacio
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            // Card que contendrá el próximo partido de cada rival
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//            ) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text(text = "Próximo partido", fontWeight = FontWeight.Bold)
//                    Text(text = "Aún no tienes partidos programados")
//                }
//            }

            ScoreboardCard("Rafael Nadal", "Novak Djokovic")
        }

        item{
            Spacer(modifier = Modifier.height(4.dp))
        }
        item{
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Añadimos espacio entre los botones
            ){
                // Botón para marcar el partido como no disputado
                Button(
                    onClick = {
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray, // Color de fondo gris
                        contentColor = Color.White   // Color del texto
                    )
                ){
                    Text("No se ha podido jugar")
                }
                Button(
                    onClick = {
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50), // Un verde estándar (Material Green 500)
                        contentColor = Color.White          // Color del texto
                    )
                ){
                    Text("Guardar Resultado")
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
                    color = if (esUsuarioActual) Color.Magenta else Color.DarkGray
                )
                Text(
                    text = "${jugador.puntos} pts",
                    fontWeight = if (esUsuarioActual) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (esUsuarioActual) Color.Magenta.copy(alpha = 0.5f) else Color.DarkGray.copy(alpha = 0.5f)
                )
            }
            // Solo ponemos el divisor si NO es el usuario actual (para que el resaltado se vea limpio)
            if (!esUsuarioActual) {
                HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.5f))  // define la opacidad del color
            }
        }
    }
}



// Funciones para la card en la que se mostrará el partido de cada usuario:
@Composable
fun ScoreboardCard(jugador1: String, jugador2: String) {
    Card(  // Definimos la card donde se verá el partido
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray), // Color gris oscuro tipo marcador
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Definimos la cabecera de la tabla
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                val headers = listOf("SETS", "1", "2", "3")
                headers.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier.width(44.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Fila Jugador 1
            ScoreRow(nombre = "(1) $jugador1")

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "VS.", color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))

            // Fila Jugador 2
            ScoreRow(nombre = "(4) $jugador2")
        }
    }
}

@Composable
fun ScoreRow(nombre: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nombre del jugador
        Text(
            text = nombre,
            modifier = Modifier.weight(1f).padding(8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            softWrap = true, // Permite que el texto salte de línea si es largo
            maxLines = 2
        )

        // Celdas de puntuación (puedes convertirlas en OutlinedTextField para que sean editables)
        repeat(4) {
            ScoreCell()
        }
    }
}

@Composable
fun ScoreCell() {
    var juego by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(35.dp)
            .background(color = Color(0xFF263238))
            .border(1.dp, Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = juego,
            onValueChange = {
                // Limitamos a 1 o 2 caracteres para que no rompa el diseño
                if (it.toInt() in 0..7) juego = it
            },
            // Estilo del texto dentro del input
            textStyle = TextStyle(
                color = Color.White,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            // Forzamos a que solo se abra el teclado numérico
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}



