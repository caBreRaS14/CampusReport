package co.fexe.campusreport.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Esquema = lightColorScheme(
    primary = Color(0xFF1B5E9E), onPrimary = Color.White,
    secondaryContainer = Color(0xFFD6E6F7), onSecondaryContainer = Color(0xFF0B2E50),
    background = Color(0xFFF7F9FC), surface = Color.White,
    error = Color(0xFFB3261E)
)

@Composable
fun CampusTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = Esquema, content = content)
