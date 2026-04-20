package com.jrg_upm.tennisrank.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrg_upm.tennisrank.model.Participante
import com.jrg_upm.tennisrank.model.Partido
import com.jrg_upm.tennisrank.ui.home.calcularSetsGanados
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone





// Función que define los items para mostrar los datos de cada campo
@Composable
fun InforRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
}

// Función general para poder desplegar las opciones a elegir de un determinado campo
@OptIn(ExperimentalMaterial3Api::class)  // Indica que es un compenente nuevo en la librería y pueden haber cambios
@Composable
fun SelectorOpciones(
    label: String,  // Campo que queremos editar
    seleccionado: String,
    opciones: List<String>,  // Lista de opciones
    onOptionSelected: (String) -> Unit  // Callback para devolver la opción elegida
) {
    // variable de estado para indicar si hay que expandir las opciones o no
    var expandido by remember { mutableStateOf(false) }

    // Caja donde estará el menú de selección
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = seleccionado,
            onValueChange = {},
            readOnly = true,  // Solo queremos que el usuario vea las opciones y que las seleccione, no que pueda escribir
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        // Menú de selección
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onOptionSelected(opcion) // Avisamos del cambio
                        expandido = false
                    }
                )
            }
        }
    }
}


// Función para mostrar el calendario. Utilizada en la view del perfil para que el usuario pueda editar su fecha de nacimiento
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(fechaSeleccionada: String, onFechaCambiada: (String) -> Unit) {
    var mostrarCalendario by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Campo de texto que al pulsar abre el calendario
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { mostrarCalendario = true }) {
        OutlinedTextField(
            value = fechaSeleccionada,
            onValueChange = { },
            readOnly = true,  // Queremos que solo elija una fecha del calendario, no que escriba
            label = { Text("Fecha de Nacimiento") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        // CALENDARIO PARA ELEGIR LA FECHA
        if (mostrarCalendario) {
            DatePickerDialog(  // Calendario a mostrar
                onDismissRequest = { mostrarCalendario = false }, // Si el usuario toca fuera de la ventana se cierra
                confirmButton = {  // Botón para confirmar la fecha seleccionada
                    TextButton(onClick = {  // Text Botton para que el botón sea plano
                        datePickerState.selectedDateMillis?.let {  // Cogemos la fecha seleccionada
                            // Formateamos para Supabase (yyyy-MM-dd)
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }  // Actualizamos nuestra variable de estado
                            onFechaCambiada(formatter.format(Date(it)))
                        }  // Cerramos el botón
                        mostrarCalendario = false
                    }) { Text("Aceptar") }
                },
                dismissButton = {  // Botón para cancelar
                    TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
                }
            ) {
                DatePicker(state = datePickerState)
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
    onJuegosJ2Changed: (Int, Int) -> Unit,
    editable: Boolean
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
                onJuegosChanged = onJuegosJ1Changed,
                editable = editable
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
                onJuegosChanged = onJuegosJ2Changed,
                editable = editable
            )
        }
    }
}

@Composable
fun ScoreRow(
    nombre: String,
    setsGanados: Int,
    juegos: List<Int>,
    onJuegosChanged: (Int, Int) -> Unit,
    editable: Boolean
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
                editable = editable,
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

// Círculo para enseñar las estadísticas de cada jugador:
@Composable
fun CircularProgressBar(
    percentage: Float,
    number: Int,
    fontSize: TextUnit = 28.sp,
    radius: Dp = 50.dp,
    color: Color = Color.Green,
    strokeWidth: Dp = 8.dp,
    animationDuration: Int = 1000,
    animDelay: Int = 0
) {
    // Estado que describe si está en ejecución o no
    var animationPlayed by remember{
        mutableStateOf(false)
    }
    // Con animateFloatAsState conseguimos que de forma animada vaya desde el 0 hasta el valor correspondiente.
    val curPercentage = animateFloatAsState(
        targetValue = if(animationPlayed) percentage else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animDelay
        )
    )
    LaunchedEffect(key1 = true){
        animationPlayed = true
    }
    // Parte visual:
    Box(
        contentAlignment = Alignment.Center
    ){
        // Canvas sirve para dibujar tus propias figuras
        Canvas(modifier = Modifier.size(radius * 2f) ){
            drawArc(
                color = color,
                -90f,  // angulo donde el cículo empieza
                360 * curPercentage.value, // angulo hasta donde acaba
                useCenter = false, // para no conectar las líneas con el centro
                style = Stroke(
                    strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )  // para ver el grosor del stroke
            )
        }
        Text (
            text = (curPercentage.value * number).toInt().toString(),
            color = Color.Black,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}
