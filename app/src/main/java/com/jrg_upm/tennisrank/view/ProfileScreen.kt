package com.jrg_upm.tennisrank.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jrg_upm.tennisrank.model.Jugador
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jrg_upm.tennisrank.model.updateAvatarUrl
import com.jrg_upm.tennisrank.model.updatePlayerData
import com.jrg_upm.tennisrank.model.uploadProfileImage
import kotlinx.coroutines.launch
// Imports para calcular la edad
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)  // Necesario ya que hay componentes experimentales en esta función: rememberModalBottomSheetState() y ModalBottomSheet
@Composable
fun ProfileScreen(jugadorActual: Jugador?) {  // Recibimos el usuario que está ejecutando la app
    // Variables para controlar si se ve o no la pestaña para editar el perfil
    val sheetState = rememberModalBottomSheetState()
    var mostrarSheet by remember { mutableStateOf(false) }


    // Variables para que el usuario pueda editar dichos campos:
    var nombreEdit by remember(jugadorActual) { mutableStateOf(jugadorActual?.nombre ?: "") }

    var nacionalidadEdit by remember(jugadorActual) {
        mutableStateOf(
            jugadorActual?.nacionalidad ?: ""
        )
    }
    var fechaNacimientoEdit by remember(jugadorActual) {
        mutableStateOf(jugadorActual?.fechaNacimiento ?: "")
    }

    var manoDominanteEdit by remember(jugadorActual) {
        mutableStateOf(
            jugadorActual?.manoDominante ?: ""
        )
    }

    var estiloJuegoEdit by remember(jugadorActual) {
        mutableStateOf(
            jugadorActual?.estiloJuego ?: ""
        )
    }
    var mejorGolpeEdit by remember(jugadorActual) {
        mutableStateOf(
            jugadorActual?.mejorGolpe ?: ""
        )
    }

    // Variable de estado para actualizar la imagen del jugador
    // Si el jugador ya tiene imagen la cogemos, en caso contrario será null
    var imagenTemporalUrl by remember(jugadorActual?.avatarUrl) {
        mutableStateOf(jugadorActual?.avatarUrl)
    }

    // Variables para poder subir la foto de perfil de cada jugador:

    // Permiso del "contexto" que necesita la app para poder leer archivos del sistema:
    val context = LocalContext.current

    // Usamos scope ya que la subida de la imagen a Supabase se hace de manera suspend
    val coroutineScope = rememberCoroutineScope()

    // Configuración del launcher(Objeto que abre la galería para que el usuario escoja la imagen que desee):
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()  // Para indicar que se quiere que el usuario elija una imagen
    ) { uri: Uri? ->  // Uri es la dirección de la imagen
        uri?.let {
            // Convertimos la URI en un ByteArray para subirlo
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes =
                inputStream?.readBytes()  // Leemos toda la información binaria de la foto y la convertimos en un ByteArray
            if (bytes != null && jugadorActual != null) {
                coroutineScope.launch {
                    // Subimos a Supabase Storage
                    val url = uploadProfileImage(jugadorActual.id, bytes)
                    // Actualizamos nuestra variable con la nueva url:
                    // Le añadimos la fecha ya que si tiene el mismo nombre que la anterior la descartaría.
                    imagenTemporalUrl = "$url?t=${System.currentTimeMillis()}"
                    // Actualizamos el campo avatar_url de la tabla jugadores en Supabase
                    updateAvatarUrl(
                        jugadorActual.id,
                        url
                    )  // se ejecuta en 2º plano, por eso lo ponemos después
                }
            }
        }
    }

    // Componentes de la ProfileScreen:
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Llamamos a la función donde se definen las componentes que muestran la foto de perfil y el nombre del jugador
        ProfileHeader(nombre = jugadorActual?.nombre, avatarUrl = imagenTemporalUrl, onImageClick = {launcher.launch("image/*")})

        // Ponemos un espacio entre medias
        Spacer(modifier = Modifier.height(24.dp))

        // Mostramos los datos del juagodr en una Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileItem("Nacionalidad", jugadorActual?.nacionalidad ?: "No definida")
                ProfileItem("Edad", calcularEdad(jugadorActual?.fechaNacimiento) ?: "--")
                ProfileItem("Mano Dominante", jugadorActual?.manoDominante ?: "No definida")
                ProfileItem("Estilo", jugadorActual?.estiloJuego ?: "No definido")
                ProfileItem("Golpe Maestro", jugadorActual?.mejorGolpe ?: "No definido")
                ProfileItem("Superficie Favorita", jugadorActual?.superficieFavorita ?: "No definida")
            }
        }

        // Añadimos un espacio para poner los botones
        Spacer(modifier = Modifier.height(24.dp))

        // Botón para que el usuario abra la pestaña de edición de perfil
        Button(onClick = { mostrarSheet = true }) {
            Text("Editar Perfil")
        }

        // PESTAÑA QUE SE ABRE AL PULSAR EL BOTÓN DE EDITAR PERFIL PARA PODER ELEGIR Y EDITAR LOS CAMPOS QUE EL USUARIO DESEE
        if (mostrarSheet) {
            ModalBottomSheet(
                onDismissRequest = { mostrarSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF1C1C1C)
            ) {
                // Contenido de la pestaña de edición del perfil
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Editar Mi Perfil",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Variable nombre
                    OutlinedTextField(
                        value = nombreEdit,
                        onValueChange = { nombreEdit = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Variable nacionalidad
                    OutlinedTextField(
                        value = nacionalidadEdit,
                        onValueChange = { nacionalidadEdit = it },
                        label = { Text("Nacionalidad") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DatePickerField(
                        fechaSeleccionada = fechaNacimientoEdit,
                        onFechaCambiada = {fechaNacimientoEdit = it} )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Editar mano dominante con menu depegable:
                    SelectorOpciones(
                        label = "Mano Dominante",
                        seleccionado = manoDominanteEdit,
                        opciones = listOf("Derecha", "Izquierda"),
                        onOptionSelected = { manoDominanteEdit = it } // Actualizamos la variable
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Editar estilo de juego con menu depegable:
                    SelectorOpciones(
                        label = "Estilo de Juego",
                        seleccionado = estiloJuegoEdit,
                        opciones = listOf(
                            "Agresivo",
                            "Defensivo",
                            "Saque y Volea",
                            "Contragolpeador"
                        ),
                        onOptionSelected = { estiloJuegoEdit = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Editar mejor golpe con menu depegable:
                    SelectorOpciones(
                        label = "Golpe Maestro",
                        seleccionado = mejorGolpeEdit,
                        opciones = listOf("Drive", "Revés", "Saque", "Volea", "Dejada"),
                        onOptionSelected = { mejorGolpeEdit = it }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // Añadimos espacio entre los botones
                    ) {
                        // Botón de Volver
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                // Cerramos la pestaña de edición
                                coroutineScope.launch {
                                    sheetState.hide()
                                }.invokeOnCompletion {
                                    // Para comunicar al compose de que no debe mostrar el Sheet
                                    if (!sheetState.isVisible) mostrarSheet = false
                                }
                            }
                        ) {
                            Text("Volver")
                        }

                        // Boton para guardar los datos en Supabase
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                coroutineScope.launch {
                                    // Funcion para llamar a Supabase para guardar
                                    // Ponemos las !! porque sabemos que el usuario tiene id
                                    updatePlayerData(
                                        idUsuario = jugadorActual!!.id,
                                        nombre = nombreEdit,
                                        nacionalidad = nacionalidadEdit,
                                        fechaNacimiento = fechaNacimientoEdit,
                                        manoDominante = manoDominanteEdit,
                                        estilo = estiloJuegoEdit,
                                        mejorGolpe = mejorGolpeEdit
                                    )
                                    // 2. Cerrar la pestaña
                                    sheetState.hide()
                                }.invokeOnCompletion {
                                    if (!sheetState.isVisible) mostrarSheet = false
                                }
                            }
                        ) {
                            Text("Guardar Cambios")
                        }
                    }
                }
            }
        }
    }
}


