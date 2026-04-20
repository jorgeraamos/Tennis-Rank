package com.jrg_upm.tennisrank.supabase

import android.util.Log
import com.jrg_upm.tennisrank.supabase.SupabaseClient.client
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.jrg_upm.tennisrank.BuildConfig.SUPABASE_URL
import com.jrg_upm.tennisrank.BuildConfig.SUPABASE_KEY
import com.jrg_upm.tennisrank.model.Jornada
import com.jrg_upm.tennisrank.model.Jugador
import com.jrg_upm.tennisrank.model.Participante
import com.jrg_upm.tennisrank.model.Partido
import com.jrg_upm.tennisrank.model.Set
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import java.util.Collections


// Conexión con Supabase
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,  // ambos valores se encuentran en local.properties por seguridad
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}


// funciones suspend para no bloquear el resto del proceso

// FUNCIÓN DE LOGIN
// Verifica que el email y la contraseña sean correctos con la authentication que hay en supabase
suspend fun loginUser(email: String, pass: String): Boolean {
    return try {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = pass
        }
        true
    } catch (e: Exception) {
        Log.e("LOGIN_DEBUG", "Error detallado: ${e.message}") // Imprimimos el error
        false
    }
}

// FUNCIÓN DE SIGNUP
suspend fun registerUser(emailInput: String, passInput: String, nameInput: String): Boolean {
    return try {
        client.auth.signUpWith(Email) {
            email = emailInput
            password = passInput
            // Metadatos para el Trigger de la base de datos
            data = buildJsonObject {
                put("full_name", nameInput)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// FUNCIÓN PARA CERRAR SESIÓN
suspend fun signOutUser(): Boolean {
    return try {
        client.auth.signOut()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// FUNCIÓN PARA OBTENER EL JUGADOR QUE ESTÁ EJECUTANDO LA APP
suspend fun getCurrentPlayer(): Jugador? {
    // Obtenemos el ID del usuario que tiene la sesión abierta
    val user = client.auth.currentUserOrNull() ?: return null
    val userId = user.id

    // Buscamos en la tabla 'jugadores' la fila que coincida con ese ID
    return try {
        client.postgrest["jugador"]
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<Jugador>() // Lo convertimo automáticamente a la data class de Jugador
    } catch (e: Exception) {  // Si da error devolvemos null
        null
    }
}


// FUNCION PARA OBTENER EL RANKING ACTUAL
suspend fun getAllPlayers(): List<Jugador> {
    return try {
        // Cogemos a todos los jugadores ordenados por sus puntos
        client.postgrest["jugador"].select {
            order("puntos_actuales", order = Order.DESCENDING)
        }.decodeList<Jugador>()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}


// FUNCION PARA SUBIR LAS FOTOS DE PERFIL PARA CADA USUARIO EN EL STORAGE DE SUPABASE
suspend fun uploadProfileImage(userId: String, imageBytes: ByteArray): String {
    val bucket = client.storage.from("profile-images")
    val fileName = "$userId.jpg"

    // 1. Subir (o actualizar) la imagen
    bucket.upload(fileName, imageBytes) {
        upsert = true
    }

    // 2. Obtener la URL pública para guardarla luego en la tabla
    return bucket.publicUrl(fileName)
}


// FUNCION PARA ACTUALIZAR EL CAMPO DE LA URL DE LA IMAGEN DE PERFIL DE CADA JUGADOR EN SUPABASE
suspend fun updateAvatarUrl(idUsuario: String, nuevaUrl: String) {
    try {
        // Seleccionamos de la tabla jugadores y actualizamos el campo avatar_url por la nueva url
        client.postgrest["jugador"].update(
            {
                // El nombre entre comillas debe ser EXACTO al de tu tabla en Supabase
                set("avatar_url", nuevaUrl)
            }
        ) {
            filter {  // Filtramos por id para actualizar solo el campo del usuario actual
                eq("id", idUsuario)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


//FUNCION PARA ACTUALIZAR LA INFORMACIÓN DE CADA JUGADOR
suspend fun updatePlayerData(
    idUsuario: String,
    pais: String,
    fechaNacimiento: String,
    manoDominante: String,
    estilo: String,
    mejorGolpe: String,
    superficieFavorita: String
){
    try{  // Datos de la tabla jugadores
        client.postgrest["jugador"].update(
            {  // Campos a actualizar
                set("pais", pais)
                // Enviamos la fecha solo si no está en blanco, ya que si daría error al enviar "" a Supabase
                if(fechaNacimiento.isNotBlank()) set("fecha_nacimiento", fechaNacimiento)
                set("mano_dominante", manoDominante)
                set("estilo_juego", estilo)
                set("mejor_golpe", mejorGolpe)
                set("superficie_favorita", superficieFavorita)
            }
        ){
            filter{eq("id", idUsuario)}
        }
        Log.d("SUPABASE", "Datos actualizados correctamente en el servidor")
    } catch( e: Exception){
        Log.e("SUPABASE_ERROR", "Error al actualizar: ${e.message}")
        e.printStackTrace()
    }
}

// Función para obtener todos los partidos del usuario:
suspend fun getAllPartidosConSets(idJugador: String, estado: String): List<Pair<Partido, List<Set>>> {
    return try {
        // Obtenemos todos los partidos que coincidan con el filtro de estado, para así
        // poder separar ver los partidos ya jugados a los que ya se han jugado
        val response = client.postgrest["partido"].select {
            filter {
                and {
                    eq("estado", estado)
                    or {
                        eq("id_jugador1", idJugador)
                        eq("id_jugador2", idJugador)
                    }
                }
            }
            order(column = "id_jornada", order = Order.DESCENDING)
        }

        val partidos = response.decodeList<Partido>()

        // Mapeamos cada partido a su par con sets
        partidos.map { partido ->
            val setsResponse = client.postgrest["set"].select {
                filter { eq("id_partido", partido.id) }
            }
            val listaSets = setsResponse.decodeList<Set>().sortedBy { it.numeroSet }

            Pair(partido, listaSets)
        }

    } catch (e: Exception) {
        Log.e("SUPABASE", "Error en getAllPartidos: ${e.message}")
        emptyList() // Devolvemos lista vacía en lugar de null para evitar errores en el LazyColumn
    }
}


suspend fun getPartidosPorJornada(idJugador: String, estado: String): List<Pair<Jornada, Pair<Partido, List<Set>>>> {
    return try {
        // Buscamos todas las jornadas que tengan el estado indicado
        val jornadas = client.postgrest["jornada"].select {
            filter { eq("estado", estado) }
            order("numero", Order.DESCENDING)
        }.decodeList<Jornada>()

        //Mapeamos cada jornada para buscar el partido del usuario en ella
        jornadas.mapNotNull { jornada ->
            // Buscamos el partido del jugador en ESTA jornada
            val partidoResponse = client.postgrest["partido"].select {
                filter {
                    and {
                        eq("id_jornada", jornada.id)
                        or {
                            eq("id_jugador1", idJugador)
                            eq("id_jugador2", idJugador)
                        }
                    }
                }
                limit(1)
            }

            val partido = partidoResponse.decodeSingleOrNull<Partido>()

            if (partido != null) {
                // Si hay partido, buscamos sus sets
                val setsResponse = client.postgrest["set"].select {
                    filter { eq("id_partido", partido.id) }
                }
                val listaSets = setsResponse.decodeList<Set>().sortedBy { it.numeroSet }

                // Devolvemos el trío Jornada -> (Partido, Sets)
                Pair(jornada, Pair(partido, listaSets))
            } else {
                // Si el jugador no tiene partido en esta jornada, devolvemos null
                // y mapNotNull lo eliminará de la lista final
                null
            }
        }
    } catch (e: Exception) {
        Log.e("SUPABASE", "Error obteniendo partidos por jornadas: ${e.message}")
        emptyList()
    }
}


// Función para obtener a todos los participantes de la edición en la que participa el usuario:
// Recordemos que un usuario solo puede estar activo en una única edición
suspend fun getAllParticipantes(idJugador: String): List<Participante> {
    val idEdicion = try {
        // Traemos todas las ediciones donde participa el usuario
        val responseParticipa = client.postgrest["participa"].select(Columns.list("id_edicion")) {
            filter { eq("id_jugador", idJugador) }
        }
        val ediciones = responseParticipa.decodeList<Map<String, Int>>().map { it["id_edicion"] }

        if (ediciones.isEmpty()) return emptyList()

        // Buscamos de sus ediciones la que está activa a través de la tabla edicion
        // Recordar que un jugador solo puede estar en una edicion activa.
        val responseEdicion = client.postgrest["edicion"].select(Columns.list("id")) {
            filter {
                isIn("id", ediciones as List<Any>)
                eq("estado", "activo")
            }
            limit(1)
        }

        val edicionActiva = responseEdicion.decodeList<Map<String, Int>>()
        if (edicionActiva.isNotEmpty()) {
            edicionActiva[0]["id"] ?: return emptyList()
        } else {
            return emptyList()
        }
    } catch (e: Exception) {
        println("Error en la búsqueda de edición: ${e.message}")
        return emptyList()
    }

    println("Se ha encontrado la edición del jugador: $idEdicion")

    // Traemos a todos los participantes de esa edición con sus datos de jugador
    return try {
        val response = client.postgrest["participa"]
            .select(Columns.raw(
                "id_edicion, id_jugador, puntos, partidos_jugados, historial_rivales, jugador(id, nombre_completo)")) {
                filter {
                    eq("id_edicion", idEdicion)
                }
                order("puntos", order = Order.DESCENDING)
            }
        response.decodeList<Participante>()
    } catch (e: Exception) {
        println("Error cargando participantes de la edición $idEdicion: ${e.message}")
        emptyList()
    }
}


// Funcion para guardar el resultado de un partido
suspend fun updateResult(
    idPartido: String,
    juegosJ1: List<Int>,
    juegosJ2: List<Int>
): Boolean{
    return try {
        for (i in 0 until 3) {
            client.postgrest["set"].update(
                {  // Actualizamos los juegos de cada jugador para cada set
                    set("juegos_jugador1", juegosJ1[i])
                    set("juegos_jugador2", juegosJ2[i])
                }
            ) {
                filter {
                    eq("id_partido", idPartido)
                    eq("numero_set", i + 1)
                }
            }
        }
        // Actualizamos el estado del partido
        client.postgrest["partido"].update(
            {
                set("estado", "Jugado")
            }
        ){
            filter {
                eq("id", idPartido)
            }
        }
        Log.d("SUPABASE", "Todos los sets actualizados")
        true // Si termina el bucle sin fallos, devolvemos true
    } catch (e: Exception) {
        Log.e("SUPABASE_ERROR", "Error: ${e.message}")
        false // Si algo falla, devolvemos false
    }
}


