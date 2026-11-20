package co.fexe.campusreport.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun ChipEstado(estado: String) {
    val (fondo, texto) = colorEstado(estado)
    AssistChip(
        onClick = {}, label = { Text(estado) },
        leadingIcon = { Icon(iconoEstado(estado), null, Modifier.size(18.dp)) },
        colors = AssistChipDefaults.assistChipColors(containerColor = fondo, labelColor = texto, leadingIconContentColor = texto),
        border = null
    )
}

/** Filtro desplegable: muestra el título hasta que se elige un valor. */
@Composable
fun MenuFiltro(titulo: String, valor: String, opciones: List<String>, tag: String, onElegir: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = valor.isNotEmpty(), onClick = { abierto = true },
            label = { Text(if (valor.isEmpty()) titulo else valor) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(18.dp)) },
            modifier = Modifier.testTag(tag)
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(text = { Text("Todos") }, modifier = Modifier.testTag("op_todos"),
                onClick = { onElegir(""); abierto = false })
            opciones.forEach { o ->
                DropdownMenuItem(text = { Text(o) }, modifier = Modifier.testTag("op_$o"),
                    onClick = { onElegir(o); abierto = false })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BarraFiltros(f: Filtros, onCambio: (Filtros) -> Unit) {
    FlowRow(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        MenuFiltro("Estado", f.estado, Estados.TODOS, "filtro_estado") { onCambio(f.copy(estado = it)) }
        MenuFiltro("Categoría", f.categoria, CATEGORIAS, "filtro_categoria") { onCambio(f.copy(categoria = it)) }
        MenuFiltro("Lugar", f.lugar, Filtros.LUGARES, "filtro_lugar") { onCambio(f.copy(lugar = it)) }
        if (f.activos > 0) TextButton(onClick = { onCambio(Filtros()) }, modifier = Modifier.testTag("filtro_limpiar")) { Text("Limpiar") }
    }
}

@Composable
fun SinResultados(hayFiltros: Boolean, onLimpiar: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No hay resultados con estos filtros.", style = MaterialTheme.typography.titleMedium)
        if (hayFiltros) TextButton(onClick = onLimpiar) { Text("Limpiar filtros") }
    }
}

/** Tarjeta de un reporte; `pie` permite añadir acciones debajo del estado. */
@Composable
fun TarjetaReporte(r: Reporte, onClick: () -> Unit, pie: @Composable () -> Unit = {}) {
    val col = colorCategoria(r.categoria)
    Card(
        onClick = onClick,
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipEstado(r.estado)
                    if (r.ejemplo) Text("Ejemplo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                }
                pie()
            }
        }
    }
}

@Composable
fun ListadoScreen(
    todos: List<Reporte>, visibles: List<Reporte>, filtros: Filtros,
    onFiltros: (Filtros) -> Unit, onAbrir: (Reporte) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Encabezado(
            "Reportes",
            when {
                todos.isEmpty() -> "Aún no hay novedades registradas"
                filtros.activos == 0 -> "${todos.size} " + (if (todos.size == 1) "registrado" else "registrados") + " en este dispositivo"
                else -> "${visibles.size} de ${todos.size} con los filtros aplicados"
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
        BarraFiltros(filtros, onFiltros)
        if (visibles.isEmpty()) { SinResultados(filtros.activos > 0) { onFiltros(Filtros()) }; return@Column }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(visibles, key = { it.id }) { r -> TarjetaReporte(r, onClick = { onAbrir(r) }) }
        }
    }
}

