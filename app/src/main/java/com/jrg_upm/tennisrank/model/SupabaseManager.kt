package com.jrg_upm.tennisrank.model

import com.jrg_upm.tennisrank.model.SupabaseClient.client
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.jrg_upm.tennisrank.BuildConfig.SUPABASE_URL
import com.jrg_upm.tennisrank.BuildConfig.SUPABASE_KEY
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage


// Conexión con Supabase
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,  // ambos valores se encuentran en local.properties por seguridad
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(io.github.jan.supabase.storage.Storage)
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
        e.printStackTrace() // Imprimimos el error
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
        client.postgrest["jugadores"]
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
        client.postgrest["jugadores"].select {
            order("puntos", order = Order.DESCENDING)
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
        client.postgrest["jugadores"].update(
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
    nombre: String,
    nacionalidad: String,
    fechaNacimiento: String,
    manoDominante: String,
    estilo: String,
    mejorGolpe: String
){
    try{  // Datos de la tabla jugadores
        client.postgrest["jugadores"].update(
            {  // Campos a actualizar
                set("nombre", nombre)
                set("nacionalidad", nacionalidad)
                set("fecha_nacimiento", fechaNacimiento)
                set("mano_dominante", manoDominante)
                set("estilo_juego", estilo)
                set("mejor_golpe", mejorGolpe)
            }
        ){
            filter{eq("id", idUsuario)}
        }
    } catch( e: Exception){
        e.printStackTrace()
    }
}
