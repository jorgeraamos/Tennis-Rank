package com.jrg_upm.tennisrank.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrg_upm.tennisrank.supabase.registerUser
import kotlinx.coroutines.launch

// Función en la que se define la screen del inicio de sesión:
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onNavigateBack: () -> Unit) {
    // Nos creamos las variables state para el correo y la contraseña que debe introducir el usuario:
    var user_name by remember{ mutableStateOf("") }
    var user_email by remember{ mutableStateOf("") }
    var password by remember{ mutableStateOf("") }
    var password_repetead by remember{ mutableStateOf("") }


    // Al igual que en el login, scope es necesario para que la app no se quede colgada esperando a la función suspend

    val scope = rememberCoroutineScope()

    // State para controlar los popUps de error:
    val snackbarHostState = remember{ SnackbarHostState() }


    // Scaffold para organizar los espacios de la pantalla automáticamente, es el esqueleto de la pantalla
    Scaffold(
        // Le indicamos al Scaffold dónde debe poner los mensajes de error
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp)
        ){
            // Ponemos el título de la aplicación
            Text(
                text = "REGISTRO CUENTA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)  // Color Azul
            )
            // Ponemos un espacio entre el texto y los campos a rellenar
            Spacer(modifier = Modifier.height(32.dp))

            // Campo para introducir el correo:
            OutlinedTextField(
                value = user_name,
                onValueChange = { user_name = it},
                label = { Text("Nombre y Apellidos:") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            // Añadimos un espaico entre los campos
            Spacer(modifier = Modifier.height(16.dp))

            // Campo para introducir el correo:
            OutlinedTextField(
                value = user_email,
                onValueChange = { user_email = it},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            // Añadimos un espaico entre los campos
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it},
                label = { Text("Contraseña: Mínimo 8 carácteres") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            // Añadimos un espaico entre los campos
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password_repetead,
                onValueChange = { password_repetead = it},
                label = { Text("Confirmar Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            // Añadimos un espacio para los botones:
            Spacer(modifier = Modifier.height(32.dp))

            // Botón para volver hacia atrás
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Añadimos espacio entre los botones
            ){
                // Botón para volver:
                Button(
                    onClick = {
                        // Volvemos a la pantalla de inicio de sesión
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1f)
                ){
                    Text("Volver")
                }

                // Botón para registrarse:
                Button(
                    onClick = {
                        if( password.equals(password_repetead) && password.length >= 8 ){
                            scope.launch {
                                val success = registerUser(user_email, password, user_name)
                                if(success){  // Si la verificación ha sido correcta ejecutamos la función de success
                                    onRegisterSuccess()
                                }
                                else{  // En caso contrario mostramos un mensaje de error
                                    snackbarHostState.showSnackbar("Error: Al registrarse, vuelva a intentarlo")
                                }
                            }

                        } // En caso contrario mostramos un mensaje de error:
                        else if( ! password.equals(password_repetead)){
                            scope.launch{
                                snackbarHostState.showSnackbar("Las contraseñas no coinciden")
                            }
                        }
                        else if( password.length <8){
                            scope.launch{
                                snackbarHostState.showSnackbar("La contraseña introducida es muy corta")
                            }
                        }
                        else{
                            scope.launch{
                                snackbarHostState.showSnackbar("Error al registrarse, compruebe bien los campos")
                            }
                        }

                    },
                    modifier = Modifier.weight(1f)
                ){
                    Text("Registrarse")
                }

            }

        }

    }
}



