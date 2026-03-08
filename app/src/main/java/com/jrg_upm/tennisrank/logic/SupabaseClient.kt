package com.jrg_upm.tennisrank.logic

import com.jrg_upm.tennisrank.logic.SupabaseClient.client
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.jrg_upm.tennisrank.BuildConfig.SUPABASE_URL
import com.jrg_upm.tennisrank.BuildConfig.SUPABASE_KEY


// Conexión con Supabase
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,  // ambos valores se encuentran en local.properties por seguridad
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}


// funciones suspend para no bloquear el resto del proceso

// FUNCIÓN DE LOGIN
// Verifica que el email y la contraseña sean correctos con la authentication que hay en supabase
suspend fun loginUser(email: String, pass: String): Boolean {
    return try {
        SupabaseClient.client.auth.signInWith(Email) {
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

