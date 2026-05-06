package com.jrg_upm.tennisrank.viewModel.home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrg_upm.tennisrank.supabase.updateResult
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    // Estados para los juegos (editables en la UI)
    val juegosJ1 = mutableStateListOf(0, 0, 0)
    val juegosJ2 = mutableStateListOf(0, 0, 0)

    // Inicializa los juegos con los valores que vienen de la DB
    fun inicializarMarcador(sets: List<com.jrg_upm.tennisrank.model.Set>) {
        // Solo inicializamos si la lista está a cero (evita sobreescribir lo que el usuario está editando)
        // o si detectamos que los sets de la DB han cambiado realmente.
        if (juegosJ1.all { it == 0 } && juegosJ2.all { it == 0 }) {
            val setsOrdenados = sets.sortedBy { it.numeroSet }
            setsOrdenados.forEachIndexed { index, set ->
                if (index < 3) {
                    juegosJ1[index] = set.juegosJugador1
                    juegosJ2[index] = set.juegosJugador2
                }
            }
        }
    }

    // Función para validar y guardar
    fun guardarResultado(
        partidoId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        var setsJ1 = 0
        var setsJ2 = 0
        var resultadoValido = true

        for (i in 0 until 3) {
            // Lógica de validación (la que tenías en la View)
            if (i == 2 && (setsJ1 == 2 || setsJ2 == 2)) {
                if (juegosJ1[i] != 0 || juegosJ2[i] != 0) { resultadoValido = false }
                break
            } else if ((juegosJ1[i] == 6 && juegosJ2[i] in 0..4) || (juegosJ1[i] == 7 && juegosJ2[i] in 5..6)) {
                setsJ1++
            } else if ((juegosJ2[i] == 6 && juegosJ1[i] in 0..4) || (juegosJ2[i] == 7 && juegosJ1[i] in 5..6)) {
                setsJ2++
            } else {
                resultadoValido = false
                break
            }
        }

        if (!resultadoValido) {
            onError("El resultado introducido no es válido")
            return
        }

        viewModelScope.launch {
            val exito = updateResult(partidoId, juegosJ1.toList(), juegosJ2.toList())
            if (exito) onSuccess() else onError("Error al conectar con la base de datos")
        }
    }

}