/** Sección donde se cambia el estado de los reportes; incluye el control de los datos de ejemplo. */
@Composable
fun GestionScreen(
    todos: List<Reporte>, visibles: List<Reporte>, filtros: Filtros, onFiltros: (Filtros) -> Unit,
    onCambiar: (Reporte) -> Unit, cantidadEjemplos: Int, onCargarEjemplos: () -> Unit, onQuitarEjemplos: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Encabezado("Gestión", "Cambia el estado de cada reporte · ${todos.size} en total")
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White), shape = MaterialTheme.shapes.large
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Datos de ejemplo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "No son reportes reales: sirven para probar los filtros y el historial. " +
                                if (cantidadEjemplos > 0) "Hay $cantidadEjemplos cargados." else "Ahora no hay ejemplos cargados.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onCargarEjemplos, modifier = Modifier.testTag("cargar_ejemplos")) { Text("Cargar ${Ejemplos.CANTIDAD} ejemplos") }
                            if (cantidadEjemplos > 0) OutlinedButton(onClick = onQuitarEjemplos, modifier = Modifier.testTag("quitar_ejemplos")) { Text("Quitar") }
                        }
                    }
                }
            }
            item { BarraFiltros(filtros, onFiltros) }
            if (todos.isEmpty()) {
                item { Text("Aún no hay reportes para gestionar.", Modifier.padding(horizontal = 16.dp)) }
            } else if (visibles.isEmpty()) {
                item { SinResultados(filtros.activos > 0) { onFiltros(Filtros()) } }
            } else {
                items(visibles, key = { it.id }) { r ->
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        TarjetaReporte(r, onClick = { onCambiar(r) }) {
                            OutlinedButton(onClick = { onCambiar(r) }, modifier = Modifier.testTag("cambiar_estado")) {
                                Icon(Icons.Default.SwapHoriz, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Cambiar estado")
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Historial de movimientos (creación y cambios de estado), con los mismos filtros. */
@Composable
fun HistorialScreen(
    total: Int, visibles: List<EventoVista>, filtros: Filtros, onFiltros: (Filtros) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Encabezado(
            "Historial",
            if (filtros.activos == 0) "$total " + (if (total == 1) "movimiento registrado" else "movimientos registrados") else "${visibles.size} de $total movimientos con los filtros"
        )
        if (total == 0) {
            Text("Todavía no hay movimientos.", Modifier.padding(24.dp)); return@Column
        }
        BarraFiltros(filtros, onFiltros)
        if (visibles.isEmpty()) { SinResultados(filtros.activos > 0) { onFiltros(Filtros()) }; return@Column }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(visibles, key = { it.e.id }) { v ->
                Card(
                    Modifier.fillMaxWidth().testTag("evento"), shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(iconoCategoria(v.r.categoria), null, tint = colorCategoria(v.r.categoria), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(v.r.descripcion, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(formato.format(Date(v.e.fecha)), style = MaterialTheme.typography.bodySmall)
                        }
                        Text(v.r.ubicacion + if (v.r.ejemplo) " · Ejemplo" else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (v.e.estadoAnterior == null) {
                                Text("Registrado como", style = MaterialTheme.typography.bodySmall)
                            } else {
                                ChipEstado(v.e.estadoAnterior)
                                Icon(Icons.Default.ArrowForward, null, Modifier.size(16.dp))
                            }
                            ChipEstado(v.e.estadoNuevo)
                        }
                        if (v.e.nota.isNotBlank()) Text("«${v.e.nota}»", style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }
        }
    }
}

/** Elegir el nuevo estado de un reporte, con una nota opcional que queda en el historial. */
@Composable
fun CambiarEstadoDialog(r: Reporte, onConfirmar: (String, String) -> Unit, onCancelar: () -> Unit) {
    var elegido by rememberSaveable(r.id) { mutableStateOf(r.estado) }
    var nota by rememberSaveable(r.id) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Cambiar estado", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(r.descripcion + " · " + r.ubicacion, style = MaterialTheme.typography.bodyMedium)
                Text("Estado actual: ${r.estado}", style = MaterialTheme.typography.bodySmall)
                Estados.TODOS.forEach { e ->
                    Row(
                        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { elegido = e }
                            .padding(vertical = 2.dp).testTag("estado_$e"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = elegido == e, onClick = { elegido = e })
                        Text(e)
                    }
                }
                OutlinedTextField(
                    value = nota, onValueChange = { nota = it }, label = { Text("Nota (opcional)") },
                    placeholder = { Text("Ej.: técnico asignado") },
                    modifier = Modifier.fillMaxWidth().testTag("campo_nota"), shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmar(elegido, nota) }, enabled = elegido != r.estado, modifier = Modifier.testTag("guardar_estado")) { Text("Guardar estado") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

/** Detalle de un reporte con su historial y las acciones Editar / Eliminar / Cerrar. */
@Composable
fun DetalleDialog(r: Reporte, historial: List<Evento>, onEditar: () -> Unit, onEliminar: () -> Unit, onCerrar: () -> Unit) {
    val col = colorCategoria(r.categoria)
    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(iconoCategoria(r.categoria), null, tint = col) },
        title = { Text(r.categoria, color = col, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(r.descripcion)
                Text("Ubicación: ${r.ubicacion}", style = MaterialTheme.typography.bodyMedium)
                Text("Registrado: ${formato.format(Date(r.fecha))}" + if (r.ejemplo) " · dato de ejemplo" else "", style = MaterialTheme.typography.bodySmall)
                ChipEstado(r.estado)
                if (historial.isNotEmpty()) {
                    Text("Historial", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    historial.forEach { e ->
                        Text(
                            formato.format(Date(e.fecha)) + " · " + (e.estadoAnterior?.let { "$it → " } ?: "Registrado como ") + e.estadoNuevo,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
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
