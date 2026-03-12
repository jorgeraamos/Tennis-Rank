package com.jrg_upm.tennisrank.screens

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jrg_upm.tennisrank.logic.Jugador
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jrg_upm.tennisrank.logic.updateAvatarUrl
import com.jrg_upm.tennisrank.logic.uploadProfileImage
import kotlinx.coroutines.launch
// Imports para calcular la edad
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun ProfileScreen(jugadorActual: Jugador?) {  // Recibimos el usuario que está ejecutando la app
    // Variable para poder subir la foto de perfil de cada jugador:
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
            val bytes = inputStream?.readBytes()  // Leemos toda la información binaria de la foto y la convertimos en un ByteArray
            if (bytes != null && jugadorActual != null) {
                coroutineScope.launch {
                    // Subimos a Supabase Storage
                    val url = uploadProfileImage(jugadorActual.id, bytes)
                    // Actualizamos el campo avatar_url de la tabla jugadores en Supabase
                    updateAvatarUrl(jugadorActual.id, url)
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Para la foto de perfil ponemos o bien la foto si la ha puesto el usuario o su inicial en caso contrario
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Yellow, shape = CircleShape)
                .clip(CircleShape)
                .clickable { launcher.launch("image/*") },  // Aquí se activa el selector de la imagen
            contentAlignment = Alignment.Center
        ) {
            // Si hay foto guardada la mostramos
            if (jugadorActual?.avatarUrl != null) {
                // Usamos AsyncImage de Coil para cargar la foto de Supabase
                AsyncImage(
                    model = jugadorActual.avatarUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    // Para que rellene el círculo sin deformarse en caso de que la foto sea rectangular
                    contentScale = ContentScale.Crop
                )
            }else {
                // Si no hay foto, mostramos la inicial de su nombre
                Text(
                    text = jugadorActual?.nombre?.firstOrNull()?.toString() ?: "?",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Mostramos el nombre del jugador debajo de su imagen
        Text(
            text = jugadorActual?.nombre ?: "Nombre",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        // Ponemos un espacio entre medias
        Spacer(modifier = Modifier.height(24.dp))

        // Info Técnica en una Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileItem("Nacionalidad", jugadorActual?.nacionalidad ?: "No definida")
                ProfileItem("Edad", calcularEdad(jugadorActual?.fechaNacimiento) ?: "--")
                ProfileItem("Mano", jugadorActual?.manoDominante ?: "No definida")
                ProfileItem("Estilo", jugadorActual?.estiloJuego ?: "No definido")
                ProfileItem("Golpe Maestro", jugadorActual?.mejorGolpe ?: "No definido")
            }
        }
    }
}

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
            if (hoy.get(Calendar.DAY_OF_MONTH) >= nacimiento.get(Calendar.DAY_OF_MONTH)) {
                edad--
            }
        }
        "$edad años"
    } catch (e: Exception) {
        null
    }
}

