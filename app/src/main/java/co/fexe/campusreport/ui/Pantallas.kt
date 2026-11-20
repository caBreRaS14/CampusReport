package co.fexe.campusreport.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import co.fexe.campusreport.data.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Encabezado(titulo: String, subtitulo: String) {
    val horizontal = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
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

/** Campos del reporte, compartidos por el formulario nuevo y el diálogo de edición. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CamposReporte(
    categoria: String, descripcion: String, ubicacion: Ubicacion, err: ErroresForm,
    onCategoria: (String) -> Unit, onDescripcion: (String) -> Unit, onUbicacion: (Ubicacion) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Categoría *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CATEGORIAS.forEach { c ->
                val col = colorCategoria(c)
                FilterChip(
                    modifier = Modifier.testTag("cat_$c"),
                    selected = categoria == c, onClick = { onCategoria(c) },
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
        err.categoria?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        OutlinedTextField(
            value = descripcion, onValueChange = onDescripcion,
            label = { Text("Descripción *") }, placeholder = { Text("Ej.: la luminaria del fondo no enciende") },
            leadingIcon = { Icon(Icons.Default.Description, null) },
            isError = err.descripcion != null,
            supportingText = { err.descripcion?.let { Text(it) } },
            minLines = 3, modifier = Modifier.fillMaxWidth().testTag("campo_descripcion"), shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        SelectorUbicacion(ubicacion, err.ubicacion, onUbicacion)
    }
}

/** Ubicación por selección (sin texto libre salvo «Otro») para evitar errores de escritura. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectorUbicacion(u: Ubicacion, error: String?, onCambio: (Ubicacion) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ubicación *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Campus.LUGARES.forEach { l ->
                FilterChip(
                    modifier = Modifier.testTag("lugar_$l"),
                    selected = u.lugar == l,
                    onClick = { onCambio(if (u.lugar == l) u else Ubicacion(lugar = l, otro = u.otro)) },
                    label = { Text(if (l == Campus.EDIFICIO) "Edificio / salón" else l) }
                )
            }
        }
        if (u.lugar == Campus.EDIFICIO) {
            Text("Edificio", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Campus.EDIFICIOS.forEach { e ->
                    FilterChip(modifier = Modifier.testTag("edificio_$e"), selected = u.edificio == e,
                        onClick = { onCambio(u.copy(edificio = e)) }, label = { Text("$e") })
                }
            }
            Text("Piso", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Campus.PISOS.forEach { p ->
                    FilterChip(modifier = Modifier.testTag("piso_$p"), selected = u.piso == p,
                        onClick = { onCambio(if (u.piso == p) u else u.copy(piso = p, salon = -1)) }, label = { Text("$p") })
                }
            }
            if (u.piso in Campus.PISOS) {
                Text("Salón (los salones se numeran por piso)", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Campus.salonesDe(u.piso).forEach { n ->
                        FilterChip(modifier = Modifier.testTag("salon_$n"), selected = u.salon == n,
                            onClick = { onCambio(u.copy(salon = n)) }, label = { Text("$n") })
                    }
                    FilterChip(modifier = Modifier.testTag("salon_0"), selected = u.salon == 0,
                        onClick = { onCambio(u.copy(salon = 0)) }, label = { Text("Zona común") })
                }
            }
        }
        if (u.lugar == Campus.OTRO) {
            OutlinedTextField(
                value = u.otro, onValueChange = { onCambio(u.copy(otro = it)) },
                label = { Text("¿Dónde? *") }, placeholder = { Text("Describe el lugar") },
                leadingIcon = { Icon(Icons.Default.Place, null) },
                isError = error != null, singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("campo_ubicacion"), shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
        }
        if (u.lugar.isNotEmpty() && error == null) {
            Text("Seleccionado: ${u.texto()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
fun FormularioScreen(vm: ReporteViewModel, onGuardado: () -> Unit) {
    val categoria by vm.categoria.collectAsState()
    val descripcion by vm.descripcion.collectAsState()
    val ubicacionCodigo by vm.ubicacion.collectAsState()
    val ubicacion = Ubicacion.desdeCodigo(ubicacionCodigo)
    val err by vm.errores.collectAsState()
    val guardando by vm.guardando.collectAsState()

    Column(Modifier.fillMaxSize().imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Encabezado("Nueva novedad", "Todos los campos son obligatorios. Se guarda solo en este dispositivo.")
            Card(
                Modifier.fillMaxWidth().padding(16.dp), shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    CamposReporte(categoria, descripcion, ubicacion, err, vm::setCategoria, vm::setDescripcion, vm::setUbicacion)
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
fun ChipEstado(estado: String) = AssistChip(
    onClick = {}, label = { Text(estado) },
    leadingIcon = { Icon(Icons.Default.Schedule, null, Modifier.size(18.dp)) },
    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFFFF4D6), labelColor = Color(0xFF6B4E00), leadingIconContentColor = Color(0xFF6B4E00)),
    border = null
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListadoScreen(
    todos: List<Reporte>, visibles: List<Reporte>, filtro: String,
    onFiltro: (String) -> Unit, onAbrir: (Reporte) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Encabezado(
            "Reportes",
            when {
                todos.isEmpty() -> "Aún no hay novedades registradas"
                filtro.isEmpty() -> "${todos.size} " + (if (todos.size == 1) "registrado" else "registrados") + " en este dispositivo"
                else -> "${visibles.size} de ${todos.size} · $filtro"
            }
        )
        if (todos.isEmpty()) {
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
        // Filtro por categoría
        FlowRow(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(modifier = Modifier.testTag("filtro_todas"), selected = filtro.isEmpty(), onClick = { onFiltro("") }, label = { Text("Todas") })
            CATEGORIAS.forEach { c ->
                val col = colorCategoria(c)
                FilterChip(
                    modifier = Modifier.testTag("filtro_$c"),
                    selected = filtro == c, onClick = { onFiltro(if (filtro == c) "" else c) },
                    label = { Text(c) },
                    leadingIcon = { Icon(iconoCategoria(c), null, Modifier.size(18.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = col.copy(alpha = 0.16f), selectedLabelColor = col,
                        selectedLeadingIconColor = col, iconColor = col.copy(alpha = 0.8f)
                    )
                )
            }
        }
        if (visibles.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No hay reportes en «$filtro».", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { onFiltro("") }) { Text("Ver todas") }
            }
            return@Column
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(visibles, key = { it.id }) { r ->
                val col = colorCategoria(r.categoria)
                Card(
                    onClick = { onAbrir(r) },
                    modifier = Modifier.fillMaxWidth().testTag("tarjeta"), shape = MaterialTheme.shapes.large,
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
                            ChipEstado(r.estado)
                        }
                    }
                }
            }
        }
    }
}

/** Detalle de un reporte con acciones Editar / Eliminar / Cerrar. */
@Composable
fun DetalleDialog(r: Reporte, onEditar: () -> Unit, onEliminar: () -> Unit, onCerrar: () -> Unit) {
    val col = colorCategoria(r.categoria)
    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(iconoCategoria(r.categoria), null, tint = col) },
        title = { Text(r.categoria, color = col, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(r.descripcion)
                Text("Ubicación: ${r.ubicacion}", style = MaterialTheme.typography.bodyMedium)
                Text("Registrado: ${formato.format(Date(r.fecha))}", style = MaterialTheme.typography.bodySmall)
                ChipEstado(r.estado)
            }
        },
        confirmButton = { TextButton(onClick = onEditar) { Icon(Icons.Default.Edit, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Editar") } },
        dismissButton = {
            Row {
                TextButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(6.dp)); Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onCerrar) { Text("Cerrar") }
            }
        }
    )
}

