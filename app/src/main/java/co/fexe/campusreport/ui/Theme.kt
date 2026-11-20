package co.fexe.campusreport.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

val Azul = Color(0xFF0F4C81)
val AzulClaro = Color(0xFF2B7BC4)
val Fondo = Color(0xFFF1F5F9)
val Degradado = Brush.linearGradient(listOf(Azul, AzulClaro))

private val Esquema = lightColorScheme(
    primary = Azul, onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E6F7), onPrimaryContainer = Color(0xFF0B2E50),
    secondaryContainer = Color(0xFFD6E6F7), onSecondaryContainer = Color(0xFF0B2E50),
    background = Fondo, surface = Color.White, surfaceContainer = Color.White,
    outline = Color(0xFF8A97A8), error = Color(0xFFB3261E)
)

private val Formas = Shapes(
    small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp)
)

fun colorCategoria(c: String) = when (c) {
    "Infraestructura" -> Color(0xFFE8710A)
    "Equipos" -> Color(0xFF7B3FC4)
    "Conectividad" -> Color(0xFF0C8F8F)
    else -> Color(0xFF5F6B7A)
}

fun iconoCategoria(c: String): ImageVector = when (c) {
    "Infraestructura" -> Icons.Default.Apartment
    "Equipos" -> Icons.Default.Computer
    "Conectividad" -> Icons.Default.Wifi
    else -> Icons.Default.MoreHoriz
}

@Composable
fun CampusTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = Esquema, shapes = Formas, content = content)
