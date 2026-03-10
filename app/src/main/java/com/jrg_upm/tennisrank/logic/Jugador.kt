package com.jrg_upm.tennisrank.logic

import kotlinx.serialization.Serializable

@Serializable
data class Jugador(
   val nombre: String,
    val puntos: Int = 0,
    val posicion_ranking: Int = 0
)
