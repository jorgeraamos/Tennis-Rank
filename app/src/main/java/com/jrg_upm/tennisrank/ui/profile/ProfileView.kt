package com.jrg_upm.tennisrank.ui.profile

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jrg_upm.tennisrank.ui.components.DatePickerField
import com.jrg_upm.tennisrank.ui.components.InforRow
import com.jrg_upm.tennisrank.ui.components.SelectorOpciones
import com.jrg_upm.tennisrank.viewModel.ProfileViewModel
import kotlinx.coroutines.launch
// Imports para calcular la edad

@OptIn(ExperimentalMaterial3Api::class)  // Necesario ya que hay componentes experimentales en esta función: rememberModalBottomSheetState() y ModalBottomSheet
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onLogout: () -> Unit) {  // Recibimos el usuario que está ejecutando la app
    // Variables para controlar si se ve o no la pestaña para editar el perfil
    val sheetState = rememberModalBottomSheetState()
    var mostrarSheet by remember { mutableStateOf(false) }

    // Variables para poder subir la foto de perfil de cada jugador:

    val coroutineScope = rememberCoroutineScope()
    // Permiso del "contexto" que necesita la app para poder leer archivos del sistema:
    val context = LocalContext.current

    // Configuración del launcher(Objeto que abre la galería para que el usuario escoja la imagen que desee):
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()  // Para indicar que se quiere que el usuario elija una imagen
    ) { uri: Uri? ->  // Uri es la dirección de la imagen
        uri?.let {

            // Convertimos la URI en un ByteArray para subirlo
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()  // Leemos toda la información binaria de la foto y la convertimos en un ByteArray
            viewModel.subirFoto(bytes)  // Llamamos a la función subirFoto del Profile View Model
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
        ProfileHeader(nombre = viewModel.jugadorActual?.nombre, avatarUrl = viewModel.imagenTemporalUrl, onImageClick = {launcher.launch("image/*")})

        // Ponemos un espacio entre medias
        Spacer(modifier = Modifier.height(24.dp))

        // Mostramos los datos del juagodr en una Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InforRow("Nacionalidad", viewModel.jugadorActual?.nacionalidad ?: "No definida")
                InforRow("Edad", viewModel.edadJugador)
                InforRow("Mano Dominante", viewModel.jugadorActual?.manoDominante ?: "No definida")
                InforRow("Estilo", viewModel.jugadorActual?.estiloJuego ?: "No definido")
                InforRow("Golpe Maestro", viewModel.jugadorActual?.mejorGolpe ?: "No definido")
                InforRow("Superficie Favorita", viewModel.jugadorActual?.superficieFavorita ?: "No definida")
            }
        }

        // Añadimos un espacio para poner los botones
        Spacer(modifier = Modifier.height(24.dp))

        // Botones al final de la view para poder editar el perfil o cerrar sesión
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Añadimos espacio entre los botones
        ){
            // Botón para que el usuario abra la pestaña de edición de perfil
            Button(modifier = Modifier.weight(1f) ,onClick = { mostrarSheet = true }) {
                Text("Editar Perfil")
            }

            // Botón que cierra la sesión del usuario:
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                            viewModel.cerrarSesion { onLogout() }
                          },
                colors = ButtonDefaults.buttonColors(  // Ponemos el color rojo al botón (que es el de error)
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ){
                Text("Cerrar Sesión")
            }
        }


        // PESTAÑA QUE SE ABRE AL PULSAR EL BOTÓN DE EDITAR PERFIL PARA PODER ELEGIR Y EDITAR LOS CAMPOS QUE EL USUARIO DESEE
        if (mostrarSheet) {
            ModalBottomSheet(
                onDismissRequest = { mostrarSheet = false },  // Si se pulsa fuera de la pestaña se cerrará
                sheetState = sheetState,
                containerColor = Color(0xFF1C1C1C)
            ) {
               // Función donde se encuentran todos los componentes de la pestaña de Edición del perfil
               EditProfile(
                   viewModel = viewModel,
                   onVolver = {
                       coroutineScope.launch { sheetState.hide() }
                           .invokeOnCompletion { if (!sheetState.isVisible) mostrarSheet = false }
                   },
                   onGuardar = {
                       coroutineScope.launch {
                           viewModel.updateDataPlayer()
                           sheetState.hide()
                       }.invokeOnCompletion { if (!sheetState.isVisible) mostrarSheet = false }
                   }
               )
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


// Función que define la pestaña en la que se le permite editar al usuario los campos que desee
@Composable
fun EditProfile(
    viewModel: ProfileViewModel,
    onVolver: () -> Unit,  // Funcion para volver a la pantalla principal del Profile
    onGuardar: () -> Unit  // Funcion para guardar los datos editados
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp),
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
            value = viewModel.nombreEdit,
            onValueChange = { viewModel.nombreEdit = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        // Variable nacionalidad
        OutlinedTextField(
            value = viewModel.nacionalidadEdit,
            onValueChange = { viewModel.nacionalidadEdit = it },
            label = { Text("Nacionalidad") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        DatePickerField(
            fechaSeleccionada = viewModel.fechaNacimientoEdit,
            onFechaCambiada = {viewModel.fechaNacimientoEdit = it} )

        Spacer(modifier = Modifier.height(12.dp))

        // Editar mano dominante con menu depegable:
        SelectorOpciones(
            label = "Mano Dominante",
            seleccionado = viewModel.manoDominanteEdit,
            opciones = listOf("Derecha", "Izquierda"),
            onOptionSelected = { viewModel.manoDominanteEdit = it } // Actualizamos la variable
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Editar estilo de juego con menu depegable:
        SelectorOpciones(
            label = "Estilo de Juego",
            seleccionado = viewModel.estiloJuegoEdit,
            opciones = listOf(
                "Agresivo",
                "Defensivo",
                "Saque y Volea",
                "Contragolpeador"
            ),
            onOptionSelected = { viewModel.estiloJuegoEdit = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Editar mejor golpe con menu depegable:
        SelectorOpciones(
            label = "Golpe Maestro",
            seleccionado = viewModel.mejorGolpeEdit,
            opciones = listOf("Drive", "Revés", "Saque", "Volea", "Dejada"),
            onOptionSelected = { viewModel.mejorGolpeEdit = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Añadimos espacio entre los botones
        ) {
            // Botón de Volver
            Button(
                modifier = Modifier.weight(1f),
                onClick = onVolver
            ) {
                Text("Volver")
            }

            // Boton para guardar los datos en Supabase
            Button(
                modifier = Modifier.weight(1f),
                onClick = onGuardar
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}