@Composable
fun EditarDialog(r: Reporte, vm: ReporteViewModel, onListo: () -> Unit, onCancelar: () -> Unit) {
    var cat by rememberSaveable(r.id) { mutableStateOf(r.categoria) }
    var desc by rememberSaveable(r.id) { mutableStateOf(r.descripcion) }
    var ubiCodigo by rememberSaveable(r.id) { mutableStateOf(Ubicacion.desdeTexto(r.ubicacion).codigo()) }
    val ubi = Ubicacion.desdeCodigo(ubiCodigo)
    var err by remember { mutableStateOf(ErroresForm()) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar reporte", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                CamposReporte(
                    cat, desc, ubi, err,
                    { cat = it; err = err.copy(categoria = null) },
                    { desc = it; err = err.copy(descripcion = null) },
                    { ubiCodigo = it.codigo(); err = err.copy(ubicacion = null) }
                )
            }
        },
        confirmButton = { Button(onClick = { err = vm.editar(r, cat, desc, ubi, onListo) }) { Text("Guardar cambios") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun ConfirmarEliminarDialog(r: Reporte, onConfirmar: () -> Unit, onCancelar: () -> Unit) = AlertDialog(
    onDismissRequest = onCancelar,
    icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
    title = { Text("¿Eliminar este reporte?") },
    text = { Text("«${r.descripcion}» se borrará de este dispositivo y no se podrá recuperar.") },
    confirmButton = { TextButton(onClick = onConfirmar) { Text("Eliminar", color = MaterialTheme.colorScheme.error) } },
    dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
)
