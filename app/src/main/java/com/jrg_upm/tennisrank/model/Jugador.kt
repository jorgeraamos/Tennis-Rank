package com.jrg_upm.tennisrank.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Jugador(
    @SerialName("id") val id: String,
    @SerialName("nombre_completo") val nombre: String,
    @SerialName("email") val email: String = "", // Valor por defecto
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("pais") val pais: String? = null,
    @SerialName("fecha_nacimiento") val fechaNacimiento: String? = null,
    @SerialName("mano_dominante") val manoDominante: String? = null,
    @SerialName("estilo_juego") val estiloJuego: String? = null,
    @SerialName("mejor_golpe") val mejorGolpe: String? = null,
    @SerialName("superficie_favorita") val superficieFavorita: String? = null
)