// Función para definir la foto de perfil del jugador
@Composable
fun ProfileHeader(nombre: String?, avatarUrl: String?, onImageClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Yellow, shape = CircleShape)
                .clip(CircleShape)
                .clickable { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            // Para la foto de perfil ponemos o bien la foto si la ha puesto el usuario o su inicial en caso contrario
            if (avatarUrl != null) {
                AsyncImage(  // Usamos AsyncImage de Coil para cargar la foto de Supabase
                    model = avatarUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = nombre?.firstOrNull()?.toString() ?: "?",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Mostramos el nombre del jugador debajo de su imagen
        Text(
            text = nombre ?: "Nombre",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}





// Función que define los items para mostrar los datos de cada campo
@Composable
fun ProfileItem(label: String, value: String) {
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



// Función para mostrar el calendario y que el usuario pueda editar su fecha de nacimiento
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(fechaSeleccionada: String, onFechaCambiada: (String) -> Unit) {
    var mostrarCalendario by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Campo de texto que al pulsar abre el calendario
    Box(modifier = Modifier.fillMaxWidth().clickable { mostrarCalendario = true }) {
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



// FUNCION PARA CALCULAR LA EDAD DE CADA JUGADOR SEGÚN SU FECHA DE NACIMIENTO
fun calcularEdad(fechaNacimiento: String?): String? {
    if (fechaNacimiento == null) return null
    return try {
        // Suponiendo que la fecha en Supabase es "yyyy-MM-dd"
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaNac = sdf.parse(fechaNacimiento) ?: return null

        val hoy = Calendar.getInstance()
        val nacimiento = Calendar.getInstance()
        nacimiento.time = fechaNac

        var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)

        // Ajuste por si aún no ha cumplido años este año
        if (hoy.get(Calendar.MONTH) < nacimiento.get(Calendar.MONTH)) {
            edad--
        } else if (hoy.get(Calendar.MONTH) == nacimiento.get(Calendar.MONTH)) {
            if (hoy.get(Calendar.DAY_OF_MONTH) < nacimiento.get(Calendar.DAY_OF_MONTH)) {
                edad--
            }
        }
        "$edad años"
    } catch (e: Exception) {
        null
    }
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

