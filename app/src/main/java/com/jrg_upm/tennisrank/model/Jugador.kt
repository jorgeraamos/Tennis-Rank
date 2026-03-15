package com.jrg_upm.tennisrank.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Jugador(
    // Ponemos SerialName para indicar el nombre de las columnas que hay en supabase
    @SerialName("id") val id: String,
    @SerialName("nombre") val nombre: String,
    @SerialName("puntos") val puntos: Int = 0,
    @SerialName("posicion_ranking") val posicionRanking: Int = 0,
    @SerialName("nacionalidad") val nacionalidad: String? = null,
    @SerialName("fecha_nacimiento") val fechaNacimiento: String? = null,
    @SerialName("mano_dominante") val manoDominante: String? = null,
    @SerialName("estilo_juego") val estiloJuego: String? = null,
    @SerialName("mejor_golpe") val mejorGolpe: String? = null,
    @SerialName("superficie_favorita") val superficieFavorita: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null

)
