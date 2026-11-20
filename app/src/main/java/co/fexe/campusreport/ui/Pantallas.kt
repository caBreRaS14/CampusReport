package co.fexe.campusreport.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import co.fexe.campusreport.data.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Encabezado(titulo: String, subtitulo: String) {
    val horizontal = androidx.compose.ui.platform.LocalConfiguration.current.orientation ==
        android.content.res.Configuration.ORIENTATION_LANDSCAPE
    if (horizontal) {
        Box(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(Degradado)
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp)
        ) {
            Text(titulo, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        return
    }
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Degradado)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("CampusReport", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(6.dp))
            Text(titulo, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitulo, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormularioScreen(vm: ReporteViewModel, onGuardado: () -> Unit) {
    val categoria by vm.categoria.collectAsState()
    val descripcion by vm.descripcion.collectAsState()
    val ubicacion by vm.ubicacion.collectAsState()
    val err by vm.errores.collectAsState()
    val guardando by vm.guardando.collectAsState()

    Column(Modifier.fillMaxSize().imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Encabezado("Nueva novedad", "Todos los campos son obligatorios. Se guarda solo en este dispositivo.")
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Card(
                    Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Categoría *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CATEGORIAS.forEach { c ->
                                val col = colorCategoria(c)
                                FilterChip(
                                    selected = categoria == c, onClick = { vm.setCategoria(c) },
                                    label = { Text(c) },
                                    leadingIcon = { Icon(iconoCategoria(c), null, Modifier.size(18.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = col.copy(alpha = 0.16f),
                                        selectedLabelColor = col, selectedLeadingIconColor = col,
                                        iconColor = col.copy(alpha = 0.8f)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true, selected = categoria == c,
                                        selectedBorderColor = col, selectedBorderWidth = 1.5.dp
                                    )
                                )
                            }
                        }
                        err.categoria?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Card(
                    Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = descripcion, onValueChange = vm::setDescripcion,
                            label = { Text("Descripción *") }, placeholder = { Text("Ej.: la luminaria del fondo no enciende") },
                            leadingIcon = { Icon(Icons.Default.Description, null) },
                            isError = err.descripcion != null,
                            supportingText = { err.descripcion?.let { Text(it) } },
                            minLines = 3, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                        OutlinedTextField(
                            value = ubicacion, onValueChange = vm::setUbicacion,
                            label = { Text("Ubicación *") }, placeholder = { Text("Ej.: Bloque B, piso 2, salón 204") },
                            leadingIcon = { Icon(Icons.Default.Place, null) },
                            isError = err.ubicacion != null,
                            supportingText = { err.ubicacion?.let { Text(it) } },
                            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                    }
                }
            }
        }
        // Barra fija: Guardar siempre visible (también en horizontal)
        Surface(shadowElevation = 8.dp, color = Color.White) {
            Button(
                onClick = { vm.guardar(onGuardado) },
                enabled = !guardando,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).heightIn(min = 52.dp)
            ) {
                Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp))
                Text("Guardar reporte", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO"))

@Composable
fun ListadoScreen(reportes: List<Reporte>) {
    Column(Modifier.fillMaxSize()) {
        Encabezado(
            "Reportes",
            if (reportes.isEmpty()) "Aún no hay novedades registradas" else "${reportes.size} registrados en este dispositivo"
        )
        if (reportes.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) {
                Box(Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Inbox, null, Modifier.size(44.dp), tint = Azul)
                }
                Spacer(Modifier.height(16.dp))
                Text("Sin reportes todavía", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Registra el primero en la pestaña «Registrar».", style = MaterialTheme.typography.bodyMedium)
            }
            return@Column
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(reportes, key = { it.id }) { r ->
                val col = colorCategoria(r.categoria)
                Card(
                    Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(Modifier.drawBehind { drawRect(col, size = androidx.compose.ui.geometry.Size(6.dp.toPx(), size.height)) }) {
                        Column(Modifier.padding(start = 20.dp, top = 14.dp, end = 14.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(36.dp).clip(CircleShape).background(col.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                    Icon(iconoCategoria(r.categoria), null, tint = col, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(r.categoria, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = col, modifier = Modifier.weight(1f))
                                Text(formato.format(Date(r.fecha)), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(r.descripcion, style = MaterialTheme.typography.bodyLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.width(4.dp))
                                Text(r.ubicacion, style = MaterialTheme.typography.bodyMedium)
                            }
                            AssistChip(
                                onClick = {}, label = { Text(r.estado) },
                                leadingIcon = { Icon(Icons.Default.Schedule, null, Modifier.size(18.dp)) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFFFF4D6), labelColor = Color(0xFF6B4E00), leadingIconContentColor = Color(0xFF6B4E00)),
                                border = null
                            )
                        }
                    }
                }
            }
        }
    }
}
