package com.jrg_upm.tennisrank.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jrg_upm.tennisrank.ui.auth.LoginScreen
import com.jrg_upm.tennisrank.ui.auth.RegisterScreen
import com.jrg_upm.tennisrank.viewModel.Menu.MenuViewModel


// Definimos las distintas pantallas a las que podemos navegar a la hora de iniciar sesión:
@Composable
fun LogNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login")
    {
        // Ruta del login:
        composable("login"){
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("menu"){
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // Ruta del registro:
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    // Tras registrarse, volvemos al login (o podrías ir directo al home)
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack() // Simplemente vuelve atrás
                }
            )
        }
        // Ruta del menu que contiene las 4 pestañas
        composable ("menu") {
            // Instanciamos el ViewModel
            val menuViewModel: MenuViewModel = viewModel()
            MenuScreen(
                viewModel = menuViewModel,
                onLogout = {  // Función que se pasa para poder cerrar sesión una vez se esté dentro de la app
                    navController.navigate("login") {
                        popUpTo("menu") { inclusive = true }
                    }
                }
            )
        }


    }

}