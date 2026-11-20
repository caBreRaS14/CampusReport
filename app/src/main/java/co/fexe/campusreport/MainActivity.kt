package co.fexe.campusreport

import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.fexe.campusreport.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT)
        )
        setContent { CampusTheme { Surface(color = MaterialTheme.colorScheme.background) { App() } } }
    }
}

private data class Pestana(val tag: String, val titulo: String, val icono: ImageVector)

private val PESTANAS = listOf(
    Pestana("tab_registrar", "Registrar", Icons.Default.Edit),
    Pestana("tab_reportes", "Reportes", Icons.AutoMirrored.Filled.List),
    Pestana("tab_gestion", "Gestión", Icons.Default.Build),
    Pestana("tab_historial", "Historial", Icons.Default.History)
)

@Composable
fun App(vm: ReporteViewModel = viewModel()) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val reportes by vm.reportes.collectAsStateWithLifecycle()
    val eventos by vm.eventos.collectAsStateWithLifecycle()
    val visiblesRep by vm.visiblesReportes.collectAsStateWithLifecycle()
    val visiblesGes by vm.visiblesGestion.collectAsStateWithLifecycle()
    val historial by vm.historial.collectAsStateWithLifecycle()
    val fRep by vm.fReportes.collectAsStateWithLifecycle()
    val fGes by vm.fGestion.collectAsStateWithLifecycle()
    val fHis by vm.fHistorial.collectAsStateWithLifecycle()
    var detalleId by rememberSaveable { mutableStateOf<Long?>(null) }
    var modo by rememberSaveable { mutableIntStateOf(0) } // 0 detalle, 1 editar, 2 confirmar borrado
    var cambiandoId by rememberSaveable { mutableStateOf<Long?>(null) }
    val actual = reportes.firstOrNull { it.id == detalleId }
    val cambiando = reportes.firstOrNull { it.id == cambiandoId }
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.mensaje.collect { snack.showSnackbar(it) } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                PESTANAS.forEachIndexed { i, p ->
                    NavigationBarItem(
                        selected = pestana == i, onClick = { pestana = i }, modifier = Modifier.testTag(p.tag),
                        icon = {
                            if (i == 1) BadgedBox({ if (reportes.isNotEmpty()) Badge { Text("${reportes.size}") } }) { Icon(p.icono, null) }
                            else Icon(p.icono, null)
                        },
                        label = { Text(p.titulo) }
                    )
                }
            }
        }
    ) { pad ->
        Surface(Modifier.padding(pad).consumeWindowInsets(pad), color = MaterialTheme.colorScheme.background) {
            when (pestana) {
                0 -> FormularioScreen(vm) { pestana = 1 }
                1 -> ListadoScreen(reportes, visiblesRep, fRep, vm::setFReportes) { detalleId = it.id; modo = 0 }
                2 -> GestionScreen(
                    reportes, visiblesGes, fGes, vm::setFGestion, onCambiar = { cambiandoId = it.id },
                    cantidadEjemplos = reportes.count { it.ejemplo },
                    onCargarEjemplos = vm::cargarEjemplos, onQuitarEjemplos = vm::quitarEjemplos
                )
                else -> HistorialScreen(eventos.size, historial, fHis, vm::setFHistorial)
            }
        }
    }

    actual?.let { r ->
        val cerrar = { detalleId = null; modo = 0 }
        when (modo) {
            0 -> DetalleDialog(r, eventos.filter { it.reporteId == r.id }.sortedWith(compareBy({ it.fecha }, { it.id })), onEditar = { modo = 1 }, onEliminar = { modo = 2 }, onCerrar = cerrar)
            1 -> EditarDialog(r, vm, onListo = cerrar, onCancelar = { modo = 0 })
            else -> ConfirmarEliminarDialog(r, onConfirmar = { vm.borrar(r.id, cerrar) }, onCancelar = { modo = 0 })
        }
    }
    cambiando?.let { r ->
        CambiarEstadoDialog(r, onConfirmar = { nuevo, nota -> vm.cambiarEstado(r.id, nuevo, nota) { cambiandoId = null } },
            onCancelar = { cambiandoId = null })
    }
}
