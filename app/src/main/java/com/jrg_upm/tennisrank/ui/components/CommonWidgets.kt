package com.jrg_upm.tennisrank.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

