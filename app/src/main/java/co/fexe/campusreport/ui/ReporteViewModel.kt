package co.fexe.campusreport.ui

import android.app.Application
import androidx.lifecycle.*
import co.fexe.campusreport.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReporteViewModel(app: Application, private val saved: SavedStateHandle) : AndroidViewModel(app) {
    private val dao = AppDb.get(app).reporteDao()

    val reportes: StateFlow<List<Reporte>> = dao.todos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Borrador del formulario: sobrevive a rotación y a que el sistema cierre el proceso.
    val categoria = saved.getStateFlow("categoria", "")
    val descripcion = saved.getStateFlow("descripcion", "")
    val ubicacion = saved.getStateFlow("ubicacion", "")

    private val _errores = MutableStateFlow(ErroresForm())
    val errores: StateFlow<ErroresForm> = _errores

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    private val _mensaje = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensaje: SharedFlow<String> = _mensaje

    fun setCategoria(v: String) { saved["categoria"] = v; _errores.update { it.copy(categoria = null) } }
    fun setDescripcion(v: String) { saved["descripcion"] = v; _errores.update { it.copy(descripcion = null) } }
    fun setUbicacion(v: String) { saved["ubicacion"] = v; _errores.update { it.copy(ubicacion = null) } }

    /** Ignora pulsaciones mientras haya un guardado en curso (una intención = un registro). */
    fun guardar(onGuardado: () -> Unit) {
        if (_guardando.value) return
        val e = validar(categoria.value, descripcion.value, ubicacion.value)
        _errores.value = e
        if (e.hayErrores) { _mensaje.tryEmit("No se guardó: revisa los campos marcados."); return }
        _guardando.value = true
        viewModelScope.launch {
            try {
                dao.insertar(Reporte(
                    categoria = categoria.value,
                    descripcion = descripcion.value.trim(),
                    ubicacion = ubicacion.value.trim(),
                    fecha = System.currentTimeMillis()
                ))
                saved["categoria"] = ""; saved["descripcion"] = ""; saved["ubicacion"] = ""
                _mensaje.tryEmit("Reporte guardado en este dispositivo (pendiente de sincronización).")
                onGuardado()
            } finally { _guardando.value = false }
        }
    }
}
