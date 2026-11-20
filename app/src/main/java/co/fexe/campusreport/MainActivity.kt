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

@Composable
fun App(vm: ReporteViewModel = viewModel()) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val reportes by vm.reportes.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.mensaje.collect { snack.showSnackbar(it) } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                NavigationBarItem(pestana == 0, { pestana = 0 }, { Icon(Icons.Default.Edit, null) }, label = { Text("Registrar") })
                NavigationBarItem(pestana == 1, { pestana = 1 },
                    { BadgedBox({ if (reportes.isNotEmpty()) Badge { Text("${reportes.size}") } }) { Icon(Icons.AutoMirrored.Filled.List, null) } },
                    label = { Text("Reportes") })
            }
        }
    ) { pad ->
        Surface(Modifier.padding(pad).consumeWindowInsets(pad), color = MaterialTheme.colorScheme.background) {
            if (pestana == 0) FormularioScreen(vm) { pestana = 1 } else ListadoScreen(reportes)
        }
    }
}
