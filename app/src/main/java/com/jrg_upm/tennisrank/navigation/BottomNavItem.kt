package com.jrg_upm.tennisrank.navigation

import androidx.compose.ui.graphics.vector.ImageVector

// Definimos la información que necesita cada botón de la barra
data class BottomNavItem(
    val name: String,  // el nombre que aparece para cada sección
    val route: String,  // identificador único para la navegación
    val icon: ImageVector,  // icono que se mostrará
    val badgeCount: Int = 0  // Número para mostrar notificaciones
)