package com.jrg_upm.tennisrank.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jrg_upm.tennisrank.logic.Jugador
import com.jrg_upm.tennisrank.screens.HistoricalScreen
import com.jrg_upm.tennisrank.screens.HomeScreen
import com.jrg_upm.tennisrank.screens.ProfileScreen
import com.jrg_upm.tennisrank.screens.StatisticsScreen
import kotlin.compareTo
import kotlin.toString

// Gestor de Pantallas dentro del Menú de la App:
// navController es el objeto que ejecuta las órdenes de ir de una pantalla a otra
@Composable
fun Navigate(navController: NavHostController, jugadorActual: Jugador?) {
    NavHost(navController = navController, startDestination = "home"){
        // Para cada ruta definimos la función que se ejecuta
        composable("home", ){
            HomeScreen(jugadorActual)
        }

        composable("historical", ){
            HistoricalScreen()
        }

        composable("statistics", ){
            StatisticsScreen()
        }

        composable("profile", ){
            ProfileScreen(jugadorActual)
        }
    }

}


// Componente visual del menú de botones:
@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavItem) -> Unit
) {
    // Para saber en qué pantalla estamos
    val backStackEntry = navController.currentBackStackEntryAsState()
    NavigationBar(
        modifier = modifier,
        containerColor = Color.DarkGray,
        tonalElevation = 5.dp
    ) { // Recorre la lista de botones y para cada uno crea un NavigationBarItem
        items.forEach { item ->
            // booleano que compara si la ruta actual del botón coincide con la ruta en la que se está
            val selected = item.route == backStackEntry.value?.destination?.route
            NavigationBarItem(
                // We need a state for whenever the route changes
                selected = selected,
                onClick = { onItemClick(item) },  // Ejecuta la función que le pasemos (navController.navigate(item.route))
                icon = {
                    // Si tiene notificaciones envolvemos el icono en un BadgedBox para mostrar dicho número
                    if (item.badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(text = item.badgeCount.toString())
                                }
                            }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.name
                            )
                        }
                    }
                    // Si no tiene notificaciones mostramos simplemente el Icono
                    else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.name
                        )
                    }
                    // Solo mostramos el nombre de la pestaña si el botón está seleccionado
                    if (selected) {
                        Text(
                            text = item.name,
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp
                        )
                    }
                },
                // Definimos los colores de los items
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.Gray
                )
            )
        }
    }
}


