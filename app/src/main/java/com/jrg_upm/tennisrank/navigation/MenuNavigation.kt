package com.jrg_upm.tennisrank.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.jrg_upm.tennisrank.ui.theme.TennisRankTheme
import com.jrg_upm.tennisrank.viewModel.menu.MenuViewModel

@Composable
fun MenuScreen(viewModel: MenuViewModel, onLogout: () -> Unit) {
    //Each screen will be its own composable
    // Theme para estilizar la aplicación
    TennisRankTheme() {
        // Objeto que controla la navegación, debe ser el mismo para la barra de botones como para el NavHost que cambia las pantallas
        val navController = rememberNavController()

        // Conectamos con supabase para obtener el jugadorActual
        // Launched Effect indica que se ejecute solo cuando se redibuje la pantalla, para no tener que
        // llamar infinitas veces al getCurrentPlayer y no colapsar la base de datos a llamadas
        LaunchedEffect(Unit) {
            // Cargamos el jugador actual
            viewModel.cargarJugador()
        }

        if(viewModel.isLoading){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally // Centra hijos horizontalmente
                ) {
                    Text(
                        text = "Cargando...",
                        style = MaterialTheme.typography.headlineSmall, // Texto más grande
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp)) // Más espacio entre texto y círculo

                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp), // Aumentamos el tamaño del círculo
                        color = Color(0xFF1976D2),
                        strokeWidth = 6.dp // Más grosor para que destaque
                    )
                }
            }

        } else if (viewModel.errorMessage != null) {
            // Mostrar el error y un botón de reintentar
            Text(text = viewModel.errorMessage!!)
        }
        else {
            // Scaffold es el componente que organiza los espacios de la pantalla automáticamente, es el esqueleto de la pantalla
            Scaffold(
                bottomBar = {
                    // Llamamos a la función que habíamos definido para la barra de botones
                    BottomNavigationBar(
                        items = listOf(
                            BottomNavItem(
                                name = "Home",
                                route = "home",
                                icon = Icons.Default.Home
                            ),
                            BottomNavItem(
                                name = "Historical",
                                route = "historical",
                                icon = Icons.Default.DateRange
                            ),
                            BottomNavItem(
                                name = "Statistics",
                                route = "statistics",
                                icon = Icons.Default.Info
                            ),
                            BottomNavItem(
                                name = "Profile",
                                route = "profile",
                                icon = Icons.Default.AccountCircle
                            )
                        ),
                        navController = navController,
                        // Lógica que se ejecuta cuando se pulsa un botón:
                        // Cambiamos de pantalla internamente.
                        onItemClick = { item ->
                            navController.navigate(item.route) {
                                // evita acumular pantallas en el backstack, es decir, evita que si el usuario
                                // pulsa muchas veces los botones, se cree una pila infinita de pantallas
                                popUpTo(navController.graph.startDestinationId)
                                // evita que se abra la misma pantalla dos veces si ya estás en ella
                                launchSingleTop = true
                            }
                        }
                    )
                }
            ) { paddingValues ->
                // Es vital usar paddingValues para que el contenido no se tape con los elementos fijos del Scaffold
                Column(modifier = Modifier.padding(paddingValues)) {
                    // Cambiamos visualmente de pantalla
                    Navigate(
                        navController = navController,
                        menuViewModel =  viewModel,
                        onLogout = onLogout, // Para poder cerrar sesión
                        onProfileUpdated = { viewModel.cargarJugador() } // Función para poder actualizar al jugador actual
                    )
                }
            }
        }
    }
}

