package co.fexe.campusreport.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import co.fexe.campusreport.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormularioScreen(vm: ReporteViewModel, onGuardado: () -> Unit) {
    val categoria by vm.categoria.collectAsState()
    val descripcion by vm.descripcion.collectAsState()
    val ubicacion by vm.ubicacion.collectAsState()
    val err by vm.errores.collectAsState()
    val guardando by vm.guardando.collectAsState()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nueva novedad", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Todos los campos son obligatorios. El reporte se guarda solo en este dispositivo.",
            style = MaterialTheme.typography.bodyMedium
        )

        Text("Categoría *", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CATEGORIAS.forEach { c ->
                FilterChip(selected = categoria == c, onClick = { vm.setCategoria(c) }, label = { Text(c) })
            }
        }
        err.categoria?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        OutlinedTextField(
            value = descripcion, onValueChange = vm::setDescripcion,
            label = { Text("Descripción *") }, placeholder = { Text("Ej.: la luminaria del fondo no enciende") },
            isError = err.descripcion != null,
            supportingText = { err.descripcion?.let { Text(it) } },
            minLines = 3, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        OutlinedTextField(
            value = ubicacion, onValueChange = vm::setUbicacion,
            label = { Text("Ubicación *") }, placeholder = { Text("Ej.: Bloque B, piso 2, salón 204") },
            isError = err.ubicacion != null,
            supportingText = { err.ubicacion?.let { Text(it) } },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        Button(
            onClick = { vm.guardar(onGuardado) },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) {
            Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Guardar reporte")
        }
    }
}

private val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO"))

@Composable
fun ListadoScreen(reportes: List<Reporte>) {
    if (reportes.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Aún no hay reportes. Registra el primero en la pestaña «Registrar».")
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Reportes (${reportes.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(reportes, key = { it.id }) { r ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(r.categoria, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(formato.format(Date(r.fecha)), style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Ubicación: ${r.ubicacion}")
                    Text(r.descripcion, style = MaterialTheme.typography.bodyMedium)
                    AssistChip(onClick = {}, label = { Text(r.estado) }, leadingIcon = { Icon(Icons.Default.Schedule, null, Modifier.size(18.dp)) })
                }
            }
        }
    }
}
