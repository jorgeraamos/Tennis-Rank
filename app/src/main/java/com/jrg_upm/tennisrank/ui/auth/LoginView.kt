package com.jrg_upm.tennisrank.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jrg_upm.tennisrank.viewModel.Auth.LoginViewModel
import kotlinx.coroutines.launch

// Función en la que se define la screen del inicio de sesión:
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),  // viewModel para obtener la lógica de la pantalla
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {

    // scope es necesario para poder crear un entorno seguro donde podemos llamar a una función suspend
    // para que la app no se quede colgada esperando a esa función
    val scope = rememberCoroutineScope()

    // Nos creamos un sanckbarHostState para mostrar mensajes en caso de error
    val snackbarHostState = remember { SnackbarHostState() }

    // Scaffold para organizar los espacios de la pantalla automáticamente, es el esqueleto de la pantalla
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp)
        ){
            // 0xFF1976D2
            // 0xFF0D47A1

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // Centra el logo en la pantalla
                verticalAlignment = Alignment.CenterVertically // Alinea verticalmente texto e icono
            ){
                // Ponemos el título de la aplicación y el icono:
                Text(
                    text = "TennisRank",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFF1976D2) // Color azul
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.SportsTennis, // O Icons.Default.EmojiEvents para un trofeo
                    contentDescription = "Logo Tennis",
                    tint = Color(0xFF9CF527), // Color pelota tenis
                    modifier = Modifier.size(40.dp)
                )
            }

            // Ponemos un espacio entre el texto y los campos a rellenar
            Spacer(modifier = Modifier.height(32.dp))

            // Campo para introducir el correo:
            // Los campos de texto se leen y escriben en el ViewModel
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            // Añadimos un espaico entre los campos
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it},
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            // Añadimos un espacio para los botones:
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Añadimos espacio entre los botones
            ){
                // Botón para registrarse:
                Button(
                    onClick = {
                        onNavigateToRegister()
                    },
                    modifier = Modifier.weight(1f)
                ){
                    Text("Registrarse")
                }

                // Botón para entrar:
                Button(
                    onClick = {
                        viewModel.onLoginClick(
                            onSuccess = onLoginSuccess,
                            onError = { message ->
                                scope.launch { snackbarHostState.showSnackbar(message) }
                            }
                        )
                    },
                    enabled = !viewModel.isLoading, // Deshabilitar si está cargando
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (viewModel.isLoading) "Cargando..." else "Iniciar Sesión")
                }
            }
        }
    }
}


