package com.jrg_upm.tennisrank.viewModel.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.supabase.getCurrentPlayer
import com.jrg_upm.tennisrank.supabase.signOutUser
import com.jrg_upm.tennisrank.supabase.updateAvatarUrl
import com.jrg_upm.tennisrank.supabase.updatePlayerData
import com.jrg_upm.tennisrank.supabase.uploadProfileImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileViewModel(val jugadorInicial: Jugador?) : ViewModel() {

    var jugadorActual by mutableStateOf(jugadorInicial)
    // Variables para que el usuario pueda editar dichos campos:
    var nombreEdit by mutableStateOf(jugadorActual?.nombre ?: "")

    var paisEdit by mutableStateOf(jugadorActual?.pais ?: "")
    var fechaNacimientoEdit by mutableStateOf(jugadorActual?.fechaNacimiento ?: "")

    val edadJugador: String
        get() = calcularEdad(jugadorActual?.fechaNacimiento) ?: "--"

    var manoDominanteEdit by mutableStateOf(jugadorActual?.manoDominante ?: "")


    var estiloJuegoEdit by mutableStateOf(jugadorActual?.estiloJuego ?: "")

    var mejorGolpeEdit by mutableStateOf(jugadorActual?.mejorGolpe ?: "")

    var superficieFavoritaEdit by mutableStateOf(jugadorActual?.superficieFavorita ?: "")


    // Variable de estado para actualizar la imagen del jugador
    var imagenTemporalUrl by mutableStateOf(jugadorActual?.avatarUrl)

    // Funciones de lógica que realiza esta screen:

    // Función para subir la foto de perfil a Supabase
    fun subirFoto(bytes: ByteArray?) {
        if (bytes != null && jugadorActual != null) {
            viewModelScope.launch {  // Con launch se abre otro hilo
                // Subimos la imagen a Supabase Storage
                val url = uploadProfileImage(jugadorActual!!.id, bytes)
                imagenTemporalUrl = "$url?t=${System.currentTimeMillis()}"
                // Actualizamos el campo avatar_url de la tabla jugadores en Supabase
                // se ejecuta en 2º plano, por eso lo ponemos después
                updateAvatarUrl(jugadorActual!!.id, url)
            }
        }
    }


    // Función para guardar los datos editados del jugador
    // Necesario que sea suspend para luego poder, una vez se han guardado los datos, cerrar la pestaña de edición
    suspend fun updateDataPlayer() {
        if (jugadorActual != null) {
            try {
                updatePlayerData(
                    idUsuario = jugadorActual!!.id,
                    pais = paisEdit,
                    fechaNacimiento = fechaNacimientoEdit,
                    manoDominante = manoDominanteEdit,
                    estilo = estiloJuegoEdit,
                    mejorGolpe = mejorGolpeEdit,
                    superficieFavorita = superficieFavoritaEdit
                )
                // Actualizamos los campos de la pantalla de perfil inmediatamente
                val nuevoJugador = getCurrentPlayer()
                if (nuevoJugador != null) {
                    jugadorActual = nuevoJugador
                }
            } catch (e: Exception) {
                Log.e("UPDATE_DEBUG", "Error: ${e.message}")
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

    fun cerrarSesion(onLogout: () -> Unit){
        viewModelScope.launch{
            val cierreSesion = signOutUser()
            if(cierreSesion){
                onLogout()
            }
            else{
                // Se puede incluir un estado de error
            }
        }
    }

}