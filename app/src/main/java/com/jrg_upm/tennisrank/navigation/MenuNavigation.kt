package com.jrg_upm.tennisrank.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.jrg_upm.tennisrank.ui.theme.TennisRankTheme

@Composable
fun MenuScreen() {
    //Each screen will be its own composable
    // Theme para estilizar la aplicación
    TennisRankTheme() {
        // Objeto que controla la navegación, debe ser el mismo para la barra de botones como para el NavHost que cambia las pantallas
        val navController = rememberNavController()
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
                Navigate(navController = navController)
            }
        }
    }

}