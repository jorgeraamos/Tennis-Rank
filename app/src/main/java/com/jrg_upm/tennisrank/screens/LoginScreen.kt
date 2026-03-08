package com.jrg_upm.tennisrank.screens

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Función en la que se define la screen del inicio de sesión:
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit) {
    // Nos creamos las variables state para el correo y la contraseña que debe introducir el usuario:
    var user_email by remember{ mutableStateOf("") }
    var password by remember{ mutableStateOf("") }

    // Scaffold para organizar los espacios de la pantalla automáticamente, es el esqueleto de la pantalla
    Scaffold()
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp)
        ){
            // Ponemos el título de la aplicación
            Text(
                text = "RANKING TENNIS UPM",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)  // Color Azul
            )
            // Ponemos un espacio entre el texto y los campos a rellenar
            Spacer(modifier = Modifier.height(32.dp))

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
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            // Añadimos un espacio para los botones:
            Spacer(modifier = Modifier.height(32.dp))
            Row(){

                // Botón para registrarse:
                Button(
                    onClick = {
                        onNavigateToRegister()
                    },
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("Registrarse")
                }

                // Botón para entrar:
                Button(
                    onClick = {
                        // Función que verifica el inicio de sesión:
                        onLoginSuccess()
                    },
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("Iniciar Sesión")
                }
            }


        }

    }
}